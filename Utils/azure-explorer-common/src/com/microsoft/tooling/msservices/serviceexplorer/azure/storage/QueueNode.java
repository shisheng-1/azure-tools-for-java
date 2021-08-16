/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.storage;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azuretools.telemetry.AppInsightsConstants;
import com.microsoft.azuretools.telemetry.TelemetryProperties;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.sdk.StorageClientSDKManager;
import com.microsoft.tooling.msservices.model.storage.Queue;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureNodeActionPromptListener;

import java.util.HashMap;
import java.util.Map;

public class QueueNode extends Node implements TelemetryProperties{
    @Override
    public Map<String, String> toProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put(AppInsightsConstants.SubscriptionId, ResourceId.fromString(this.storageAccount.id()).subscriptionId());
        properties.put(AppInsightsConstants.Region, this.storageAccount.regionName());
        return properties;
    }

    public class RefreshAction extends NodeActionListener {
        @Override
        public void actionPerformed(NodeActionEvent e) {
            DefaultLoader.getUIHelper().refreshQueue(getProject(), storageAccount, queue);
        }
    }

    public class ViewQueue extends NodeActionListener {
        @Override
        public void actionPerformed(NodeActionEvent e) {
            onNodeClick(e);
        }
    }

    public class DeleteQueue extends AzureNodeActionPromptListener {
        public DeleteQueue() {
            super(QueueNode.this,
                    String.format("Are you sure you want to delete the queue \"%s\"?", queue.getName()),
                    "Deleting Queue");
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e)
                throws AzureCmdException {
            Object openedFile = DefaultLoader.getUIHelper().getOpenedFile(getProject(), storageAccount.name(), queue);

            if (openedFile != null) {
                DefaultLoader.getIdeHelper().closeFile(getProject(), openedFile);
            }

            try {
                StorageClientSDKManager.getManager().deleteQueue(storageAccount, queue);

                parent.removeAllChildNodes();
                ((QueueModule) parent).load(false);
            } catch (AzureCmdException ex) {
                DefaultLoader.getUIHelper().showException("An error occurred while attempting to delete queue", ex,
                        "MS Services - Error Deleting Queue", false, true);
            }
        }

        @Override
        protected void onSubscriptionsChanged(NodeActionEvent e)
                throws AzureCmdException {
        }
    }

    public class ClearQueue extends AzureNodeActionPromptListener {
        public ClearQueue() {
            super(QueueNode.this,
                    String.format("Are you sure you want to clear the queue \"%s\"?", queue.getName()),
                    "Clearing Queue");
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e)
                throws AzureCmdException {
            try {
                StorageClientSDKManager.getManager().clearQueue(storageAccount, queue);

                DefaultLoader.getUIHelper().refreshQueue(getProject(), storageAccount, queue);
            } catch (AzureCmdException ex) {
                DefaultLoader.getUIHelper().showException("An error occurred while attempting to clear queue.", ex,
                        "MS Services - Error Clearing Queue", false, true);
            }
        }

        @Override
        protected void onSubscriptionsChanged(NodeActionEvent e)
                throws AzureCmdException {
        }
    }

    private static final String QUEUE_MODULE_ID = QueueNode.class.getName();
    private static final String ICON_PATH = "container.png";
    private final Queue queue;
    private final StorageAccount storageAccount;

    public QueueNode(QueueModule parent, StorageAccount storageAccount, Queue queue) {
        super(QUEUE_MODULE_ID, queue.getName(), parent, ICON_PATH, true);

        this.storageAccount = storageAccount;
        this.queue = queue;

        loadActions();
    }

    @Override
    protected void onNodeClick(NodeActionEvent ex) {
        final Object openedFile = DefaultLoader.getUIHelper().getOpenedFile(getProject(), storageAccount.name(), queue);

        if (openedFile == null) {
            DefaultLoader.getUIHelper().openItem(getProject(), storageAccount, queue, " [Queue]", "Queue", "container.png");
        } else {
            DefaultLoader.getUIHelper().openItem(getProject(), openedFile);
        }
    }

    @Override
    protected Map<String, Class<? extends NodeActionListener>> initActions() {
        return ImmutableMap.of(
                "Refresh", RefreshAction.class,
                "View Queue", ViewQueue.class,
                "Delete", DeleteQueue.class,
                "Clear Queue", ClearQueue.class
        );
    }
}