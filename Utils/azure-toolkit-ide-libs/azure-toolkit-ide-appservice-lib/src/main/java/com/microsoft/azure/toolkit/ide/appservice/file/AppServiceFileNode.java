/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.file;

import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.lib.appservice.model.AppServiceFile;
import org.jetbrains.annotations.NotNull;

public class AppServiceFileNode extends Node<AppServiceFile> {
    public AppServiceFileNode(@NotNull AppServiceFile data) {
        super(data);
    }
}
