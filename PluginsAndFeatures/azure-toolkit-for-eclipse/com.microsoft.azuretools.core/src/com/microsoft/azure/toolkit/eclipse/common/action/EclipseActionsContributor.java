/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
 
package com.microsoft.azure.toolkit.eclipse.common.action;

import java.util.Objects;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import  com.microsoft.azuretools.core.utils.PluginUtil;

public class EclipseActionsContributor implements IActionsContributor {

    @Override
    public int getOrder() {
        // TODO Auto-generated method stub
        return 2; //after azure resource common actions registered
    }

    @Override
    public void registerHandlers(AzureActionManager am) {
        // TODO Auto-generated method stub
        am.<String>registerHandler(ResourceCommonActionsContributor.OPEN_URL, Objects::nonNull, PluginUtil::openLinkInBrowser);
    }

}
