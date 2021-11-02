/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.model;

import com.azure.core.management.AzureEnvironment;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public abstract class AppServiceConfig {
    @Builder.Default
    private MonitorConfig monitorConfig = MonitorConfig.builder().build();
    private String name;
    private String resourceId;
    private Path application;
    private Subscription subscription;
    private ResourceGroup resourceGroup;
    private AppServicePlanEntity servicePlan;
    private Region region;
    private PricingTier pricingTier;
    @Builder.Default
    private Map<String, String> appSettings = new HashMap<>();
    private DeploymentSlotConfig deploymentSlot;

    public Map<String, String> getTelemetryProperties() {
        final Map<String, String> result = new HashMap<>();
        result.put("subscriptionId", Optional.ofNullable(subscription).map(Subscription::getId).orElse(StringUtils.EMPTY));
        result.put("region", Optional.ofNullable(region).map(Region::getName).orElse(StringUtils.EMPTY));
        result.put("pricingTier", Optional.ofNullable(pricingTier).map(PricingTier::getSize).orElse(StringUtils.EMPTY));
        return result;
    }

    public static Region getDefaultRegion() {
        final AzureEnvironment environment = Azure.az(AzureAccount.class).account().getEnvironment();
        if (environment == AzureEnvironment.AZURE) {
            return Region.US_WEST;
        } else if (environment == AzureEnvironment.AZURE_CHINA) {
            return Region.CHINA_NORTH2;
        } else {
            return Azure.az(AzureAccount.class).listRegions().stream().findFirst().orElse(null);
        }
    }

    public String getResourceGroupName() {
        return Optional.ofNullable(resourceGroup).map(ResourceGroup::getName).orElse(StringUtils.EMPTY);
    }

    public String getSubscriptionId() {
        return Optional.ofNullable(subscription).map(Subscription::getId).orElse(StringUtils.EMPTY);
    }

    public abstract Runtime getRuntime();

    public abstract void setRuntime(Runtime runtime);

    public static boolean isSameApp(AppServiceConfig first, AppServiceConfig second) {
        if (Objects.isNull(first) || Objects.isNull(second)) {
            return first == second;
        }
        return StringUtils.equalsIgnoreCase(first.resourceId, second.resourceId) ||
                (StringUtils.equalsIgnoreCase(first.name, second.name) &&
                        StringUtils.equalsIgnoreCase(first.getResourceGroupName(), second.getResourceGroupName()) &&
                        StringUtils.equalsIgnoreCase(first.getSubscriptionId(), second.getSubscriptionId()));
    }
}
