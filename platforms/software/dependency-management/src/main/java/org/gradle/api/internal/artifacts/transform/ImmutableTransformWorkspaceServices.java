/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.api.internal.artifacts.transform;

import org.gradle.api.internal.cache.CacheConfigurationsInternal;
import org.gradle.cache.UnscopedCacheBuilderFactory;
import org.gradle.cache.internal.CrossBuildInMemoryCache;
import org.gradle.internal.Try;
import org.gradle.internal.execution.UnitOfWork;
import org.gradle.internal.execution.history.ExecutionHistoryStore;
import org.gradle.internal.execution.workspace.WorkspaceProvider;
import org.gradle.internal.execution.workspace.impl.FinerGrainedImmutableWorkspaceProvider;
import org.gradle.internal.file.FileAccessTimeJournal;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;
import java.io.File;

@NotThreadSafe
public class ImmutableTransformWorkspaceServices implements TransformWorkspaceServices, Closeable {
    private final CrossBuildInMemoryCache<UnitOfWork.Identity, Try<TransformExecutionResult>> identityCache;
    private final FinerGrainedImmutableWorkspaceProvider workspaceProvider;

    public ImmutableTransformWorkspaceServices(
        String cacheDisplayName,
        File cacheBaseDir,
        UnscopedCacheBuilderFactory cacheFactory,
        FileAccessTimeJournal fileAccessTimeJournal,
        ExecutionHistoryStore executionHistoryStore,
        CrossBuildInMemoryCache<UnitOfWork.Identity, Try<TransformExecutionResult>> identityCache,
        CacheConfigurationsInternal cacheConfigurations
    ) {
        this.workspaceProvider = FinerGrainedImmutableWorkspaceProvider.withExternalHistory(
            cacheDisplayName,
            cacheBaseDir,
            cacheFactory,
            fileAccessTimeJournal,
            executionHistoryStore,
            cacheConfigurations
        );
        this.identityCache = identityCache;
    }

    @Override
    public WorkspaceProvider getWorkspaceProvider() {
        return workspaceProvider;
    }

    @Override
    public CrossBuildInMemoryCache<UnitOfWork.Identity, Try<TransformExecutionResult>> getIdentityCache() {
        return identityCache;
    }

    @Override
    public void close() {
        workspaceProvider.close();
    }
}
