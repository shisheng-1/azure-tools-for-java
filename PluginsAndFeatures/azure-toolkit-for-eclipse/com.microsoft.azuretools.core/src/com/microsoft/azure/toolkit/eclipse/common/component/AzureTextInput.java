/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.form.Validatable;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class AzureTextInput extends Text implements AzureFormInputControl<String>, ModifyListener {
    @Getter
    @Setter
    private boolean required;
    @Getter
    @Setter
    private Validatable.Validator validator;

    public AzureTextInput(Composite parent) {
        super(parent, SWT.NONE);
        this.addModifyListener(this);
    }

    @Override
    public String getValue() {
        return this.getText();
    }

    @Override
    public void setValue(final String val) {
        this.setText(val);
    }

    @Override
    public void modifyText(ModifyEvent modifyEvent) {

    }

    @Override
    public Control getInputControl() {
        return this;
    }
}
