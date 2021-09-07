/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.store;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IPropertyStore {
    @Nullable
    String getProperty(@Nullable String serviceName, @Nonnull String key);

    @Nullable
    String getProperty(@Nullable String serviceName, @Nonnull String key, @Nullable String defaultValue);

    void setProperty(@Nullable String serviceName, @Nonnull String key, @Nullable String value);

    void load();
}
