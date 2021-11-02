/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.webapp.runner.webappconfig.slimui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.packaging.artifacts.Artifact;
import com.microsoft.azure.toolkit.ide.appservice.model.AzureArtifactConfig;
import com.microsoft.azure.toolkit.ide.appservice.model.DeploymentSlotConfig;
import com.microsoft.azure.toolkit.ide.appservice.model.MonitorConfig;
import com.microsoft.azure.toolkit.ide.appservice.webapp.model.WebAppConfig;
import com.microsoft.azure.toolkit.ide.appservice.webapp.model.WebAppDeployRunConfigurationModel;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifact;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifactManager;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifactType;
import com.microsoft.azure.toolkit.intellij.common.AzureSettingPanel;
import com.microsoft.azure.toolkit.intellij.common.Draft;
import com.microsoft.azure.toolkit.intellij.common.DraftResourceGroup;
import com.microsoft.azure.toolkit.lib.appservice.model.DiagnosticConfig;
import com.microsoft.azure.toolkit.lib.appservice.model.LogLevel;
import com.microsoft.azure.toolkit.lib.appservice.model.OperatingSystem;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.intellij.webapp.runner.Constants;
import com.microsoft.azure.toolkit.intellij.webapp.runner.webappconfig.IntelliJWebAppSettingModel;
import com.microsoft.azure.toolkit.intellij.webapp.runner.webappconfig.WebAppConfiguration;
import com.microsoft.azure.toolkit.lib.appservice.DraftServicePlan;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;

import javax.swing.*;
import java.util.Optional;

public class WebAppSlimSettingPanel extends AzureSettingPanel<WebAppConfiguration> {
    private JPanel pnlRoot;
    private WebAppDeployConfigurationPanel pnlDeployment;

    public WebAppSlimSettingPanel(@NotNull Project project, @NotNull WebAppConfiguration webAppConfiguration) {
        super(project, false);
        $$$setupUI$$$();
    }

    @NotNull
    @Override
    public String getPanelName() {
        return "Deploy to Azure";
    }

    @Override
    public void disposeEditor() {
    }

    @NotNull
    @Override
    public JPanel getMainPanel() {
        return pnlRoot;
    }

    @NotNull
    @Override
    protected JComboBox<Artifact> getCbArtifact() {
        return new ComboBox<>();
    }

    @NotNull
    @Override
    protected JLabel getLblArtifact() {
        return new JLabel();
    }

    @NotNull
    @Override
    protected JComboBox<MavenProject> getCbMavenProject() {
        return new ComboBox<>();
    }

    @NotNull
    @Override
    protected JLabel getLblMavenProject() {
        return new JLabel();
    }

    @Override
    protected void resetFromConfig(@NotNull WebAppConfiguration configuration) {
        final Subscription subscription = Subscription.builder().id(configuration.getSubscriptionId()).build();
        final ResourceGroup resourceGroup = configuration.isCreatingResGrp() ?
                new DraftResourceGroup(subscription, configuration.getResourceGroup()) :
                ResourceGroup.builder().name(configuration.getResourceGroup()).build();
        final PricingTier pricingTier = StringUtils.isEmpty(configuration.getPricing()) ? null : PricingTier.fromString(configuration.getPricing());
        final Runtime runtime = Optional.ofNullable(configuration.getModel()).map(IntelliJWebAppSettingModel::getRuntime).orElse(null);
        final OperatingSystem operatingSystem = Optional.ofNullable(runtime).map(Runtime::getOperatingSystem).orElse(null);
        final Region region = StringUtils.isEmpty(configuration.getRegion()) ? null : Region.fromName(configuration.getRegion());
        final AppServicePlanEntity servicePlanEntity = configuration.isCreatingAppServicePlan() ?
                new DraftServicePlan(subscription, configuration.getAppServicePlanName(), region, operatingSystem, pricingTier) :
                AppServicePlanEntity.builder().id(configuration.getAppServicePlanId()).build();
        final DeploymentSlotConfig slotConfig = !configuration.isDeployToSlot() ? null :
                StringUtils.equals(configuration.getSlotName(), Constants.CREATE_NEW_SLOT) ?
                        DeploymentSlotConfig.builder().name(configuration.getNewSlotName())
                                .configurationSource(configuration.getNewSlotConfigurationSource()).build() :
                        DeploymentSlotConfig.builder().name(configuration.getSlotName()).build();
        final DiagnosticConfig diagnosticConfig = DiagnosticConfig.builder()
                .enableApplicationLog(configuration.getModel().isEnableApplicationLog())
                .applicationLogLevel(LogLevel.fromString(configuration.getModel().getApplicationLogLevel()))
                .enableDetailedErrorMessage(configuration.getModel().isEnableDetailedErrorMessage())
                .enableFailedRequestTracing(configuration.getModel().isEnableFailedRequestTracing())
                .enableWebServerLogging(configuration.getModel().isEnableWebServerLogging())
                .webServerRetentionPeriod(configuration.getModel().getWebServerRetentionPeriod())
                .webServerLogQuota(configuration.getModel().getWebServerLogQuota()).build();
        final MonitorConfig monitorConfig = MonitorConfig.builder().diagnosticConfig(diagnosticConfig).build();
        final WebAppConfig.WebAppConfigBuilder<?, ?> configBuilder = WebAppConfig.builder().name(configuration.getWebAppName())
                .resourceId(configuration.getWebAppId())
                .subscription(subscription)
                .resourceGroup(resourceGroup)
                .runtime(runtime)
                .servicePlan(servicePlanEntity)
                .deploymentSlot(slotConfig);
        final WebAppConfig webAppConfig = !configuration.isCreatingNew() ? configBuilder.build() :
                configBuilder.region(region).pricingTier(pricingTier).monitorConfig(monitorConfig).build();
        final AzureArtifactConfig artifactConfig = AzureArtifactConfig.builder()
                .artifactIdentifier(configuration.getArtifactIdentifier())
                .artifactType(configuration.getAzureArtifactType().name()).build();
        final WebAppDeployRunConfigurationModel runConfigurationModel = WebAppDeployRunConfigurationModel.builder()
                .webAppConfig(webAppConfig)
                .artifactConfig(artifactConfig)
                .deployToRoot(configuration.isDeployToRoot())
                .slotPanelVisible(configuration.isSlotPanelVisible())
                .openBrowserAfterDeployment(configuration.isOpenBrowserAfterDeployment()).build();
        pnlDeployment.setData(runConfigurationModel);
    }

