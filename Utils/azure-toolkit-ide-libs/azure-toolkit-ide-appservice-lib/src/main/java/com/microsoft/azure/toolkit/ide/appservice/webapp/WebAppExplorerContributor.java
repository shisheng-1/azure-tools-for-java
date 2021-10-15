/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.webapp;

import com.microsoft.azure.toolkit.ide.appservice.file.AppServiceFileNode;
import com.microsoft.azure.toolkit.ide.common.IExplorerContributor;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.AzureServiceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.component.NodeView;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureWebApp;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class WebAppExplorerContributor implements IExplorerContributor {
    private static final String NAME = "Web Apps";
    private static final String ICON = "/icons/WebApp_16.png";

    @Override
    public Node<?> getModuleNode() {
        final AzureWebApp service = Azure.az(AzureWebApp.class);
        return new Node<>(service).view(new AzureServiceLabelView<>(service, NAME, ICON))
                .actions(WebAppActionsContributor.SERVICE_ACTIONS)
                .addChildren(AzureWebApp::list, (webApp, webAppModule) -> new Node<>(webApp)
                        .view(new AzureResourceLabelView<>(webApp))
                        .actions(WebAppActionsContributor.WEBAPP_ACTIONS)
                        .addChildren(app -> Arrays.asList(app), (app, webAppNode) -> new Node<>(app.id())
                                .view(new NodeView(){

                                    @Override
                                    public void dispose() {

                                    }

                                    @Override
                                    public void setRefresher(Refresher refresher) {

                                    }

                                    @Nullable
                                    @Override
                                    public Refresher getRefresher() {
                                        return null;
                                    }

                                    @Override
                                    public String getLabel() {
                                        return "Deployment Slots";
                                    }

                                    @Override
                                    public String getIconPath() {
                                        return null;
                                    }

                                    @Override
                                    public String getDescription() {
                                        return getLabel();
                                    }
                                })
                                .addChildren(id -> Azure.az(AzureWebApp.class).get(id).deploymentSlots(), (slot, slotsNode) -> new Node<>(slot)
                                        .view(new AzureResourceLabelView<>(slot)))
                                .actions(WebAppActionsContributor.DEPLOYMENT_SLOT_ACTIONS)) // Deployment Slots
                        .addChildren(app -> Arrays.asList(AppServiceFileNode.getRootFileNodeForAppService(app)),
                                (file, webAppNode) -> new AppServiceFileNode(file)) // Files
                        .addChildren(app -> Arrays.asList(AppServiceFileNode.getRootLogNodeForAppService(app)),
                                (file, webAppNode) -> new AppServiceFileNode(file)) // Logs
                );
    }
}
