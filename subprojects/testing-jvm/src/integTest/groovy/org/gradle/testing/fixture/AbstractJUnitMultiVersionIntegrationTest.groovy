/*
 * Copyright 2023 the original author or authors.
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

package org.gradle.testing.fixture

import org.gradle.integtests.fixtures.MultiVersionIntegrationSpec

/**
 * Base class for multi-version integration tests that use various versions of JUnit.  This class provides the common
 * configuration for the build script and test framework.  Subclasses must provide test framework specific configuration
 * for the build script.
 *
 * The following depicts the general pattern of implementation where function-specific tests extend from this class,
 * while framework-specific classes extend from the function-specific classes.  The framework-specific classes should
 * provide the {@link BuildScriptConfiguration} required by this class via a reusable trait.
 *
 *  ┌──────────────────────────────────────────────┐
 *  │   AbstractJUnitMultiVersionIntegrationTest   │
 *  └──────────────────────────────────────────────┘
 *                        ▲
 *                        │
 *  ┌─────────────────────┴────────────────────────┐
 *  │       AbstractTestTaskIntegrationTest        │
 *  └──────────────────────────────────────────────┘
 *                        ▲
 *                        │
 *  ┌─────────────────────┴────────────────────────┐      ┌──────────────────────────────────────────────┐
 *  │     JUnitJupiterTestTaskIntegrationTest      ├─────►│          JUnitJupiterMultiversionTest        │
 *  └──────────────────────────────────────────────┘      └──────────────────────────────────────────────┘
 *
 */
abstract class AbstractJUnitMultiVersionIntegrationTest extends MultiVersionIntegrationSpec {
    abstract BuildScriptConfiguration getBuildScriptConfiguration()

    String getTestFrameworkDependencies() {
        return buildScriptConfiguration.testFrameworkDependencies
    }

    String getConfigureTestFramework() {
        return buildScriptConfiguration.configureTestFramework
    }

    String includeCategoryOrTag(String categoryOrTag) {
        return "${buildScriptConfiguration.includeCategoryOrTagConfigurationElement} '${categoryOrTag}'"
    }

    String excludeCategoryOrTag(String categoryOrTag) {
        return "${buildScriptConfiguration.excludeCategoryOrTagConfigurationElement} '${categoryOrTag}'"
    }

    def setup() {
        executer.withRepositoryMirrors()
    }

    /**
     * Test framework specific configuration for the build script.
     *
     * These values will be provided by test framework traits that can be attached to any test class.  As such, we use an interface
     * to ensure type safety and avoid decoupling of these methods in the trait classes.  The trait classes simply have to provide
     * an implementation of this interface specific to the test framework.
     */
    interface BuildScriptConfiguration {
        String getTestFrameworkDependencies()
        String getConfigureTestFramework()
        String getIncludeCategoryOrTagConfigurationElement()
        String getExcludeCategoryOrTagConfigurationElement()
    }
}