package com.microsoft.azure.toolkit.eclipse.appservice;

import com.microsoft.azure.toolkit.eclipse.appservice.platform.RuntimeComboBox;
import com.microsoft.azure.toolkit.eclipse.common.component.RegionComboBox;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

public class CreateWebAppInstanceDetailComposite extends Composite {
    private final RuntimeComboBox cbRuntime;
    private final RegionComboBox cbRegion;
    private Text text;
    private Label text_1;

    public RegionComboBox getRegionComboBox() {
        return cbRegion;
    }

    public RuntimeComboBox getRuntimeComboBox() {
        return cbRuntime;
    }

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public CreateWebAppInstanceDetailComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(3, false));

        Label lblNewLabel = new Label(this, SWT.NONE);
        GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblNewLabel.minimumWidth = 95;
        gd_lblNewLabel.widthHint = 95;
        lblNewLabel.setLayoutData(gd_lblNewLabel);
        lblNewLabel.setText("Name:");

        text = new Text(this, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        text_1 = new Label(this, SWT.NONE);
        text_1.setText(".azurewebsites.net");
        text_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

        Label lblPlatform = new Label(this, SWT.NONE);
        lblPlatform.setText("Platform:");

        cbRuntime = new RuntimeComboBox(this);
        cbRuntime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        Label lblRegion = new Label(this, SWT.NONE);
        lblRegion.setText("Region:");

        cbRegion = new RegionComboBox(this);
        cbRegion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
