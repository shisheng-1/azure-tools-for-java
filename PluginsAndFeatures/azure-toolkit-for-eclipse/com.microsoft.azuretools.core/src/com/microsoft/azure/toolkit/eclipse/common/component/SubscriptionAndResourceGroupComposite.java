/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.eclipse.common.component.resourcegroup.ResourceGroupComboBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class SubscriptionAndResourceGroupComposite extends Composite {
    public static final int MINIMUM_LABEL_WIDTH = 95;
    private final SubscriptionComboBox cbSubs;
    private final ResourceGroupComboBox cbResourceGroup;

    public SubscriptionAndResourceGroupComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(2, false));

        Label lblNewLabel = new Label(this, SWT.NONE);
        lblNewLabel.setText("Subscription:");

        cbSubs = new SubscriptionComboBox(this);
        cbSubs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblResourceGroup = new Label(this, SWT.NONE);
        GridData gd_lblResourceGroup = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gd_lblResourceGroup.minimumWidth = MINIMUM_LABEL_WIDTH;
        gd_lblResourceGroup.widthHint = MINIMUM_LABEL_WIDTH;
        lblResourceGroup.setLayoutData(gd_lblResourceGroup);
        lblResourceGroup.setText("Resource Group:");

        cbResourceGroup = new ResourceGroupComboBox(this);
        cbResourceGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        cbSubs.addValueChangedListener(event -> {
            cbResourceGroup.setSubscription(cbSubs.getValue());
        });
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public SubscriptionComboBox getSubscriptionComboBox() {
        return cbSubs;
    }
}
