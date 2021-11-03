/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common;

import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;

public interface IActionsContributor {
    int DEFAULT_ORDER = 0;

    default void registerActions(AzureActionManager am) {
    }

    default void registerGroups(AzureActionManager am) {
    }

    default void registerHandlers(AzureActionManager am) {
    }

    int getOrder();
}
