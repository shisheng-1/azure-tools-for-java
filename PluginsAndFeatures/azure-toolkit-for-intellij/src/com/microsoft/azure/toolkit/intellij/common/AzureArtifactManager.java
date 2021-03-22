/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.common;

import com.intellij.openapi.externalSystem.model.project.ExternalProjectPojo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.Artifact;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.intellij.util.GradleUtils;
import com.microsoft.intellij.util.MavenRunTaskUtil;
import com.microsoft.intellij.util.MavenUtils;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.plugins.gradle.model.ExternalProject;
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AzureArtifactManager {
    private static Map<Project, AzureArtifactManager> projectAzureArtifactManagerMap = new HashMap<>();
    private final Project project;

    private AzureArtifactManager(Project project) {
        this.project = project;
    }

    public static AzureArtifactManager getInstance(@NotNull Project project) {
        return projectAzureArtifactManagerMap.computeIfAbsent(project, key ->
                                                                      new AzureArtifactManager(project)
                                                             );
    }

    public List<AzureArtifact> getAllSupportedAzureArtifacts() {
        return prepareAzureArtifacts(null);
    }

    public List<AzureArtifact> getSupportedAzureArtifactsForSpringCloud() {
        return prepareAzureArtifacts(packaging -> StringUtils.equals(packaging, MavenConstants.TYPE_JAR));
    }

    @AzureOperation(
        name = "common|artifact.get_id",
        params = {"artifact.getName()"},
        type = AzureOperation.Type.TASK
    )
    public String getArtifactIdentifier(AzureArtifact artifact) {
        switch (artifact.getType()) {
            case Gradle:
                return getGradleProjectId((ExternalProjectPojo) artifact.getReferencedObject());
            case Maven:
                return artifact.getReferencedObject().toString();
            case Artifact:
                return ((Artifact) artifact.getReferencedObject()).getOutputFilePath();
            case File:
                return getFileForDeployment(artifact);
            default:
                return null;
        }
    }

    @AzureOperation(
        name = "common|artifact.get_file",
        params = {"artifact.getName()"},
        type = AzureOperation.Type.TASK
    )
    public String getFileForDeployment(AzureArtifact artifact) {
        switch (artifact.getType()) {
            case Gradle:
                return GradleUtils.getTargetFile(project, (ExternalProjectPojo) artifact.getReferencedObject());
            case Maven:
                return MavenUtils.getSpringBootFinalJarFilePath(project, (MavenProject) artifact.getReferencedObject());
            case Artifact:
                return ((Artifact) artifact.getReferencedObject()).getOutputFilePath();
            case File:
                return ((VirtualFile) artifact.getReferencedObject()).getPath();
            default:
                return null;
        }
    }

    public AzureArtifact getAzureArtifactById(String artifactId) {
        return getAllSupportedAzureArtifacts().stream().filter(artifact -> StringUtils.equals(getArtifactIdentifier(
                artifact), artifactId)).findFirst().orElse(null);
    }

    public AzureArtifact getAzureArtifactById(AzureArtifactType azureArtifactType, String artifactId) {
        return azureArtifactType == AzureArtifactType.File ? AzureArtifact.createFromFile(artifactId) :
               getAllSupportedAzureArtifacts().stream().filter(artifact -> StringUtils.equals(getArtifactIdentifier(
                artifact), artifactId)).findFirst().orElse(null);
    }

    public String getPackaging(AzureArtifact artifact) {
        switch (artifact.getType()) {
            case Gradle:
                return FileNameUtils.getExtension(GradleUtils.getTargetFile(project,
                                                                            (ExternalProjectPojo) artifact.getReferencedObject()));
            case Maven:
                return ((MavenProject) artifact.getReferencedObject()).getPackaging();
            case Artifact:
                return FileNameUtils.getExtension(((Artifact) artifact.getReferencedObject()).getOutputFilePath());
            case File:
                return FileNameUtils.getExtension(getFileForDeployment(artifact));
            default:
                return null;
        }
    }

    public boolean equalsAzureArtifactIdentifier(AzureArtifact artifact1, AzureArtifact artifact2) {
        if (Objects.isNull(artifact1) || Objects.isNull(artifact2)) {
            return artifact1 == artifact2;
        }
        return StringUtils.equals(getArtifactIdentifier(artifact1), getArtifactIdentifier(artifact2));
    }

    private String getGradleProjectId(ExternalProjectPojo gradleProjectPojo) {
        ExternalProject externalProject = getRelatedExternalProject(gradleProjectPojo);
        return Objects.nonNull(externalProject) ? externalProject.getQName() : null;
    }

    private ExternalProject getRelatedExternalProject(ExternalProjectPojo gradleProjectPojo) {
        ExternalProject externalProject =
                ExternalProjectDataCache.getInstance(project).getRootExternalProject(gradleProjectPojo.getPath());
        return externalProject;
    }

    private List<AzureArtifact> prepareAzureArtifacts(Predicate<String> packagingFilter) {
        List<AzureArtifact> azureArtifacts = new ArrayList<>();
        List<ExternalProjectPojo> gradleProjects = GradleUtils.listGradleProjects(project);
        if (Objects.nonNull(gradleProjects)) {
            azureArtifacts.addAll(gradleProjects.stream()
                                                .map(AzureArtifact::createFromGradleProject)
                                                .collect(Collectors.toList()));
        }
        List<MavenProject> mavenProjects = MavenProjectsManager.getInstance(project).getProjects();
        if (Objects.nonNull(mavenProjects)) {
            azureArtifacts.addAll(
                    mavenProjects.stream().map(AzureArtifact::createFromMavenProject).collect(Collectors.toList()));
        }
        List<Artifact> artifactList = MavenRunTaskUtil.collectProjectArtifact(project);
        if (Objects.nonNull(artifactList)) {
            azureArtifacts.addAll(
                    artifactList.stream().map(AzureArtifact::createFromArtifact).collect(Collectors.toList()));
        }
        if (packagingFilter == null) {
            return azureArtifacts;
        }
        return azureArtifacts.stream().filter(artifact -> packagingFilter.test(getPackaging(artifact))).collect(Collectors.toList());

    }
}
