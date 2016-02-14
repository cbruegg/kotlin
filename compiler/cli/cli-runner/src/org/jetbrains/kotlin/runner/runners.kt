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

package org.jetbrains.kotlin.runner

import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.util.jar.Attributes
import java.util.jar.JarFile

class RunnerException(message: String) : RuntimeException(message)

abstract class AbstractRunner : Runner {
    protected abstract val className: String

    protected abstract fun createClassLoader(classpath: Classpath): ClassLoader

    override fun run(classpath: Classpath, arguments: List<String>) {
        val classLoader = createClassLoader(classpath)

        val mainClass = try {
            classLoader.loadClass(className)
        }
        catch (e: ClassNotFoundException) {
            throw RunnerException("could not find or load main class $className")
        }

        val main = try {
            mainClass.getDeclaredMethod("main", Array<String>::class.java)
        }
        catch (e: NoSuchMethodException) {
            throw RunnerException("'main' method not found in class $className")
        }

        if (!Modifier.isStatic(main.modifiers)) {
            throw RunnerException(
                    "'main' method of class $className is not static. " +
                    "Please ensure that 'main' is either a top level Kotlin function, " +
                    "a member function annotated with @JvmStatic, or a static Java method"
            )
        }

        try {
            main.invoke(null, arguments.toTypedArray())
        }
        catch (e: IllegalAccessException) {
            throw RunnerException("'main' method of class $className is not public")
        }
        catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }
}

class MainClassRunner(override val className: String) : AbstractRunner() {
    override fun createClassLoader(classpath: Classpath): ClassLoader =
            URLClassLoader(classpath.urls, null)
}

class JarRunner(private val path: String) : AbstractRunner() {
    override val className: String =
            try {
                val jar = JarFile(path)
                try {
                    jar.manifest.mainAttributes.getValue(Attributes.Name.MAIN_CLASS)
                }
                finally {
                    jar.close()
                }
            }
            catch (e: IOException) {
                throw RunnerException("could not read manifest from " + path + ": " + e.message)
            }
            ?: throw RunnerException("no Main-Class entry found in manifest in $path")

    override fun createClassLoader(classpath: Classpath): ClassLoader {
        // 'kotlin *.jar' ignores the passed classpath as 'java -jar' does
        // TODO: warn on non-empty classpath?

        return URLClassLoader(arrayOf(File(path).toURI().toURL()), null)
    }
}

abstract class CompilerRunner(private val home: File) : Runner {
    private val preloaderMain: Method

    init {
        val preloaderJar = File(home, "lib/kotlin-preloader.jar")
        val preloader = URLClassLoader(arrayOf(preloaderJar.toURI().toURL()), null).loadClass("org.jetbrains.kotlin.preloading.Preloader")
        preloaderMain = preloader.getDeclaredMethod("main", Array<String>::class.java)
    }

    protected fun runCompilerClass(fqName: String, arguments: List<String>) {
        val args = listOf("-cp", File(home, "lib/kotlin-compiler.jar").path) + listOf(fqName) + arguments
        preloaderMain(null, args.toTypedArray())
    }
}

class ReplRunner(home: File) : CompilerRunner(home) {
    override fun run(classpath: Classpath, arguments: List<String>) {
        require(arguments.isEmpty()) { "REPL runner cannot be instantiated with arguments" }

        runCompilerClass("org.jetbrains.kotlin.cli.jvm.repl.ReplFromTerminal", classpath.files.map(File::getPath))
    }
}

class ScriptRunner(home: File, private val scriptPath: String) : CompilerRunner(home) {
    override fun run(classpath: Classpath, arguments: List<String>) {
        unwrapInvocationTargetTwice {
            runCompilerClass(
                    "org.jetbrains.kotlin.cli.jvm.ScriptRunner",
                    listOf(scriptPath, classpath.files.joinToString(separator = File.pathSeparator, transform = File::getPath)) +
                    arguments
            )
        }
    }
}

class ExpressionRunner(home: File, private val code: String) : CompilerRunner(home) {
    override fun run(classpath: Classpath, arguments: List<String>) {
        unwrapInvocationTargetTwice {
            // TODO: make the rest of the arguments available in the expression or warn that they are ignored
            runCompilerClass(
                    "org.jetbrains.kotlin.cli.jvm.ExpressionRunner",
                    listOf(code, classpath.files.joinToString(separator = File.pathSeparator, transform = File::getPath))
            )
        }
    }
}

inline fun unwrapInvocationTargetTwice(block: () -> Unit) =
        try {
            block()
        }
        catch (e: InvocationTargetException) {
            // Unwrapping exception one more time here because Preloader also invokes the compiler via reflection
            (e.targetException as? InvocationTargetException)?.let { throw it.targetException }
            throw e.targetException
        }
