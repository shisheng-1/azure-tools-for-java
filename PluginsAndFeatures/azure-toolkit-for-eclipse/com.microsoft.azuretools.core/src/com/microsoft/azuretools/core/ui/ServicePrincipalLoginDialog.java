package com.microsoft.azuretools.core.ui;

import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.toolkit.lib.auth.model.AuthConfiguration;
import com.microsoft.azure.toolkit.lib.auth.model.AuthType;
import com.microsoft.azure.toolkit.lib.common.utils.Debouncer;
import com.microsoft.azure.toolkit.lib.common.utils.TailingDebouncer;
import com.microsoft.azuretools.utils.JsonUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ServicePrincipalLoginDialog extends Dialog {
    private static final String GUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"; //UUID v1-v5
    private static final Pattern GUID_PATTERN = Pattern.compile(GUID_REGEX, Pattern.CASE_INSENSITIVE);
    private Text txtTenantId;
    private Text txtClientId;
    private ControlDecoration decoratorErrorName;
    private Text txtPassword;
    private Text txtCertificate;
    private Text txtJson;
    private Button radioPassword;
    private Button radioCertificate;
    protected static final int DEBOUNCE_DELAY = 500;

    @Override
    public void okPressed() {
        model =  getData();
        super.okPressed();
    }

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public ServicePrincipalLoginDialog(Shell parentShell) {
        super(parentShell);
        this.validator = new TailingDebouncer(() -> {
            json2UIComponents();
        }, DEBOUNCE_DELAY);

        this.validator2 = new TailingDebouncer(() -> {

            Display.getDefault().syncExec(() -> {
                AuthConfiguration newData = getData();
                if (!equalsData(newData, model)) {
                    model = newData;
                    uiTextComponents2Json();
                }

            });
        }, DEBOUNCE_DELAY);


    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) container.getLayout();
        gridLayout.marginRight = 10;
        gridLayout.marginLeft = 10;
        gridLayout.numColumns = 2;

        Label lblNewLabel = new Label(container, SWT.NONE);
        lblNewLabel.setText("Tenant Id: ");

        txtTenantId = new Text(container, SWT.BORDER);
        txtTenantId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblClientId = new Label(container, SWT.NONE);
        lblClientId.setText("Client Id: ");

        txtClientId = new Text(container, SWT.BORDER);
        txtClientId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));


        decoratorErrorName = new ControlDecoration(txtClientId, SWT.CENTER);
        decoratorErrorName.setDescriptionText("Ttest");

        Label lblSecret = new Label(container, SWT.NONE);
        lblSecret.setText("Secret: ");

        Group composite = new Group(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        GridLayout gl_composite = new GridLayout(2, false);
        gl_composite.marginTop = 1;
        gl_composite.marginRight = 10;
        gl_composite.marginLeft = 10;
        gl_composite.marginWidth = 0;
        composite.setLayout(gl_composite);

        radioPassword = new Button(composite, SWT.RADIO);
        radioPassword.setSelection(true);
        radioPassword.setText("Password:");

        txtPassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        radioCertificate = new Button(composite, SWT.RADIO);
        radioCertificate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        radioCertificate.setBounds(0, 0, 90, 16);
        radioCertificate.setText("Certificate:");

        Composite composite_cert = new Composite(composite, SWT.NONE);
        composite_cert.setLayout(new FormLayout());
        GridData gd_composite_cert = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_composite_cert.widthHint = 191;
        composite_cert.setLayoutData(gd_composite_cert);

        txtCertificate = new Text(composite_cert, SWT.BORDER | SWT.READ_ONLY);
        FormData fd_txtCertificate = new FormData();
        fd_txtCertificate.top = new FormAttachment(0, 3);
        fd_txtCertificate.left = new FormAttachment(0, 0);
        txtCertificate.setLayoutData(fd_txtCertificate);

        Button btnOpenFileButton = new Button(composite_cert, SWT.CENTER);
        fd_txtCertificate.right = new FormAttachment(btnOpenFileButton, -6);
        FormData fd_btnOpenFileButton = new FormData();
        fd_btnOpenFileButton.top = new FormAttachment(0, 1);
        fd_btnOpenFileButton.right = new FormAttachment(100);
        btnOpenFileButton.setLayoutData(fd_btnOpenFileButton);
        btnOpenFileButton.setText("...");
        btnOpenFileButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                FileDialog dlg = new FileDialog(btnOpenFileButton.getShell(), SWT.OPEN);
                dlg.setText("Open");
                String path = dlg.open();
                if (path == null) return;
                txtCertificate.setText(path);
            }
        });
        btnOpenFileButton.setEnabled(false);


        Label lblJson = new Label(container, SWT.NONE);
        lblJson.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
        lblJson.setText("JSON:");

        Composite composite_1 = new Composite(container, SWT.NONE);
        composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));

        txtJson = new Text(composite_1, SWT.MULTI | SWT.BORDER);
        txtJson.setLayoutData(new GridData(GridData.FILL_BOTH));
        FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);

        if (fieldDecoration != null) {
            Image image = fieldDecoration.getImage();
            decoratorErrorName.setImage(image);
        }
        txtJson.addModifyListener(event -> {
            this.validator.debounce();
        });

        radioCertificate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                btnOpenFileButton.setEnabled(true);
            }
        });

        radioPassword.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                btnOpenFileButton.setEnabled(false);
            }
        });


        Stream.of(txtCertificate
                , txtClientId, txtPassword, txtTenantId).forEach(d -> {
            d.addModifyListener(r -> {
                validator2.debounce();

            });
        });
        Stream.of(radioCertificate,
                radioPassword).forEach(d -> {
                    d.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            super.widgetSelected(e);
                            validator2.debounce();
                        }
                    });
        });

        return container;
    }

    private void uiTextComponents2Json() {
    	if (txtJson.isDisposed()) {
    		return;
    	}
        try {
            Map<String, String> map = new LinkedHashMap<>();
            if (model.getCertificate() != null) {
                map.put("fileWithCertAndPrivateKey", model.getCertificate());
            } else {
                String password = StringUtils.isNotBlank(model.getKey()) ? "<hidden>" : "<empty>";
                map.put("password", password);
            }
            map.put("appId", model.getClient());
            map.put("tenant", model.getTenant());
            String text = JsonUtils.getGson().toJson(map);

            if (!StringUtils.equals(
                    txtJson.getText().replaceAll("\\s", ""), text.replaceAll("\\s", ""))) {
                txtJson.setText(text);
            }

        } finally {
            intermediateState.set(false);
        }
    }

    private static boolean equalsData(AuthConfiguration config1, AuthConfiguration config2) {
        if (config1 == config2) {
            return true;
        }
        if (config1 == null) {
            config1 = new AuthConfiguration();
        }
        if (config2 == null) {
            config2 = new AuthConfiguration();
        }
        return StringUtils.equals(config1.getClient(), config2.getClient()) &&
                StringUtils.equals(config1.getTenant(), config2.getTenant()) &&
                StringUtils.equals(config1.getCertificate(), config2.getCertificate()) &&
                StringUtils.equals(config1.getKey(), config2.getKey()) &&
                StringUtils.equals(config1.getCertificatePassword(), config2.getCertificatePassword());

    }

    private final Debouncer validator;
    private final Debouncer validator2;

    
    private AuthConfiguration model;
    

    public AuthConfiguration getModel() {
		return model;
	}

	public void setModel(AuthConfiguration model) {
		this.model = model;
	}

	public AuthConfiguration getData() {
        AuthConfiguration data = new AuthConfiguration();

        data.setClient(txtClientId.getText());
        data.setTenant(txtTenantId.getText());
        if (radioPassword.getSelection()) {
            data.setKey(String.valueOf(txtPassword.getText()));
        } else {
            data.setCertificate(this.txtCertificate.getText());
        }
        data.setType(AuthType.SERVICE_PRINCIPAL);
        return data;
    }


    @Override
    protected Point getInitialSize() {
        return new Point(454, 379);
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        button.setText("Sign In");
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    protected void configureShell(Shell shell) {
        shell.setMinimumSize(new Point(420, 339));
        super.configureShell(shell);
        shell.setText("Sign In - Service Principal");
    }

    //
    //	/**
    //	 * Return the initial size of the dialog.
    //	 */
    //	@Override
    //	protected Point getInitialSize() {
    //		return new Point(450, 300);
    //	}
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        new ServicePrincipalLoginDialog(shell).open();
    }

    private AtomicBoolean intermediateState = new AtomicBoolean(false);

    private void json2UIComponents() {


        Display.getDefault().syncExec(() -> {
        	if (txtJson.isDisposed()) {
        		return;
        	}

            try {
//                if (!intermediateState.compareAndSet(false, true)) {
//                    return;
//                }
                String json = this.txtJson.getText();
                Map<String, String> map = JsonUtils.fromJson(json, HashMap.class);
                AuthConfiguration newData = new AuthConfiguration();
                if (map.containsKey("appId")) {
                    newData.setClient(StringUtils.defaultString(map.get("appId")));
                }

                if (map.containsKey("tenant")) {
                    newData.setTenant(StringUtils.defaultString(map.get("tenant")));
                }

                if (map.containsKey("password")) {
                    newData.setKey(isPlaceHolder(map.get("password")) ? this.txtPassword.getText() : map.get("password"));
                }

                if (map.containsKey("fileWithCertAndPrivateKey")) {
                    newData.setCertificate(StringUtils.defaultString(map.get("fileWithCertAndPrivateKey")));
                }
                setData(newData);
            } catch (JsonSyntaxException ex) {
                // ignore all json errors
            } finally {
                intermediateState.set(false);
            }
        });


    }

    public void setData(AuthConfiguration newData) {
        if (equalsData(newData, model)) {
            return;
        }
        this.model = newData;
        this.txtTenantId.setText(StringUtils.defaultString(newData.getTenant()));
        this.txtClientId.setText(StringUtils.defaultString(newData.getClient()));

        if (!StringUtils.isAllBlank(model.getCertificate(), model.getKey())) {
            if (model.getKey() != null) {
                this.txtPassword.setText(model.getKey());
                this.radioPassword.setSelection(true);
                this.radioCertificate.setSelection(false);
            } else {
                this.txtCertificate.setText(model.getCertificate());
                this.radioPassword.setSelection(false);
                this.radioCertificate.setSelection(true);
            }
        }
    }

    private static boolean isPlaceHolder(String password) {
        return Arrays.asList("<hidden>", "<empty>").contains(password);
    }

    private static boolean isGuid(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return GUID_PATTERN.matcher(str).matches();
    }
}