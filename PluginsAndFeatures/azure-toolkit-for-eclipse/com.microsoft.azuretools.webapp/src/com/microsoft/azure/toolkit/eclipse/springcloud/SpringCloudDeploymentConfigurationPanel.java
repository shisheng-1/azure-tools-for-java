/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.springcloud;

import com.microsoft.azure.toolkit.eclipse.common.artifact.AzureArtifact;
import com.microsoft.azure.toolkit.eclipse.common.artifact.AzureArtifactManager;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureArtifactComboBox;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureComboBox;
import com.microsoft.azure.toolkit.eclipse.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.eclipse.common.form.AzureForm;
import com.microsoft.azure.toolkit.eclipse.springcloud.component.SpringCloudAppComboBox;
import com.microsoft.azure.toolkit.eclipse.springcloud.component.SpringCloudClusterComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.model.IArtifact;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.springcloud.AzureSpringCloud;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;
import com.microsoft.azure.toolkit.lib.springcloud.config.SpringCloudAppConfig;
import com.microsoft.azure.toolkit.lib.springcloud.config.SpringCloudDeploymentConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SpringCloudDeploymentConfigurationPanel extends Composite implements AzureForm<SpringCloudAppConfig> {
    private final AzureArtifactComboBox cbArtifact;
    private final SubscriptionComboBox cbSubscription;
    private final SpringCloudClusterComboBox cbCluster;
    private final SpringCloudAppComboBox cbApp;
    private Label lblBeforeDeploy;
    private Button btnBuildMavenProject;

    /**
     * Create the composite.
     *
     * @param parent
     */
    public SpringCloudDeploymentConfigurationPanel(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new GridLayout(2, false));

        Label lblArtifact = new Label(this, SWT.NONE);
        lblArtifact.setText("Artifact:");

        cbArtifact = new AzureArtifactComboBox(this);
        cbArtifact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblSubscription = new Label(this, SWT.NONE);
        lblSubscription.setText("Subscription:");

        cbSubscription = new SubscriptionComboBox(this);
        cbSubscription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblService = new Label(this, SWT.NONE);
        lblService.setText("Service:");

        cbCluster = new SpringCloudClusterComboBox(this);
        cbCluster.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblApp = new Label(this, SWT.NONE);
        lblApp.setText("App:");

        cbApp = new SpringCloudAppComboBox(this);
        cbApp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        lblBeforeDeploy = new Label(this, SWT.NONE);
        lblBeforeDeploy.setText("Before deploy:");

        btnBuildMavenProject = new Button(this, SWT.CHECK);
        btnBuildMavenProject.setText("Build maven project");
        init();

    }

    private void init() {
        this.cbArtifact.addValueChangedListener(this::onArtifactChanged);
        this.cbSubscription.addValueChangedListener(this::onSubscriptionChanged);
        this.cbCluster.addValueChangedListener(this::onClusterChanger);
        this.cbSubscription.setRequired(true);
        this.cbCluster.setRequired(true);
        this.cbApp.setRequired(true);
        this.cbArtifact.setRequired(true);
    }

    private void onClusterChanger(SpringCloudCluster cluster) {
        this.cbApp.setCluster(cluster);
    }

    private void onSubscriptionChanged(Subscription subscription) {
        this.cbCluster.setSubscription(subscription);
    }

    private void onArtifactChanged(AzureArtifact azureArtifact) {
        //TODO(andxu): disable auto build when choosing file
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public synchronized void setFormData(SpringCloudAppConfig appConfig) {
        final SpringCloudCluster cluster = Azure.az(AzureSpringCloud.class).cluster(appConfig.getClusterName());
        if (Objects.nonNull(cluster) && !cluster.app(appConfig.getAppName()).exists()) {
            this.cbApp.addLocalItem(cluster.app(appConfig));
        }
        final SpringCloudDeploymentConfig deploymentConfig = appConfig.getDeployment();
        Optional.ofNullable(deploymentConfig.getArtifact()).map(a -> ((WrappedAzureArtifact) a))
                .ifPresent((a -> this.cbArtifact.setValue(a.getArtifact())));
        Optional.ofNullable(appConfig.getSubscriptionId())
                .ifPresent((id -> this.cbSubscription.setValue(new AzureComboBox.ItemReference<>(id, Subscription::getId))));
        Optional.ofNullable(appConfig.getClusterName())
                .ifPresent((id -> this.cbCluster.setValue(new AzureComboBox.ItemReference<>(id, SpringCloudCluster::name))));
        Optional.ofNullable(appConfig.getAppName())
                .ifPresent((id -> this.cbApp.setValue(new AzureComboBox.ItemReference<>(id, SpringCloudApp::name))));
    }

    @Nullable
    public SpringCloudAppConfig getFormData() {
        SpringCloudAppConfig appConfig = this.cbApp.getValue().entity().getConfig();
        if (Objects.isNull(appConfig)) {
            appConfig = SpringCloudAppConfig.builder()
                    .deployment(SpringCloudDeploymentConfig.builder().build())
                    .build();
        }
        return this.getFormData(appConfig);
    }

    public SpringCloudAppConfig getFormData(SpringCloudAppConfig appConfig) {
        final SpringCloudDeploymentConfig deploymentConfig = appConfig.getDeployment();
        appConfig.setSubscriptionId(this.cbSubscription.getValue().getId());
        appConfig.setClusterName(this.cbCluster.getValue().name());
        appConfig.setAppName(this.cbApp.getValue().name());
        deploymentConfig.setArtifact(new WrappedAzureArtifact(this.cbArtifact.getValue()));
        return appConfig;
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(
                this.cbArtifact,
                this.cbSubscription,
                this.cbCluster,
                this.cbApp
        );
    }

    @Override
    public List<AzureValidationInfo> validateData() {
        return AzureForm.super.validateData();
    }

    private static class WrappedAzureArtifact implements IArtifact {
        private final AzureArtifact artifact;

        public WrappedAzureArtifact(@Nonnull final AzureArtifact artifact) {
            this.artifact = artifact;
        }

        @Override
        public File getFile() {
            return AzureArtifactManager.getInstance().getFileForDeployment(artifact);
        }

        public AzureArtifact getArtifact() {
            return artifact;
        }

    }
}
