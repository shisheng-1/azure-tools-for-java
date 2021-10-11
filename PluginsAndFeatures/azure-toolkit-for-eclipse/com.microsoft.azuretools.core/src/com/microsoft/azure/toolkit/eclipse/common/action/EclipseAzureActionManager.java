package com.microsoft.azure.toolkit.eclipse.common.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.swt.widgets.Event;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.Action.Id;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;

public class EclipseAzureActionManager extends AzureActionManager {
    private static final String ACTIONS_CATEGORY = "com.microsoft.azure.toolkit.actions.category";
    private static final String EXTENSION_POINT_ID = "com.microsoft.azure.toolkit.actions";
    private static final Map<String, ActionGroup> groups = new HashMap<>();
    private static final ICommandService cmdService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
    private static final IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);

    public static void register() {
        final EclipseAzureActionManager am = new EclipseAzureActionManager();
        register(am);
        IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(EXTENSION_POINT_ID);
        for (IConfigurationElement element : configurationElements) {
            try {
                Object extension = element.createExecutableExtension("implementation");
                if (extension instanceof IActionsContributor) {
                    ((IActionsContributor) extension).registerActions(am);
                    ((IActionsContributor) extension).registerHandlers(am);
                    ((IActionsContributor) extension).registerGroups(am);
                }
            } catch (CoreException e) {
                // swallow exception during register
            }
        }
    }
    
    @Override
    public <D> Action<D> getAction(Id<D> actionId) {
        Command command = cmdService.getCommand(actionId.getId());
        if(!command.isDefined()) {
            return null;
        } 
        Event e = new Event();
        return new Action<>((D d, ExecutionEvent event) -> {
            try {
                handlerService.executeCommand(actionId.getId(), null);
            } catch (org.eclipse.core.commands.ExecutionException | NotDefinedException | NotEnabledException
                    | NotHandledException e) {
                e.printStackTrace();
            }
        }).authRequired(false);
    }

    @Override
    public ActionGroup getGroup(String id) {
        return groups.get(id);
    }

    @Override
    public <D> void registerAction(Id<D> actionId, Action<D> action) {
        Command command = cmdService.getCommand(actionId.getId());
        
        if(!command.isDefined()) {
            command.define(EXTENSION_POINT_ID, ACTIONS_CATEGORY, getActionCategory());
            command.define(actionId.getId(), actionId.getId(), getActionCategory());
            handlerService.activateHandler(actionId.getId() , new AbstractHandler() {
                @Override
                public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
                    action.handle((D)event.getObjectParameterForExecution(""), event);
                    return null;
                }
                
            });
        }else {
            AzureMessager.getMessager().warning("Command %s has been registered", actionId.getId());
        }
    }

    @Override
    public void registerGroup(String id, ActionGroup group) {
        groups.put(id, group);
    }

    public Category getActionCategory() {
        final Category category = cmdService.getCategory(ACTIONS_CATEGORY);
        if(!category.isDefined()) {
            category.define("AzureToolkitForEclipse", "Actions for Azure Toolkit for Eclipse");
        }
        return category;
    }
}
