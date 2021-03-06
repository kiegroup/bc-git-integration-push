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

import java.io.IOException;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHCreateRepositoryBuilder;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kie.example.git.integration.githook.push.properties.GitRemoteProperties;

public class GitHubIntegration implements GitRemoteIntegration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubIntegration.class);
    private final GHOrganization organization;

    private GitHub github;
    private String user;
    private CredentialsProvider credentialsProvider;
    private boolean useSSH;

    public GitHubIntegration(final GitRemoteProperties props) {
        try {
            if (props.getToken().isEmpty()) {
                LOGGER.info("Unsecured connection using username and password since token is not provided in properties file");
                github = GitHub.connectToEnterprise(props.getRemoteGitUrl(), props.getLogin(), props.getPassword());
                credentialsProvider = new UsernamePasswordCredentialsProvider(props.getLogin(), props.getPassword());
            } else {
                LOGGER.info("Connecting using secured token from properties file");
                github = GitHub.connectUsingOAuth(props.getRemoteGitUrl(), props.getToken());
                credentialsProvider = new UsernamePasswordCredentialsProvider(props.getToken(), "");
            }
            user = github.getMyself().getLogin();
            if (!props.getGitHubOrg().isEmpty()){
                organization = github.getOrganization(props.getGitHubOrg());
            } else {
                organization = null;
            }
            useSSH = props.getUseSSH();
        } catch (IOException e) {
            LOGGER.error("An unexpected error occurred.", e);
            throw new RuntimeException(e);
        }
    }

    public String createRepository(String repoName) {
        try {
            final GHCreateRepositoryBuilder builder;
            if (organization != null){
                builder = organization.createRepository(repoName);
            } else {
                builder = github.createRepository(repoName);
            }
            final GHRepository repo = builder.description("Created from Business Central: " + repoName)
                    .autoInit(false)
                    .create();
            if (user.isEmpty()) {
                user = repo.getOwnerName();
            }
            if (useSSH){
                return repo.getSshUrl();
            }
            return repo.getHttpTransportUrl();
        } catch (IOException e) {
            LOGGER.error("An unexpected error occurred.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }
}
