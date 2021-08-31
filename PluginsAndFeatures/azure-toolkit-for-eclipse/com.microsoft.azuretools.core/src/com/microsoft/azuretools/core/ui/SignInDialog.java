/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.ui;

import static com.microsoft.azuretools.telemetry.TelemetryConstants.ACCOUNT;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.AZURE_ENVIRONMENT;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.SIGNIN;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.SIGNIN_METHOD;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.microsoft.azure.hdinsight.sdk.rest.spark.Application;
import com.microsoft.azure.toolkit.lib.auth.core.devicecode.DeviceCodeAccount;
import com.microsoft.azure.toolkit.lib.auth.model.AccountEntity;
import com.microsoft.azure.toolkit.lib.auth.util.AzureEnvironmentUtils;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azuretools.adauth.IDeviceLoginUI;
import com.microsoft.azuretools.authmanage.AuthMethod;
import lombok.Lombok;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.auth.AzureCloud;
import com.microsoft.azure.toolkit.lib.auth.model.AuthConfiguration;
import com.microsoft.azure.toolkit.lib.auth.model.AuthType;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.authmanage.models.AuthMethodDetails;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.core.Activator;
import com.microsoft.azuretools.core.components.AzureTitleAreaDialogWrapper;
import com.microsoft.azuretools.core.utils.EclipseAzureTaskManager;
import com.microsoft.azuretools.sdkmanage.IdentityAzureManager;
import com.microsoft.azuretools.telemetrywrapper.ErrorType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;

import lombok.Getter;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import rx.Single;

public class SignInDialog extends AzureTitleAreaDialogWrapper {
    private static final String AZURE_SIGN_IN = "Azure Sign In";
    private static ILog LOG = Activator.getDefault().getLog();
    private Button btnAzureCli;
    private Button btnDeviceCode;
    private Button btnSPRadio;
    private Label lblAzureCli;
    private Label lblDeviceInfo;
    private Label lblSP;

    private AuthMethodDetails authMethodDetails;
    private String accountEmail;
    FileDialog fileDialog;

    private AuthConfiguration data = new AuthConfiguration();

    public AuthMethodDetails getAuthMethodDetails() {
        return authMethodDetails;
    }

