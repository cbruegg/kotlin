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

package org.jetbrains.kotlin.resolve.calls

import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinLiteFixture
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import java.io.File
import org.jetbrains.kotlin.resolve.lazy.JvmResolveUtil
import org.jetbrains.kotlin.resolve.calls.model.ArgumentMapping
import org.jetbrains.kotlin.resolve.calls.model.ArgumentMatch
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.calls.model.VariableAsFunctionResolvedCall
import org.jetbrains.kotlin.resolve.calls.callUtil.getParentResolvedCall
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.psi.KtFile
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.resolve.scopes.receivers.*

abstract class AbstractResolvedCallsTest : KotlinLiteFixture() {
    override fun createEnvironment(): KotlinCoreEnvironment = createEnvironmentWithMockJdk(ConfigurationKind.ALL)

    fun doTest(filePath: String) {
        val text = KotlinTestUtils.doLoadFile(File(filePath))!!

        val jetFile = KtPsiFactory(project).createFile(text.replace("<caret>", ""))
        val bindingContext = JvmResolveUtil.analyzeOneFileWithJavaIntegration(jetFile, environment).bindingContext

        val (element, cachedCall) = buildCachedCall(bindingContext, jetFile, text)

        val resolvedCall = if (cachedCall !is VariableAsFunctionResolvedCall) cachedCall
            else if ("(" == element?.text) cachedCall.functionCall
            else cachedCall.variableCall

        val resolvedCallInfoFileName = FileUtil.getNameWithoutExtension(filePath) + ".txt"
        KotlinTestUtils.assertEqualsToFile(File(resolvedCallInfoFileName), "$text\n\n\n${resolvedCall?.renderToText()}")
    }

    open protected fun buildCachedCall(
            bindingContext: BindingContext, jetFile: KtFile, text: String
    ): Pair<PsiElement?, ResolvedCall<out CallableDescriptor>?> {
        val element = jetFile.findElementAt(text.indexOf("<caret>"))!!
        val expression = element.getStrictParentOfType<KtExpression>()

        val cachedCall = expression?.getParentResolvedCall(bindingContext, strict = false)
        return Pair(element, cachedCall)
    }

}

private fun Receiver?.getText() = when (this) {
    is ExpressionReceiver -> "${expression.text} {${type}}"
    is ImplicitClassReceiver -> "Class{${type}}"
    is ExtensionReceiver -> "${type}Ext{${declarationDescriptor.getText()}}"
    null -> "NO_RECEIVER"
    else -> toString()
}

private fun ValueArgument.getText() = this.getArgumentExpression()?.text?.replace("\n", " ") ?: ""

private fun ArgumentMapping.getText() = when (this) {
    is ArgumentMatch -> {
        val parameterType = DescriptorRenderer.SHORT_NAMES_IN_TYPES.renderType(valueParameter.type)
        "${status.name}  ${valueParameter.name} : ${parameterType} ="
    }
    else -> "ARGUMENT UNMAPPED: "
}

private fun DeclarationDescriptor.getText(): String = when (this) {
    is ReceiverParameterDescriptor -> "${value.getText()}::this"
    else -> DescriptorRenderer.COMPACT_WITH_SHORT_TYPES.render(this)
}

private fun ResolvedCall<*>.renderToText(): String {
    return buildString {
        appendln("Resolved call:")
        appendln()

        if (candidateDescriptor != resultingDescriptor) {
            appendln("Candidate descriptor: ${candidateDescriptor!!.getText()}")
        }
        appendln("Resulting descriptor: ${resultingDescriptor!!.getText()}")
        appendln()

        appendln("Explicit receiver kind = ${explicitReceiverKind}")
        appendln("Dispatch receiver = ${dispatchReceiver.getText()}")
        appendln("Extension receiver = ${extensionReceiver.getText()}")

        val valueArguments = call.valueArguments
        if (!valueArguments.isEmpty()) {
            appendln()
            appendln("Value arguments mapping:")
            appendln()

            for (valueArgument in valueArguments) {
                val argumentText = valueArgument!!.getText()
                val argumentMappingText = getArgumentMapping(valueArgument).getText()

                appendln("$argumentMappingText $argumentText")
            }
        }
    }
}
