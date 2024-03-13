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

import com.intellij.ide.starter.di.DiContainerKt
import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.ide.command.CommandChain
import com.intellij.ide.starter.junit4.JUnit4StarterRule
import com.intellij.ide.starter.junit4.JUnit4StarterRuleKt
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.models.VMOptions
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.IDECommandLine
import com.jetbrains.performancePlugin.commands.chain.GeneralCommandChainKt
import kotlin.Unit
import kotlin.jvm.functions.Function1
import kotlin.time.DurationKt
import kotlin.time.DurationUnit
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.junit.Rule
import org.junit.rules.TestName
import org.kodein.di.BindSingletonKt
import org.kodein.di.Copy
import org.kodein.di.DI
import static org.kodein.di.BindSingletonKt.*

import java.nio.file.Path

@SuppressWarnings("GroovyAccessibility")
abstract class AbstractIdeaSyncTest extends AbstractIntegrationSpec {

    @Rule
    JUnit4StarterRule testContextFactory = JUnit4StarterRuleKt.initJUnit4StarterRule()

    @Rule
    TestName testName = new TestName()

    def setup() {
        DiContainerKt.di = DI.Companion.invoke(false, new Function1<DI.MainBuilder, Unit>() {
            @Override
            Unit invoke(DI.MainBuilder mainBuilder) {
                mainBuilder.extend(DiContainerKt.di, true, Copy.NonCached.INSTANCE)
                return Unit.INSTANCE
            }
        })
    }

    protected void ideaSync(String buildType, String version) {
        TestCase testCase = new TestCase(
            getIdeaCommunity(buildType, version),
            getTestProject(),
            new Function1<IDETestContext, IDETestContext>() {
                @Override
                IDETestContext invoke(IDETestContext ideTestContext) {
                    return ideTestContext
                }
            },
            Collections.EMPTY_LIST,
            false
        )

        IDETestContext testContext = testContextFactory
            .initializeTestContext(testName.methodName, testCase, false)
            .prepareProjectCleanImport()

        useLocalGradleDistributionIn(testContext)

        runSync(testContext)
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
            IDECommandLine.OpenTestCaseProject.INSTANCE,
            GeneralCommandChainKt.exitApp(new CommandChain(), false),
            null,
            DurationKt.toDuration(10, DurationUnit.MINUTES),
            true,
            "",
            false,
            false,
            new Function1<VMOptions, VMOptions>() {
                @Override
                VMOptions invoke(VMOptions vmOptions) {
                    vmOptions.removeProfilerAgents()
                    vmOptions.inHeadlessMode()
                    return vmOptions
                }
            }
        ]
        )
    }

    private LocalProjectInfo getTestProject() {
        new LocalProjectInfo(
            testDirectory.toPath(),
            false,
            0L,
            new Function1<Path, Path>() {
                @Override
                Path invoke(Path path) {
                    return path
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
        )
    }
}
