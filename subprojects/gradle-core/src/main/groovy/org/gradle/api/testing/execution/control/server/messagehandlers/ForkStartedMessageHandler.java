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
package org.gradle.api.testing.execution.control.server.messagehandlers;

import org.apache.mina.core.session.IoSession;
import org.gradle.api.testing.execution.PipelineDispatcher;
import org.gradle.api.testing.execution.control.messages.client.ForkStartedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Eyckmans
 */
public class ForkStartedMessageHandler extends AbstractTestServerControlMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ForkStartedMessageHandler.class);

    protected ForkStartedMessageHandler(PipelineDispatcher pipelineDispatcher) {
        super(pipelineDispatcher);
    }

    public void handle(IoSession ioSession, Object controlMessage) {
        final ForkStartedMessage message = (ForkStartedMessage) controlMessage;
        final int forkId = message.getForkId();

        pipelineDispatcher.initializeFork(forkId, ioSession);
        pipelineDispatcher.scheduleExecuteTest(forkId);
    }
}
