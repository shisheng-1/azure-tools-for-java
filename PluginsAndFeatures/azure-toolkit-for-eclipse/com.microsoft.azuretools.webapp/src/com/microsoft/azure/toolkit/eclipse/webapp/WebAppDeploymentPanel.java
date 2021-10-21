package com.microsoft.azure.toolkit.eclipse.webapp;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureArtifactComboBox;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;

public class WebAppDeploymentPanel extends Composite {

	private final Combo cbWebApp;
	private final AzureArtifactComboBox cbArtifact;
	private final Button btnCreateDeploySlot;
	private final Button btnUseExistingDeploySlot;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public WebAppDeploymentPanel(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label lblArtifact = new Label(this, SWT.NONE);
		GridData gd_lblArtifact = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblArtifact.widthHint = 140;
		lblArtifact.setLayoutData(gd_lblArtifact);
		lblArtifact.setText("Artifact:");

		cbArtifact = new AzureArtifactComboBox(this);
		cbArtifact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblWebApp = new Label(this, SWT.NONE);
		lblWebApp.setText("Web app:");

		cbWebApp = new Combo(this, SWT.NONE);
		cbWebApp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Group deploySlotGroup = new Group(this, SWT.NONE);
		deploySlotGroup.setText("Deployment slot:");
		GridLayout gl_deploySlotGroup = new GridLayout(2, false);
		gl_deploySlotGroup.marginHeight = 0;
		gl_deploySlotGroup.horizontalSpacing = 0;
		gl_deploySlotGroup.marginWidth = 0;
		gl_deploySlotGroup.verticalSpacing = 0;
		deploySlotGroup.setLayout(gl_deploySlotGroup);
		GridData gd_deploySlotGroup = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		gd_deploySlotGroup.widthHint = 162;
		deploySlotGroup.setLayoutData(gd_deploySlotGroup);

		Button btnDeployToSlot = new Button(deploySlotGroup, SWT.CHECK);
		btnDeployToSlot.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnDeployToSlot.setText("Deploy to slot");

		Composite composite = new Composite(deploySlotGroup, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);

		btnCreateDeploySlot = new Button(composite, SWT.RADIO);
		btnCreateDeploySlot.setSelection(true);
		btnCreateDeploySlot.setText("Create new");

		btnUseExistingDeploySlot = new Button(composite, SWT.RADIO);
		btnUseExistingDeploySlot.setText("Using existing slot");

		Label lblSlotName = new Label(deploySlotGroup, SWT.NONE);
		lblSlotName.setText("Slot name:");

		Combo combo_1 = new Combo(deploySlotGroup, SWT.NONE);
		combo_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblSlotConfigurationSource = new Label(deploySlotGroup, SWT.NONE);
		GridData gd_lblSlotConfigurationSource = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblSlotConfigurationSource.widthHint = 140;
		lblSlotConfigurationSource.setLayoutData(gd_lblSlotConfigurationSource);
		lblSlotConfigurationSource.setText("Slot configuration source:");

		Combo combo_2 = new Combo(deploySlotGroup, SWT.NONE);
		combo_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnBuildProject = new Button(this, SWT.CHECK);
		btnBuildProject.setSelection(true);
		GridData gd_btnBuildProject = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_btnBuildProject.widthHint = 429;
		btnBuildProject.setLayoutData(gd_btnBuildProject);
		btnBuildProject.setText("Build project");

		Button btnOpenBrowser = new Button(this, SWT.CHECK);
		btnOpenBrowser.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnOpenBrowser.setText("Open browser after deployment");
		btnOpenBrowser.setSelection(true);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
