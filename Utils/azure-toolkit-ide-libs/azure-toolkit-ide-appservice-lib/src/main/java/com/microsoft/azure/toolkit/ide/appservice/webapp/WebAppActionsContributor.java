/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.webapp;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.appservice.service.IWebApp;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;

public class WebAppActionsContributor implements IActionsContributor {
    public static final String SERVICE_ACTIONS = "actions.webapp.service";
    public static final String WEBAPP_ACTIONS = "actions.webapp.management";
    public static final String DEPLOYMENT_SLOT_ACTIONS = "actions.webapp.management";

    public static final Action.Id<IWebApp> SWAP_DEPLOYMENT_SLOT = Action.Id.of("action.webapp.slot.swap");

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.SERVICE_REFRESH,
                ResourceCommonActionsContributor.CREATE
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup webAppActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                ResourceCommonActionsContributor.OPEN_URL,
                ResourceCommonActionsContributor.DEPLOY,
                ResourceCommonActionsContributor.SHOW_PROPERTIES,
                "---",
                ResourceCommonActionsContributor.START,
                ResourceCommonActionsContributor.STOP,
                ResourceCommonActionsContributor.RESTART,
                ResourceCommonActionsContributor.DELETE
                // todo: add profile actions like log streaming
        );
        am.registerGroup(WEBAPP_ACTIONS, webAppActionGroup);

        final ActionGroup deploymentSlotActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                ResourceCommonActionsContributor.OPEN_URL,
                ResourceCommonActionsContributor.SHOW_PROPERTIES,
                SWAP_DEPLOYMENT_SLOT,
                "---",
                ResourceCommonActionsContributor.START,
                ResourceCommonActionsContributor.STOP,
                ResourceCommonActionsContributor.RESTART,
                ResourceCommonActionsContributor.DELETE
                // todo: add profile actions like log streaming
        );
        am.registerGroup(DEPLOYMENT_SLOT_ACTIONS, deploymentSlotActionGroup);
    }
}
