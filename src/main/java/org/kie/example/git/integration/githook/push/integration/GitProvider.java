/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.example.git.integration.githook.push.integration;

import org.kie.example.git.integration.githook.push.properties.GitRemoteProperties;

public enum GitProvider {
    GIT_HUB {
        public GitRemoteIntegration getRemoteIntegration(GitRemoteProperties prop) {
            return new GitHubIntegration(prop);
        }
    },
    GIT_LAB {
        public GitRemoteIntegration getRemoteIntegration(GitRemoteProperties prop) {
            return new GitLabIntegration(prop);
        }
    };

    public abstract GitRemoteIntegration getRemoteIntegration(GitRemoteProperties prop);
}