    @Override
    protected void apply(@NotNull WebAppConfiguration configuration) {
        final WebAppDeployRunConfigurationModel runConfigurationModel = pnlDeployment.getData();
        Optional.ofNullable(runConfigurationModel.getWebAppConfig()).ifPresent(webAppConfig -> {
            configuration.setWebAppId(webAppConfig.getResourceId());
            configuration.setSubscriptionId(webAppConfig.getSubscriptionId());
            configuration.setResourceGroup(webAppConfig.getResourceGroupName());
            configuration.setWebAppName(webAppConfig.getName());
            configuration.saveRuntime(webAppConfig.getRuntime());
            configuration.setCreatingNew(StringUtils.isEmpty(webAppConfig.getResourceId()));
            if (configuration.isCreatingNew()) {
                configuration.setRegion(webAppConfig.getRegion().getName());
                configuration.setPricing(webAppConfig.getPricingTier().getSize());
                configuration.setCreatingResGrp(webAppConfig.getResourceGroup() instanceof Draft);
                configuration.setCreatingAppServicePlan(webAppConfig.getServicePlan() instanceof Draft);
                configuration.setAppServicePlanName(webAppConfig.getServicePlan().getName());
                Optional.ofNullable(webAppConfig.getMonitorConfig()).map(MonitorConfig::getDiagnosticConfig).ifPresent(diagnosticConfig -> {
                    configuration.getModel().setEnableApplicationLog(diagnosticConfig.isEnableApplicationLog());
                    configuration.getModel().setApplicationLogLevel(diagnosticConfig.getApplicationLogLevel().getValue());
                    configuration.getModel().setEnableWebServerLogging(diagnosticConfig.isEnableWebServerLogging());
                    configuration.getModel().setWebServerLogQuota(diagnosticConfig.getWebServerLogQuota());
                    configuration.getModel().setWebServerRetentionPeriod(diagnosticConfig.getWebServerRetentionPeriod());
                    configuration.getModel().setEnableDetailedErrorMessage(diagnosticConfig.isEnableDetailedErrorMessage());
                    configuration.getModel().setEnableFailedRequestTracing(diagnosticConfig.isEnableFailedRequestTracing());
                });
            } else {
                configuration.setAppServicePlanId(webAppConfig.getServicePlan().getId());
            }
            configuration.setDeployToSlot(webAppConfig.getDeploymentSlot() != null);
            Optional.ofNullable(webAppConfig.getDeploymentSlot()).ifPresent(slot -> {
                configuration.setSlotName(slot.getName());
                if (slot.isNewCreate()) {
                    configuration.setSlotName(Constants.CREATE_NEW_SLOT);
                    configuration.setNewSlotName(slot.getName());
                    configuration.setNewSlotConfigurationSource(slot.getConfigurationSource());
                }
            });
        });
        Optional.ofNullable(runConfigurationModel.getArtifactConfig()).ifPresent(artifactConfig -> {
            final AzureArtifact azureArtifact = AzureArtifactManager.getInstance(project)
                    .getAzureArtifactById(AzureArtifactType.valueOf(artifactConfig.getArtifactType()), artifactConfig.getArtifactIdentifier());
            configuration.saveArtifact(azureArtifact);
            if (ApplicationManager.getApplication().isDispatchThread()) {
                syncBeforeRunTasks(azureArtifact, configuration);
            } else {
                ApplicationManager.getApplication().invokeLater(() -> syncBeforeRunTasks(azureArtifact, configuration));
            }
        });
        configuration.setDeployToRoot(runConfigurationModel.isDeployToRoot());
        configuration.setSlotPanelVisible(runConfigurationModel.isSlotPanelVisible());
        configuration.setOpenBrowserAfterDeployment(runConfigurationModel.isOpenBrowserAfterDeployment());
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        pnlDeployment = new WebAppDeployConfigurationPanel(project);
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    private void $$$setupUI$$$() {
    }
}
