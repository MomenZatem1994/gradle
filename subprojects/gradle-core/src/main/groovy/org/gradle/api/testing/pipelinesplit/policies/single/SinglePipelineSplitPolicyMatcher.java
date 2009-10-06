/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.testing.pipelinesplit.policies.single;

import org.gradle.api.testing.execution.PipelineConfig;
import org.gradle.api.testing.fabric.TestClassRunInfo;
import org.gradle.api.testing.pipelinesplit.policies.AbstractSplitPolicyMatcher;

/**
 * @author Tom Eyckmans
 */
public class SinglePipelineSplitPolicyMatcher extends AbstractSplitPolicyMatcher {

    public SinglePipelineSplitPolicyMatcher(PipelineConfig config) {
        super(config);
    }

    public boolean match(TestClassRunInfo testClassRunInfo) {
        return true;
    }
}
