package com.microsoft.azure.toolkit.eclipse.springcloud;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureDialog;
import com.microsoft.azure.toolkit.lib.springcloud.config.SpringCloudAppConfig;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import com.microsoft.azure.toolkit.eclipse.common.form.AzureForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
public class DeploySpringCloudDialog extends AzureDialog<SpringCloudAppConfig> {

    private SpringCloudDeploymentConfigurationPanel panel;

    public DeploySpringCloudDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected String getDialogTitle() {
        return "Deploy to Azure Spring Cloud";
    }


    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        panel = new SpringCloudDeploymentConfigurationPanel(container);
        panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        return container;
    }

    @Override
    public AzureForm<SpringCloudAppConfig> getForm() {
        return panel;
    }

}
