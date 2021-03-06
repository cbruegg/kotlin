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

package org.jetbrains.kotlin.resolve.scopes

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.utils.takeSnapshot
import org.jetbrains.kotlin.utils.Printer

class LexicalScopeImpl @JvmOverloads constructor(
        parent: HierarchicalScope,
        override val ownerDescriptor: DeclarationDescriptor,
        override val isOwnerDescriptorAccessibleByLabel: Boolean,
        override val implicitReceiver: ReceiverParameterDescriptor?,
        override val kind: LexicalScopeKind,
        redeclarationHandler: RedeclarationHandler = RedeclarationHandler.DO_NOTHING,
        initialize: LexicalScopeImpl.InitializeHandler.() -> Unit = {}
): LexicalScope, WritableScopeStorage(redeclarationHandler) {
    override val parent = parent.takeSnapshot()

    init {
        InitializeHandler().initialize()
    }

    override fun getContributedClassifier(name: Name, location: LookupLocation) = getClassifier(name)
    override fun getContributedVariables(name: Name, location: LookupLocation) = getVariables(name)

    override fun getContributedFunctions(name: Name, location: LookupLocation) = getFunctions(name)
    override fun getContributedDescriptors(kindFilter: DescriptorKindFilter, nameFilter: (Name) -> Boolean) = addedDescriptors

    override fun toString(): String = kind.toString()

    override fun printStructure(p: Printer) {
        p.println(javaClass.simpleName, ": ", kind, "; for descriptor: ", ownerDescriptor.name,
                  " with implicitReceiver: ", implicitReceiver?.value ?: "NONE", " {")
        p.pushIndent()

        p.print("parent = ")
        parent.printStructure(p.withholdIndentOnce())

        p.popIndent()
        p.println("}")
    }

    inner class InitializeHandler() {

        fun addVariableDescriptor(variableDescriptor: VariableDescriptor): Unit
                = this@LexicalScopeImpl.addVariableOrClassDescriptor(variableDescriptor)

        fun addFunctionDescriptor(functionDescriptor: FunctionDescriptor): Unit
                = this@LexicalScopeImpl.addFunctionDescriptorInternal(functionDescriptor)

        fun addClassifierDescriptor(classifierDescriptor: ClassifierDescriptor): Unit
                = this@LexicalScopeImpl.addVariableOrClassDescriptor(classifierDescriptor)

    }
}
