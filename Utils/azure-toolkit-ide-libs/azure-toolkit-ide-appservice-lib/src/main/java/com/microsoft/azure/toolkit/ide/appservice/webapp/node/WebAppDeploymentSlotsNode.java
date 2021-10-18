package com.microsoft.azure.toolkit.ide.appservice.webapp.node;

import com.microsoft.azure.toolkit.ide.appservice.webapp.WebAppActionsContributor;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.component.NodeView;
import com.microsoft.azure.toolkit.lib.appservice.service.IWebApp;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureBaseResource;
import com.microsoft.azure.toolkit.lib.common.event.AzureEvent;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class WebAppDeploymentSlotsNode extends Node<IWebApp> {
    private IWebApp webApp;
    private NodeView nodeView;

    public WebAppDeploymentSlotsNode(@Nonnull IWebApp data) {
        super(data);
        this.webApp = data;
        this.nodeView = new WebAppDeploymentSlotsNodeView(data);
    }

    @Nonnull
    @Override
    public NodeView view() {
        return this.nodeView;
    }

    @Override
    public List<Node<?>> getChildren() {
        return webApp.deploymentSlots().stream()
                .map(slot -> new Node<>(slot).view(new AzureResourceLabelView<>(slot)).actions(WebAppActionsContributor.DEPLOYMENT_SLOT_ACTIONS))
                .collect(Collectors.toList());
    }

    static class WebAppDeploymentSlotsNodeView implements NodeView {

        @Nonnull
        @Getter
        private final IWebApp webApp;
        private final AzureEventBus.EventListener<Object, AzureEvent<Object>> listener;

        @Nullable
        @Setter
        @Getter
        private Refresher refresher;

        public WebAppDeploymentSlotsNodeView(@Nonnull IWebApp webApp) {
            this.webApp = webApp;
            this.listener = new AzureEventBus.EventListener<>(this::onEvent);
            AzureEventBus.on("appservice|webapp.slot.refresh", listener);
            this.refreshView();
        }

        @Override
        public String getLabel() {
            return "Deployment Slots";
        }

        @Override
        public String getIconPath() {
            return "/icons/Slot_16.png";
        }

        @Override
        public String getDescription() {
            return "Deployment Slots";
        }

        @Override
        public void dispose() {
            AzureEventBus.off("appservice|webapp.slot.refresh", listener);
            this.refresher = null;
        }

        public void onEvent(AzureEvent<Object> event) {
            final String type = event.getType();
            final Object source = event.getSource();
            if (source instanceof IAzureBaseResource && ((IAzureBaseResource<?, ?>) source).id().equals(this.webApp.id())) {
                final AzureTaskManager tm = AzureTaskManager.getInstance();
                if ("appservice|webapp.slot.refresh".equals(type)) {
                    tm.runLater(this::refreshChildren);
                }
            }
        }
    }
}
