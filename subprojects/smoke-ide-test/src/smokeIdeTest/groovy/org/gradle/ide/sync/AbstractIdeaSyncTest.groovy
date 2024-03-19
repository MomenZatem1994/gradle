/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.ide.sync


import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.junit4.JUnit4StarterRule
import com.intellij.ide.starter.junit4.JUnit4StarterRuleKt
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.models.VMOptions
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.IDECommandLine
import com.intellij.ide.starter.runner.IDERunContext
import com.intellij.tools.ide.performanceTesting.commands.CommandChain
import com.intellij.tools.ide.performanceTesting.commands.GeneralCommandChainKt
import kotlin.Unit
import kotlin.jvm.functions.Function1
import kotlin.time.DurationKt
import kotlin.time.DurationUnit
import org.gradle.integtests.fixtures.executer.GradleDistribution
import org.gradle.integtests.fixtures.executer.IntegrationTestBuildContext
import org.gradle.integtests.fixtures.executer.UnderDevelopmentGradleDistribution
import org.gradle.test.fixtures.file.CleanupTestDirectory
import org.gradle.test.fixtures.file.TestFile
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Rule
import org.junit.rules.TestName
import spock.lang.Specification
import spock.lang.Timeout

import java.nio.file.Path

@SuppressWarnings("GroovyAccessibility")
@CleanupTestDirectory
@Timeout(600)
abstract class AbstractIdeaSyncTest extends Specification {

    @Rule
    final TestName testName = new TestName()

    @Rule
    final JUnit4StarterRule testContextFactory = JUnit4StarterRuleKt.initJUnit4StarterRule()

    @Rule
    final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider(getClass())

    private final GradleDistribution distribution = new UnderDevelopmentGradleDistribution(getBuildContext())

    private IntegrationTestBuildContext getBuildContext() {
        return IntegrationTestBuildContext.INSTANCE
    }

    protected void ideaSync(String buildType, String version) {
        TestCase testCase = new TestCase(
            getIdeaCommunity(buildType, version),
            getTestProject(),
            Collections.EMPTY_LIST,
            false
        )

        IDETestContext testContext = testContextFactory
            .initializeTestContext(testName.methodName, testCase, false)
            .prepareProjectCleanImport()

        useLocalGradleDistributionIn(testContext)

        runSync(testContext)
    }

    protected TestFile getTestDirectory() {
        temporaryFolder.testDirectory
    }

    private void useLocalGradleDistributionIn(IDETestContext testContext) {
        testContext.paths.configDir.resolve("options/gradle.default.xml").toFile() << """
            <application>
                <component name="GradleDefaultProjectSettings">
                    <option name="distributionType" value="LOCAL" />
                    <option name="gradleHome" value="${distribution.gradleHomeDir.absolutePath}" />
                </component>
            </application>
        """
    }

    private void runSync(IDETestContext testContext) {
        testContext.invokeMethod("runIDE-SBKQj6I", [
            new Function1<IDERunContext, IDECommandLine>() {
                @Override
                IDECommandLine invoke(IDERunContext ideRunContext) {
                    return new IDECommandLine.OpenTestCaseProject(testContext, [])
                }
            },
            GeneralCommandChainKt.exitApp(new CommandChain(), false),
            null,
            DurationKt.toDuration(10, DurationUnit.MINUTES),
            true,
            "",
            false,
            false,
            new Function1<IDERunContext, Unit>() {
                @Override
                Unit invoke(IDERunContext vmOptions) {
                    return Unit.INSTANCE
                }
            }
        ]
        )
    }

    protected TestFile file(Object... path) {
        if (path.length == 1 && path[0] instanceof TestFile) {
            return path[0] as TestFile
        }
        testDirectory.file(path)
    }

    private LocalProjectInfo getTestProject() {
        new LocalProjectInfo(
            testDirectory.toPath(),
            false,
            DurationKt.toDuration(1, DurationUnit.MINUTES),
            new Function1<IDETestContext, Unit>() {
                @Override
                Unit invoke(IDETestContext context) {
                    return Unit.INSTANCE
                }
            },
            "Project under test"
        )
    }

    private static IdeInfo getIdeaCommunity(String buildType, String version) {
        new IdeInfo(
            IdeProductProvider.IC.productCode,
            IdeProductProvider.IC.platformPrefix,
            IdeProductProvider.IC.executableFileName,
            buildType,
            IdeProductProvider.IC.additionalModules,
            IdeProductProvider.IC.buildNumber,
            version,
            IdeProductProvider.IC.tag,
            IdeProductProvider.IC.downloadURI,
            IdeProductProvider.IC.fullName,
            IdeProductProvider.IC.getInstaller
        )
    }
}
