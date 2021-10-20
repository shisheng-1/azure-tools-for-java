/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.webapp;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AzureWebAppDeployConfigurationTab extends AbstractLaunchConfigurationTab {

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public void createControl(Composite parent) {

        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        comp.setLayout(layout);
        comp.setLayoutData(gridData);
        comp.setFont(parent.getFont());
        Label lblArtifact = new Label(comp, SWT.NONE);
        GridData gd_lblArtifact = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gd_lblArtifact.widthHint = 76;
        lblArtifact.setLayoutData(gd_lblArtifact);
        lblArtifact.setText("Artifact:");
        GridDataFactory.swtDefaults().applyTo(lblArtifact);

        Combo combo = new Combo(comp, SWT.NONE);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

    }


    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {

    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {

    }

    @Override
    public String getName() {
        return "Deploy";
    }
}
