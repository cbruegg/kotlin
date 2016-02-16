/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.idea.codeInsight.generate;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/testData/codeInsight/generate/toString")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class GenerateToStringActionTestGenerated extends AbstractGenerateToStringActionTest {
    public void testAllFilesPresentInToString() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("idea/testData/codeInsight/generate/toString"), Pattern.compile("^(.+)\\.kt$"), true);
    }

    @TestMetadata("idea/testData/codeInsight/generate/toString/common")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Common extends AbstractGenerateToStringActionTest {
        public void testAllFilesPresentInCommon() throws Exception {
            KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("idea/testData/codeInsight/generate/toString/common"), Pattern.compile("^(.+)\\.kt$"), true);
        }

        @TestMetadata("annotation.kt")
        public void testAnnotation() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/common/annotation.kt");
            doTest(fileName);
        }

        @TestMetadata("dataClass.kt")
        public void testDataClass() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/common/dataClass.kt");
            doTest(fileName);
        }

        @TestMetadata("interface.kt")
        public void testInterface() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/common/interface.kt");
            doTest(fileName);
        }

        @TestMetadata("object.kt")
        public void testObject() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/common/object.kt");
            doTest(fileName);
        }
    }

    @TestMetadata("idea/testData/codeInsight/generate/toString/multipeTemplates")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class MultipeTemplates extends AbstractGenerateToStringActionTest {
        public void testAllFilesPresentInMultipeTemplates() throws Exception {
            KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("idea/testData/codeInsight/generate/toString/multipeTemplates"), Pattern.compile("^(.+)\\.kt$"), true);
        }

        @TestMetadata("arrays.kt")
        public void testArrays() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/multipeTemplates/arrays.kt");
            doTest(fileName);
        }

        @TestMetadata("multipleVars.kt")
        public void testMultipleVars() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/multipeTemplates/multipleVars.kt");
            doTest(fileName);
        }

        @TestMetadata("multipleVarsWithSuperClass.kt")
        public void testMultipleVarsWithSuperClass() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/multipeTemplates/multipleVarsWithSuperClass.kt");
            doTest(fileName);
        }

        @TestMetadata("noVars.kt")
        public void testNoVars() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/multipeTemplates/noVars.kt");
            doTest(fileName);
        }

        @TestMetadata("singleVar.kt")
        public void testSingleVar() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/multipeTemplates/singleVar.kt");
            doTest(fileName);
        }

        @TestMetadata("superClassNoVars.kt")
        public void testSuperClassNoVars() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/multipeTemplates/superClassNoVars.kt");
            doTest(fileName);
        }
    }

    @TestMetadata("idea/testData/codeInsight/generate/toString/singleTemplate")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class SingleTemplate extends AbstractGenerateToStringActionTest {
        public void testAllFilesPresentInSingleTemplate() throws Exception {
            KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("idea/testData/codeInsight/generate/toString/singleTemplate"), Pattern.compile("^(.+)\\.kt$"), true);
        }

        @TestMetadata("arrays.kt")
        public void testArrays() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/singleTemplate/arrays.kt");
            doTest(fileName);
        }

        @TestMetadata("multipleVars.kt")
        public void testMultipleVars() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/singleTemplate/multipleVars.kt");
            doTest(fileName);
        }

        @TestMetadata("multipleVarsWithSuperClass.kt")
        public void testMultipleVarsWithSuperClass() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/singleTemplate/multipleVarsWithSuperClass.kt");
            doTest(fileName);
        }

        @TestMetadata("noVars.kt")
        public void testNoVars() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/singleTemplate/noVars.kt");
            doTest(fileName);
        }

        @TestMetadata("singleVar.kt")
        public void testSingleVar() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/singleTemplate/singleVar.kt");
            doTest(fileName);
        }

        @TestMetadata("superClassNoVars.kt")
        public void testSuperClassNoVars() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/codeInsight/generate/toString/singleTemplate/superClassNoVars.kt");
            doTest(fileName);
        }
    }
}
