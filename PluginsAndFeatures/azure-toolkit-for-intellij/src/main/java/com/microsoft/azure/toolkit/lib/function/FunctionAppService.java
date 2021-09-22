/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.lib.function;

import com.microsoft.azure.toolkit.lib.appservice.ApplicationInsightsConfig;
import com.microsoft.azure.toolkit.lib.appservice.config.RuntimeConfig;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.appservice.model.WebContainer;
import com.microsoft.azure.toolkit.lib.appservice.service.IFunctionApp;
import com.microsoft.azure.toolkit.lib.appservice.task.CreateOrUpdateFunctionAppTask;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;

import javax.annotation.Nonnull;
import java.util.Optional;

public class FunctionAppService {

    private static final FunctionAppService instance = new FunctionAppService();

    public static FunctionAppService getInstance() {
        return FunctionAppService.instance;
    }

    public FunctionAppConfig getFunctionAppConfigFromExistingFunction(@Nonnull final IFunctionApp functionApp) {
        return FunctionAppConfig.builder()
                .resourceId(functionApp.id())
                .name(functionApp.name())
                .region(functionApp.entity().getRegion())
                .resourceGroup(ResourceGroup.builder().name(functionApp.resourceGroup()).build())
                .subscription(Subscription.builder().id(functionApp.subscriptionId()).build())
                .runtime(functionApp.getRuntime())
                .servicePlan(AppServicePlanEntity.builder().id(functionApp.entity().getAppServicePlanId()).build()).build();
    }

    public IFunctionApp createFunctionApp(final FunctionAppConfig config) {
        return (IFunctionApp) new CreateOrUpdateFunctionAppTask(parseConfig(config)).execute();
    }

    // todo: add monitor config to app service create/update task
    private com.microsoft.azure.toolkit.lib.appservice.config.FunctionAppConfig parseConfig(final FunctionAppConfig config) {
        com.microsoft.azure.toolkit.lib.appservice.config.FunctionAppConfig result = new com.microsoft.azure.toolkit.lib.appservice.config.FunctionAppConfig();
        return (com.microsoft.azure.toolkit.lib.appservice.config.FunctionAppConfig) result
                .disableAppInsights(config.getMonitorConfig().getApplicationInsightsConfig() == null)
                .appInsightsKey(Optional.ofNullable(config.getMonitorConfig().getApplicationInsightsConfig())
                        .map(ApplicationInsightsConfig::getInstrumentationKey).orElse(null))
                .appInsightsInstance(Optional.ofNullable(config.getMonitorConfig().getApplicationInsightsConfig())
                        .map(ApplicationInsightsConfig::getName).orElse(null))
                .subscriptionId(config.getSubscription().getId())
                .resourceGroup(config.getResourceGroup().getName())
                .appName(config.getName())
                .servicePlanName(config.getServicePlan().getName())
                .servicePlanResourceGroup(config.getServicePlan().getResourceGroup())
                .pricingTier(config.getPricingTier())
                .region(config.getRegion())
                .runtime(getRuntimeConfig(config))
                .appSettings(config.getAppSettings());
    }

    private RuntimeConfig getRuntimeConfig(final FunctionAppConfig config) {
        final Runtime runtime = config.getRuntime();
        return new RuntimeConfig().os(runtime.getOperatingSystem()).javaVersion(runtime.getJavaVersion()).webContainer(WebContainer.JAVA_OFF);
    }
}
