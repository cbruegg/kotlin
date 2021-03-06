/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.decompiler.classFile

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.compiled.ClsStubBuilder
import com.intellij.psi.impl.compiled.ClassFileStubBuilder
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.util.indexing.FileContent
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.idea.caches.IDEKotlinBinaryClassCache
import org.jetbrains.kotlin.idea.decompiler.stubBuilder.*
import org.jetbrains.kotlin.idea.decompiler.textBuilder.LoggingErrorReporter
import org.jetbrains.kotlin.load.kotlin.AbstractBinaryClassAnnotationAndConstantLoader
import org.jetbrains.kotlin.load.kotlin.KotlinClassFinder
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.serialization.ProtoBuf
import org.jetbrains.kotlin.serialization.deserialization.ErrorReporter
import org.jetbrains.kotlin.serialization.deserialization.NameResolver
import org.jetbrains.kotlin.serialization.deserialization.TypeTable
import org.jetbrains.kotlin.serialization.jvm.JvmProtoBufUtil
import org.jetbrains.kotlin.storage.LockBasedStorageManager

open class KotlinClsStubBuilder : ClsStubBuilder() {
    override fun getStubVersion() = ClassFileStubBuilder.STUB_VERSION + 1

    override fun buildFileStub(content: FileContent): PsiFileStub<*>? {
        val file = content.file

        if (isKotlinInternalCompiledFile(file)) {
            return null
        }

        return doBuildFileStub(file)
    }

    fun doBuildFileStub(file: VirtualFile): PsiFileStub<KtFile>? {
        val kotlinClassHeaderInfo = IDEKotlinBinaryClassCache.getKotlinBinaryClassHeaderData(file)!!
        val header = kotlinClassHeaderInfo.classHeader
        val classId = kotlinClassHeaderInfo.classId
        val packageFqName = classId.packageFqName
        if (!header.metadataVersion.isCompatible()) {
            return createIncompatibleAbiVersionFileStub()
        }

        val components = createStubBuilderComponents(file, packageFqName)
        if (header.kind == KotlinClassHeader.Kind.MULTIFILE_CLASS) {
            val partFiles = findMultifileClassParts(file, classId, header)
            return createMultifileClassStub(header, partFiles, classId.asSingleFqName(), components)
        }

        val annotationData = header.data
        if (annotationData == null) {
            LOG.error("Corrupted kotlin header for file ${file.name}")
            return null
        }
        val strings = header.strings
        if (strings == null) {
            LOG.error("String table not found in file ${file.name}")
            return null
        }
        return when (header.kind) {
            KotlinClassHeader.Kind.CLASS -> {
                if (classId.isLocal) return null
                val (nameResolver, classProto) = JvmProtoBufUtil.readClassDataFrom(annotationData, strings)
                val context = components.createContext(nameResolver, packageFqName, TypeTable(classProto.typeTable))
                createTopLevelClassStub(classId, classProto, context)
            }
            KotlinClassHeader.Kind.FILE_FACADE -> {
                val (nameResolver, packageProto) = JvmProtoBufUtil.readPackageDataFrom(annotationData, strings)
                val context = components.createContext(nameResolver, packageFqName, TypeTable(packageProto.typeTable))
                createFileFacadeStub(packageProto, classId.asSingleFqName(), context)
            }
            else -> throw IllegalStateException("Should have processed " + file.path + " with header $header")
        }
    }

    private fun createStubBuilderComponents(file: VirtualFile, packageFqName: FqName): ClsStubBuilderComponents {
        val classFinder = DirectoryBasedClassFinder(file.parent!!, packageFqName)
        val classDataFinder = DirectoryBasedDataFinder(classFinder, LOG)
        val annotationLoader = AnnotationLoaderForClassFileStubBuilder(classFinder, LoggingErrorReporter(LOG))
        return ClsStubBuilderComponents(classDataFinder, annotationLoader, file)
    }

    companion object {
        val LOG = Logger.getInstance(KotlinClsStubBuilder::class.java)
    }
}

class AnnotationLoaderForClassFileStubBuilder(
        kotlinClassFinder: KotlinClassFinder,
        errorReporter: ErrorReporter
) : AbstractBinaryClassAnnotationAndConstantLoader<ClassId, Unit, ClassIdWithTarget>(
        LockBasedStorageManager.NO_LOCKS, kotlinClassFinder, errorReporter) {

    override fun loadTypeAnnotation(proto: ProtoBuf.Annotation, nameResolver: NameResolver): ClassId =
            nameResolver.getClassId(proto.id)

    override fun loadConstant(desc: String, initializer: Any) = null

    override fun loadAnnotation(
            annotationClassId: ClassId, source: SourceElement, result: MutableList<ClassId>
    ): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
        result.add(annotationClassId)
        return null
    }

    override fun loadPropertyAnnotations(
            propertyAnnotations: List<ClassId>, fieldAnnotations: List<ClassId>, fieldUseSiteTarget: AnnotationUseSiteTarget
    ): List<ClassIdWithTarget> {
        return propertyAnnotations.map { ClassIdWithTarget(it, null) } +
               fieldAnnotations.map { ClassIdWithTarget(it, fieldUseSiteTarget ) }
    }

    override fun transformAnnotations(annotations: List<ClassId>) = annotations.map { ClassIdWithTarget(it, null) }
}
