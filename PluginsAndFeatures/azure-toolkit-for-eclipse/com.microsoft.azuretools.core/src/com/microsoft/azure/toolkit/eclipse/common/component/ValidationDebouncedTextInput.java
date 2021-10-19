/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.utils.Debouncer;
import com.microsoft.azure.toolkit.lib.common.utils.TailingDebouncer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Composite;

public class ValidationDebouncedTextInput extends AzureTextInput {
    protected static final int DEBOUNCE_DELAY = 500;
    protected AzureValidationInfo validationInfo;
    private final Debouncer validator;

    public ValidationDebouncedTextInput(Composite parent) {
        super(parent);
        this.validator = new TailingDebouncer(() -> this.validationInfo = this.doValidateValue(), DEBOUNCE_DELAY);
    }

    protected AzureValidationInfo doValidateValue() {
        return super.doValidate();
    }

    @Override
    public AzureValidationInfo doValidate() {
        AzureValidationInfo info = this.validationInfo;
        if (this.validator.isPending()) {
            info = AzureValidationInfo.PENDING;
        } else if (this.validationInfo == null) {
            this.validationInfo = this.doValidateValue();
            info = this.validationInfo;
        }
        this.setExtension(info);
        return info;
    }

    private void setExtension(AzureValidationInfo info) {

    }

    @Override
    public void modifyText(ModifyEvent modifyEvent) {
        this.validator.debounce();
    }
}