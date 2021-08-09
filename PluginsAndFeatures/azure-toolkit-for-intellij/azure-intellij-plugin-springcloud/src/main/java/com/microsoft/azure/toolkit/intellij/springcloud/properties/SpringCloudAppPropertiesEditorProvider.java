package com.microsoft.azure.toolkit.intellij.springcloud.properties;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.common.properties.IntellijShowPropertiesViewAction;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import org.jetbrains.annotations.NotNull;

public class SpringCloudAppPropertiesEditorProvider implements FileEditorProvider, DumbAware {

    public static final String SPRING_CLOUD_APP_PROPERTY_TYPE = "SPRINGCLOUDAPP_FILE_TYPE";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return virtualFile.getFileType().getName().equals(getEditorTypeId());
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        final SpringCloudApp app = (SpringCloudApp) virtualFile.getUserData(IntellijShowPropertiesViewAction.AZURE_RESOURCE_KEY);
        assert app != null;
        return new SpringCloudAppPropertiesEditor(project, app, virtualFile);
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return SPRING_CLOUD_APP_PROPERTY_TYPE;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
