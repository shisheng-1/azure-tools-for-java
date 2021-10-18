/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.webapp;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.appservice.model.AppServiceFile;
import com.microsoft.azure.toolkit.lib.appservice.service.IWebApp;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureBaseResource;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class WebAppActionsContributor implements IActionsContributor {
    public static final String SERVICE_ACTIONS = "actions.webapp.service";
    public static final String WEBAPP_ACTIONS = "actions.webapp.management";
    public static final String DEPLOYMENT_SLOTS_ACTIONS = "actions.webapp.deployments";
    public static final String DEPLOYMENT_SLOT_ACTIONS = "actions.webapp.deployments.slot";

    public static final Action.Id<IWebApp> SWAP_DEPLOYMENT_SLOT = Action.Id.of("action.webapp.slot.swap");
    public static final Action.Id<IWebApp> REFRESH_DEPLOYMENT_SLOTS = Action.Id.of("actions.webapp.deployments.refresh");

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
        );
        am.registerGroup(DEPLOYMENT_SLOT_ACTIONS, deploymentSlotActionGroup);

        am.registerGroup(DEPLOYMENT_SLOTS_ACTIONS, new ActionGroup(REFRESH_DEPLOYMENT_SLOTS));
    }

    @Override
    public void registerActions(AzureActionManager am) {
        final Consumer<IWebApp> refresh = file -> AzureEventBus.emit("appservice|webapp.slot.refresh", file);
        final ActionView.Builder refreshView = new ActionView.Builder("Refresh", "/icons/action/refresh.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("appservice|webapp.slot.refresh", ((AppServiceFile) r).getName())).orElse(null))
                .enabled(s -> s instanceof IWebApp);
        am.registerAction(REFRESH_DEPLOYMENT_SLOTS, new Action<>(refresh, refreshView));
    }

    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<IAzureBaseResource<?, ?>, AnActionEvent> startCondition = (r, e) -> r instanceof VirtualMachine &&
                StringUtils.equals(r.status(), IAzureBaseResource.Status.STOPPED);
        final BiConsumer<IAzureBaseResource<?, ?>, AnActionEvent> startHandler = (c, e) -> ((VirtualMachine) c).start();
        am.registerHandler(ResourceCommonActionsContributor.START, startCondition, startHandler);

        final BiPredicate<IAzureBaseResource<?, ?>, AnActionEvent> stopCondition = (r, e) -> r instanceof VirtualMachine &&
                StringUtils.equals(r.status(), IAzureBaseResource.Status.RUNNING);
        final BiConsumer<IAzureBaseResource<?, ?>, AnActionEvent> stopHandler = (c, e) -> ((VirtualMachine) c).stop();
        am.registerHandler(ResourceCommonActionsContributor.STOP, stopCondition, stopHandler);

        final BiPredicate<IAzureBaseResource<?, ?>, AnActionEvent> restartCondition = (r, e) -> r instanceof VirtualMachine &&
                StringUtils.equals(r.status(), IAzureBaseResource.Status.RUNNING);
        final BiConsumer<IAzureBaseResource<?, ?>, AnActionEvent> restartHandler = (c, e) -> ((VirtualMachine) c).restart();
        am.registerHandler(ResourceCommonActionsContributor.RESTART, restartCondition, restartHandler);
    }
}
