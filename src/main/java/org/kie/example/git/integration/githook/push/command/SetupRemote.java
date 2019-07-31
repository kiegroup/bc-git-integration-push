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

package org.kie.example.git.integration.githook.push.command;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.kie.example.git.integration.githook.push.integration.GitRemoteIntegration;

public class SetupRemote implements Command {

    private final GitRemoteIntegration integration;

    public SetupRemote(final GitRemoteIntegration integration) {
        this.integration = integration;
    }

    public String execute(final Git git,
                          final Path currentPath) throws IOException, GitAPIException {
        final StoredConfig storedConfig = git.getRepository().getConfig();

        final String repoName = new GetRepoName().execute(currentPath);
        final String remoteURL = integration.createRepository(repoName);
        storedConfig.setString("remote", "origin", "url", remoteURL);
        storedConfig.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");

        final List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        for (Ref value : branches) {
            final String shortName = value.getName().replaceAll("refs/heads/", "");
            storedConfig.setString("branch", shortName, "remote", "origin");
            storedConfig.setString("branch", shortName, "merge", "refs/heads/" + shortName);
        }
        storedConfig.save();

        git.push().setCredentialsProvider(integration.getCredentialsProvider()).call();
        return repoName;
    }
}