    /**
     * Create the dialog.
     * @param parentShell
     */
    public SignInDialog(Shell parentShell) {
        super(parentShell);
        setHelpAvailable(false);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    public static SignInDialog go(Shell parentShell, AuthMethodDetails authMethodDetails) {
        SignInDialog d = new SignInDialog(parentShell);
        d.authMethodDetails = authMethodDetails;
        d.create();
        if (d.open() == Window.OK) {
            return d;
        }
        return null;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        Button okButton = getButton(IDialogConstants.OK_ID);
        okButton.setText("Sign in");
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage(AZURE_SIGN_IN);
        setTitle(AZURE_SIGN_IN);
        getShell().setText(AZURE_SIGN_IN);
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Group group = new Group(composite, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        group.setText("Authentication Method");
        group.setLayout(new GridLayout(1, false));

        btnAzureCli = createRadioButton(group, "Azure CLI", AuthType.AZURE_CLI);
        lblAzureCli = createDescriptionLabel(group, "Consume your existing Azure CLI credential..");

        btnDeviceCode = createRadioButton(group, "Device Login", AuthType.DEVICE_CODE);
        lblDeviceInfo = createDescriptionLabel(group, "You will need to open an external browser and sign in with a generated device code.");

        btnSPRadio = createRadioButton(group, "Service Principal", AuthType.SERVICE_PRINCIPAL);

        lblSP = createDescriptionLabel(group, "Use Azure Active Directory service principal for sign in.");

        return area;
    }

    private Button createRadioButton(Composite parent, String label, AuthType type) {
        final Button radioButton = new Button(parent, SWT.RADIO);
        radioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (radioButton.getSelection()) {
                    // Set the radio button to be focused or the default one will be selected when refresh
                    // For issue https://github.com/microsoft/azure-tools-for-java/issues/3543
                    radioButton.setFocus();
                    data.setType(type);
                    syncControlControls();
                }
            }
        });
        radioButton.setText(label);
        return radioButton;
    }

    private Label createDescriptionLabel(Composite parent, String description) {
        Composite compositeDevice = new Composite(parent, SWT.NONE);
        GridData gdCompositeDevice = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdCompositeDevice.heightHint = 38;
        gdCompositeDevice.widthHint = 66;
        compositeDevice.setLayoutData(gdCompositeDevice);
        compositeDevice.setLayout(new GridLayout(1, false));
        Label label = new Label(compositeDevice, SWT.WRAP);
        GridData gdLblDeviceInfo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdLblDeviceInfo.horizontalIndent = 11;
        label.setLayoutData(gdLblDeviceInfo);
        label.setText(description);
        return label;
        //
    }

    private void syncControlControls() {
        setErrorMessage(null);
        AuthType type = data.getType();
        lblDeviceInfo.setEnabled(type == AuthType.DEVICE_CODE);
        lblAzureCli.setEnabled(type == AuthType.AZURE_CLI);
        boolean spLoginSelected = type == AuthType.SERVICE_PRINCIPAL;
        lblSP.setEnabled(spLoginSelected);
    }

    @SneakyThrows
    @Override
    public void okPressed() {
       
        AuthConfiguration auth = new AuthConfiguration();
        if (btnAzureCli.getSelection()) {
            auth.setType(AuthType.AZURE_CLI);

        } else if (btnDeviceCode.getSelection()) {
            auth.setType(AuthType.DEVICE_CODE);
            super.okPressed();
            doDeviceCodeLogin();
            return;

        } else if (btnSPRadio.getSelection()) {
            auth.setType(AuthType.SERVICE_PRINCIPAL);
            ServicePrincipalLoginDialog servicePrincipalLoginDialog = new ServicePrincipalLoginDialog(this.getShell());
            if (servicePrincipalLoginDialog.open() == Window.CANCEL) {
                return;
            }
            auth = servicePrincipalLoginDialog.getModel();
            System.out.println();
        }

        IRunnableWithProgress op = new YourThread(10, auth);

        try {
            new ProgressMonitorDialog(new Shell()).run(true, true, op);
        } catch (InvocationTargetException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        this.authMethodDetails = authMethodDetailsResult;

        super.okPressed();
    }

    private void doDeviceCodeLogin() {
        DeviceCodeAccount account = loginDeviceCodeSingle();
        final IDeviceLoginUI deviceLoginUI = new DeviceLoginWindow();
        new Thread(()  -> {
        	CompletableFuture<AuthMethodDetails> future =
                    account.continueLogin()
                            .subscribeOn(Schedulers.boundedElastic())
                    .map(ac -> {
                    	System.out.println("got entity");
                    	return fromAccountEntity(ac.getEntity());	
                    })
                    .doFinally(signal -> {
                    	deviceLoginUI.closePrompt();
                    	System.out.println("doFinally");
                    })
                    .toFuture();
            deviceLoginUI.setFuture(future);
        }).start();
        deviceLoginUI.promptDeviceCode(account.getDeviceCode());
        
        
    }

    private static AuthMethodDetails fromAccountEntity(AccountEntity entity) {
        final AuthMethodDetails authMethodDetails = new AuthMethodDetails();
        authMethodDetails.setAuthMethod(AuthMethod.IDENTITY);
        authMethodDetails.setAuthType(entity.getType());
        authMethodDetails.setClientId(entity.getClientId());
        authMethodDetails.setTenantId(CollectionUtils.isEmpty(entity.getTenantIds()) ? "" : entity.getTenantIds().get(0));
        authMethodDetails.setAzureEnv(AzureEnvironmentUtils.getCloudNameForAzureCli(entity.getEnvironment()));
        authMethodDetails.setAccountEmail(entity.getEmail());
        return authMethodDetails;
    }


    private DeviceCodeAccount loginDeviceCodeSingle() {
        final AzureAccount az = Azure.az(AzureAccount.class);
        CompletableFuture<DeviceCodeAccount> result = new CompletableFuture<>();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor indicator) throws InvocationTargetException, InterruptedException {
                DeviceCodeAccount ac = (DeviceCodeAccount) checkCanceled(indicator, az.loginAsync(AuthType.DEVICE_CODE, true), () -> {
                    result.cancel(true);
                    throw Lombok.sneakyThrow(new InterruptedException("user cancel"));
                });
                result.complete(ac);
            }
        };

        try {
            new ProgressMonitorDialog(this.getShell()).run(true, true, op);
        } catch (InvocationTargetException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            return result.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

	private class YourThread implements IRunnableWithProgress {
        private int workload;

        private AuthConfiguration auth;
        @Getter
        private AuthMethodDetails authMethodDetailsResult;

        public YourThread(int workload, AuthConfiguration auth) {
            this.workload = workload;
            this.auth = auth;
        }

        @Override
        public void run(IProgressMonitor indicator) throws InvocationTargetException, InterruptedException {
            // Tell the user what you are doing
            indicator.beginTask("Sign in", IProgressMonitor.UNKNOWN);


            authMethodDetailsResult = new AuthMethodDetails();
            switch (auth.getType()) {
                case SERVICE_PRINCIPAL:
                    authMethodDetailsResult = call(() -> checkCanceled(indicator, IdentityAzureManager.getInstance().signInServicePrincipal(auth),
                            AuthMethodDetails::new), "sp");
                    break;
                case AZURE_CLI:
                    authMethodDetailsResult = call(() -> checkCanceled(indicator, IdentityAzureManager.getInstance().signInAzureCli(),
                            AuthMethodDetails::new), "az");
                    break;
                case OAUTH2:
                    authMethodDetailsResult = call(() -> checkCanceled(indicator, IdentityAzureManager.getInstance().signInOAuth(),
                            AuthMethodDetails::new), "oauth");
                    break;
                default:
                    break;
            }
            indicator.done();
        }

    }


    private static Single<AuthMethodDetails> loginNonDeviceCodeSingle(AuthConfiguration auth) {
        final AzureString title = AzureOperationBundle.title("account.sign_in");
        final EclipseAzureTaskManager.AzureEclipseTask<AuthMethodDetails> task = new EclipseAzureTaskManager.AzureEclipseTask<AuthMethodDetails>(null, title, true, () -> {
//            final IProgressMonitor indicator = task.getMonitor();
//            indicator.setIndeterminate(true);
//            return doLogin(indicator, auth);
            return new AuthMethodDetails();
        });
        return AzureTaskManager.getInstance().runInBackgroundAsObservable(task).toSingle();
    }

    private static <T> T call(Callable<T> loginCallable, String authMethod) {
        final Operation operation = TelemetryManager.createOperation(ACCOUNT, SIGNIN);
        final Map<String, String> properties = new HashMap<>();
        properties.put(SIGNIN_METHOD, authMethod);

        try {
            operation.start();
            operation.trackProperties(properties);
            operation.trackProperty(AZURE_ENVIRONMENT, Azure.az(AzureCloud.class).getName());
            return loginCallable.call();
        } catch (Exception e) {
            if (shouldNoticeErrorToUser(e)) {
                EventUtil.logError(operation, ErrorType.userError, e, properties, null);
            }
            throw new AzureToolkitRuntimeException(e.getMessage(), e);
        } finally {
            operation.complete();
        }
    }

    private static <T> T checkCanceled(IProgressMonitor indicator, Mono<? extends T> mono, Supplier<T> supplier) {
        final Mono<T> cancelMono = Flux.interval(Duration.ofSeconds(1)).map(ignore -> indicator.isCanceled())
                .any(cancel -> cancel).map(ignore -> supplier.get()).subscribeOn(Schedulers.boundedElastic());
        return Mono.firstWithSignal(cancelMono, mono.subscribeOn(Schedulers.boundedElastic())).block();
    }

    private static boolean shouldNoticeErrorToUser(Throwable cause) {
        if (cause instanceof InterruptedException) {
            return false;
        }
        if (cause instanceof MsalClientException && StringUtils.equals(cause.getMessage(), "No Authorization code was returned from the server")) {
            return false;
        }
        return true;
    }


    @Nullable
    private synchronized void doSignIn() {
        try {
//            final BaseADAuthManager dcAuthManager = AuthMethod.DC.getAdAuthManager();
//
//            if (dcAuthManager.isSignedIn()) {
//                doSignOut();
//            }
//            signInAsync(dcAuthManager);
//            accountEmail = dcAuthManager.getAccountEmail();
//
//            return dcAuthManager;
        } catch (Exception ex) {
            System.out.println("doSignIn@SingInDialog: " + ex.getMessage());
            ex.printStackTrace();
            LOG.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "doSignIn@SingInDialog", ex));
        }

        return;
    }

    private void signInAsync() throws InvocationTargetException, InterruptedException {
        Operation operation = TelemetryManager.createOperation(ACCOUNT, SIGNIN);
        IRunnableWithProgress op = (monitor) -> {
            operation.start();
            monitor.beginTask("Signing In...", IProgressMonitor.UNKNOWN);
//            try {
//                EventUtil.logEvent(EventType.info, operation, signInDCProp, null);
//                dcAuthManager.signIn(null);
//            } catch (AuthCanceledException ex) {
//                EventUtil.logError(operation, ErrorType.userError, ex, signInDCProp, null);
//                System.out.println(ex.getMessage());
//            } catch (Exception ex) {
//                EventUtil.logError(operation, ErrorType.userError, ex, signInDCProp, null);
//                System.out.println("run@ProgressDialog@signInAsync@SingInDialog: " + ex.getMessage());
//                Display.getDefault().asyncExec(() -> ErrorWindow.go(getShell(), ex.getMessage(), "Sign In Error"));
//            } finally {
//                operation.complete();
//            }
        };
        new ProgressMonitorDialog(this.getShell()).run(true, true, op);
    }

    private void doSignOut() {
        accountEmail = null;
        // AuthMethod.AD is deprecated.

    }
}
