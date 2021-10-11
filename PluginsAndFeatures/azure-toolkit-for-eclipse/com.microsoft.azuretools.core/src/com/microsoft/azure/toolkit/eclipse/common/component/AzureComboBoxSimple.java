/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.form.Validatable;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.swt.widgets.Composite;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

@Setter
@Getter
public class AzureComboBoxSimple<T> extends AzureComboBox<T> {

    private Validatable.Validator validator;

    public AzureComboBoxSimple(@Nonnull Composite parent, @Nonnull final Supplier<? extends List<? extends T>> supplier) {
        super(parent, supplier, true);
    }
}
