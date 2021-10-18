package com.microsoft.azure.toolkit.eclipse.appservice.serviceplan;

import com.microsoft.azure.toolkit.eclipse.common.component.Draft;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;

import javax.annotation.Nonnull;

public class DraftServicePlan extends AppServicePlanEntity implements Draft {
    public DraftServicePlan(@Nonnull String name,
                            @Nonnull PricingTier pricingTier) {
        super(builder().name(name).pricingTier(pricingTier));
    }
}
