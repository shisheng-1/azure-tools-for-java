package com.microsoft.azure.toolkit.eclipse.appservice;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureDialog;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureFormInputControl;
import com.microsoft.azure.toolkit.eclipse.common.component.SubscriptionAndResourceGroupComposite;
import com.microsoft.azure.toolkit.eclipse.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.lib.appservice.config.AppServiceConfig;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class CreateWebAppDialog extends AzureDialog<AppServiceConfig> implements AzureForm<AppServiceConfig> {
    private CreateWebAppInstanceDetailComposite instanceDetailPanel;
    private SubscriptionAndResourceGroupComposite subsAndResourceGroupPanel;
    private CreateWebAppAppServicePlanComposite appServicePlanPanel;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public CreateWebAppDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.SHELL_TRIM);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) container.getLayout();

        Group grpProjectDetails = new Group(container, SWT.NONE);
        grpProjectDetails.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpProjectDetails.setText("Project Details");
        grpProjectDetails.setLayout(new FillLayout(SWT.HORIZONTAL));

        subsAndResourceGroupPanel = new SubscriptionAndResourceGroupComposite(grpProjectDetails, SWT.NONE);

        Group grpInstanceDetails = new Group(container, SWT.NONE);
        grpInstanceDetails.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpInstanceDetails.setText("Instance Details");
        grpInstanceDetails.setLayout(new FillLayout(SWT.HORIZONTAL));
        instanceDetailPanel = new CreateWebAppInstanceDetailComposite(grpInstanceDetails, SWT.NONE);

        Group grpAppServicePlan = new Group(container, SWT.NONE);
        grpAppServicePlan.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpAppServicePlan.setText("App Service Plan");
        grpAppServicePlan.setLayout(new FillLayout(SWT.HORIZONTAL));

        appServicePlanPanel = new CreateWebAppAppServicePlanComposite(grpAppServicePlan, SWT.NONE);

        SubscriptionComboBox subscriptionComboBox = subsAndResourceGroupPanel.getSubscriptionComboBox();

        subscriptionComboBox.addValueChangedListener(event -> {
            Subscription subs = subscriptionComboBox.getValue();
            instanceDetailPanel.getRegionComboBox().setSubscription(subs);
            appServicePlanPanel.getServicePlanCombobox().setSubscription(subs);
        });
        instanceDetailPanel.getRegionComboBox().addValueChangedListener(event -> {
            Region region = instanceDetailPanel.getRegionComboBox().getValue();
            appServicePlanPanel.getServicePlanCombobox().setAzureRegion(region);
        });

        instanceDetailPanel.getRuntimeComboBox().addValueChangedListener(event -> {
            Runtime r = instanceDetailPanel.getRuntimeComboBox().getValue();
            if (r != null) {
                appServicePlanPanel.getServicePlanCombobox().setOs(r.getOperatingSystem());
            }

        });
        return container;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(548, 408);
    }

    @Override
    protected void configureShell(Shell newShell) {
        newShell.setMinimumSize(new Point(400, 380));
        super.configureShell(newShell);
        newShell.setText("Create Azure Web App");
    }

    @Override
    public AppServiceConfig getData() {
        final AppServicePlanEntity entity = appServicePlanPanel.getServicePlan();
        return new AppServiceConfig()
                .subscriptionId(subsAndResourceGroupPanel.getSubscription().getId())
                .resourceGroup(subsAndResourceGroupPanel.getResourceGroup().getName())
                .appName(instanceDetailPanel.getAppName())
                .region(instanceDetailPanel.getResourceRegion())
                .runtime(instanceDetailPanel.getRuntime())
                .servicePlanName(entity.getName())
                .servicePlanResourceGroup(StringUtils.firstNonBlank(entity.getResourceGroup(), subsAndResourceGroupPanel.getResourceGroup().getName()))
                .pricingTier(entity.getPricingTier());
    }

    @Override
    public void setData(AppServiceConfig config) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    protected String getDialogTitle() {
        // TODO Auto-generated method stub
        return "Create Azure Web App";
    }

    @Override
    public AzureForm<AppServiceConfig> getForm() {
        // TODO Auto-generated method stub
        return this;
    }
}
