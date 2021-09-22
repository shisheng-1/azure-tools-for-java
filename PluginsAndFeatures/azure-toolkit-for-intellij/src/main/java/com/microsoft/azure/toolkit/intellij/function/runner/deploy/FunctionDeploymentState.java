/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.function.runner.deploy;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.microsoft.azure.toolkit.intellij.common.AzureRunProfileState;
import com.microsoft.azure.toolkit.intellij.function.runner.core.FunctionUtils;
import com.microsoft.azure.toolkit.lib.appservice.service.IFunctionApp;
import com.microsoft.azure.toolkit.lib.appservice.task.DeployFunctionAppTask;
import com.microsoft.azure.toolkit.lib.common.exception.AzureExecutionException;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.common.telemetry.AzureTelemetry;
import com.microsoft.azure.toolkit.lib.function.FunctionAppConfig;
import com.microsoft.azure.toolkit.lib.function.FunctionAppService;
import com.microsoft.azure.toolkit.lib.legacy.function.configurations.FunctionConfiguration;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;
import com.microsoft.intellij.RunProcessHandler;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class FunctionDeploymentState extends AzureRunProfileState<IFunctionApp> {

    private final FunctionDeployConfiguration functionDeployConfiguration;
    private final FunctionDeployModel deployModel;
    private File stagingFolder;

    /**
     * Place to execute the Web App deployment task.
     */
    public FunctionDeploymentState(Project project, FunctionDeployConfiguration functionDeployConfiguration) {
        super(project);
        this.functionDeployConfiguration = functionDeployConfiguration;
        this.deployModel = functionDeployConfiguration.getModel();
    }

    @Nullable
    @Override
    @AzureOperation(name = "function.deploy.state", type = AzureOperation.Type.ACTION)
    public IFunctionApp executeSteps(@NotNull RunProcessHandler processHandler, @NotNull Operation operation) throws IOException {
        final RunStateMessenger messenger = new RunStateMessenger(processHandler);
        AzureMessager.getContext().setMessager(messenger);

        // Create or update function app
        final IFunctionApp functionApp = createOrUpdateFunctionApp();
        // Compile function
        stagingFolder = FunctionUtils.getTempStagingFolder();
        prepareStagingFolder(stagingFolder, processHandler, operation);
        // Deploy function to Azure
        new DeployFunctionAppTask(functionApp, stagingFolder, null).execute();

        operation.trackProperties(AzureTelemetry.getActionContext().getProperties());
        return functionApp;
    }

    private IFunctionApp createOrUpdateFunctionApp(){
        final FunctionAppConfig config = deployModel.getFunctionAppConfig();
        config.setAppSettings(FunctionUtils.loadAppSettingsFromSecurityStorage(functionDeployConfiguration.getAppSettingsKey()));
        return FunctionAppService.getInstance().createFunctionApp(config);
    }

    @AzureOperation(
            name = "function.prepare_staging_folder_detail",
            params = {"stagingFolder.getName()", "this.deployModel.getFunctionAppConfig().getName()"},
            type = AzureOperation.Type.TASK
    )
    private void prepareStagingFolder(File stagingFolder, RunProcessHandler processHandler, final @NotNull Operation operation) {
        AzureTaskManager.getInstance().read(() -> {
            final Path hostJsonPath = FunctionUtils.getDefaultHostJson(project);
            final PsiMethod[] methods = FunctionUtils.findFunctionsByAnnotation(functionDeployConfiguration.getModule());
            final Path folder = stagingFolder.toPath();
            try {
                final Map<String, FunctionConfiguration> configMap =
                        FunctionUtils.prepareStagingFolder(folder, hostJsonPath, functionDeployConfiguration.getModule(), methods);
                operation.trackProperty(TelemetryConstants.TRIGGER_TYPE, StringUtils.join(FunctionUtils.getFunctionBindingList(configMap), ","));
            } catch (final AzureExecutionException | IOException e) {
                final String error = String.format("failed prepare staging folder[%s]", folder);
                throw new AzureToolkitRuntimeException(error, e);
            }
        });
    }

    @Override
    protected Operation createOperation() {
        return TelemetryManager.createOperation(TelemetryConstants.FUNCTION, TelemetryConstants.DEPLOY_FUNCTION_APP);
    }

    @Override
    @AzureOperation(
            name = "function.complete_deployment",
            params = {"this.deployModel.getFunctionAppConfig().getName()"},
            type = AzureOperation.Type.TASK
    )
    protected void onSuccess(IFunctionApp result, @NotNull RunProcessHandler processHandler) {
        processHandler.setText(message("appService.deploy.hint.succeed"));
        processHandler.notifyComplete();
        FunctionUtils.cleanUpStagingFolder(stagingFolder);
    }

    @Override
    protected void onFail(@NotNull Throwable error, @NotNull RunProcessHandler processHandler) {
        super.onFail(error, processHandler);
        FunctionUtils.cleanUpStagingFolder(stagingFolder);
    }

    @Override
    protected Map<String, String> getTelemetryMap() {
        return deployModel.getTelemetryProperties();
    }
}
