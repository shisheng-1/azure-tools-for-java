package com.microsoft.azure.toolkit.intellij.vm.creation;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.microsoft.azure.toolkit.intellij.common.AzureDialog;
import com.microsoft.azure.toolkit.intellij.common.component.AzureFileInput;
import com.microsoft.azure.toolkit.intellij.common.component.RegionComboBox;
import com.microsoft.azure.toolkit.intellij.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.intellij.common.component.resourcegroup.ResourceGroupComboBox;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VMCreationDialog extends AzureDialog<Object> {
    public static final String SSH_PUBLIC_KEY_DESCRIPTION = "<html> Provide an RSA public key file in the single-line format (starting with \"ssh-rsa\") or " +
            "the multi-line PEM format. <p/> You can generate SSH keys using ssh-keygen on Linux and OS X, or PuTTYGen on Windows. </html>";
    private JTabbedPane tabbedPane;
    private JPanel rootPane;
    private JPanel basicPane;
    private JLabel lblResourceGroup;
    private JLabel lblSubscription;
    private JLabel lblVirtualMachineName;
    private JTextField txtVisualMachineName;
    private JLabel lblRegion;
    private JRadioButton rdoSshPublicKey;
    private JRadioButton rdoPassword;
    private JTextField txtUserName;
    private JComboBox cbVirtualNetwork;
    private JRadioButton rdoNoneSecurityGroup;
    private JRadioButton rdoBasicSecurityGroup;
    private JRadioButton rdoAdvancedSecurityGroup;
    private JRadioButton rdoNoneInboundPorts;
    private JRadioButton rdoAllowSelectedInboundPorts;
    private JCheckBox chkAzureSpotInstance;
    private JRadioButton rdoCapacityOnly;
    private JRadioButton rdoPriceOrCapacity;
    private JRadioButton rdoStopAndDeallocate;
    private JRadioButton rdoDelete;
    private JTextField txtMaximumPrice;
    private JLabel lblUserName;
    private JTextField txtPassword;
    private JTextField txtConfirmPassword;
    private JLabel lblPassword;
    private JLabel lblConfirmPassword;
    private JLabel lblCertificate;
    private AzureFileInput txtCertificate;
    private JLabel lblPublicInboundPorts;
    private JComboBox cbSelectInboundPorts;
    private JLabel lblSelectInboundPorts;
    private JLabel lblConfigureSecurityGroup;
    private JComboBox cbSecurityGroup;
    private JLabel lblEvictionType;
    private JLabel lblEvictionPolicy;
    private JLabel lblMaximumPrice;
    private JLabel lblAvailabilityOptions;
    private JLabel lblImage;
    private JLabel lblSize;
    private JLabel lblAuthenticationType;
    private JLabel lblVirtualNetwork;
    private JLabel lblSubnet;
    private JLabel lblPublicIP;
    private JLabel lblSecurityGroup;
    private JLabel lblStorageAccount;
    private JLabel lblAzureSportInstance;
    private JPanel pnlSecurityRadios;
    private JPanel pnlPublicInboundsRadios;
    private JComboBox cbAvailabilityOptions;
    private JComboBox cbImage;
    private JComboBox cbSize;
    private JComboBox cbSubnet;
    private JComboBox cbPublicIP;
    private JComboBox cbStorageAccount;
    private SubscriptionComboBox cbSubscription;
    private ResourceGroupComboBox cbResourceGroup;
    private RegionComboBox cbRegion;

    private Project project;

    public VMCreationDialog(@Nullable Project project) {
        super(project);
        this.project = project;

        $$$setupUI$$$();

        init();
    }

    @Override
    protected void init() {
        super.init();

        final ButtonGroup authenticationGroup = new ButtonGroup();
        authenticationGroup.add(rdoPassword);
        authenticationGroup.add(rdoSshPublicKey);
        rdoPassword.addItemListener(e -> toggleAuthenticationType(false));
        rdoSshPublicKey.addItemListener(e -> toggleAuthenticationType(true));
        rdoSshPublicKey.setSelected(true);

        final ButtonGroup securityGroup = new ButtonGroup();
        securityGroup.add(rdoNoneSecurityGroup);
        securityGroup.add(rdoBasicSecurityGroup);
        securityGroup.add(rdoAdvancedSecurityGroup);
        rdoNoneSecurityGroup.addItemListener(e -> toggleSecurityGroup(SecurityGroupPolicy.None));
        rdoBasicSecurityGroup.addItemListener(e -> toggleSecurityGroup(SecurityGroupPolicy.Basic));
        rdoAdvancedSecurityGroup.addItemListener(e -> toggleSecurityGroup(SecurityGroupPolicy.Advanced));
        rdoNoneSecurityGroup.setSelected(true);

        final ButtonGroup inboundPortsGroup = new ButtonGroup();
        inboundPortsGroup.add(rdoNoneInboundPorts);
        inboundPortsGroup.add(rdoAllowSelectedInboundPorts);
        rdoNoneInboundPorts.addItemListener(e -> cbSelectInboundPorts.setEditable(false));
        rdoAllowSelectedInboundPorts.addItemListener(e -> cbSelectInboundPorts.setEditable(true));
        rdoNoneInboundPorts.setSelected(true);

        chkAzureSpotInstance.addItemListener(e -> toggleAzureSpotInstance(chkAzureSpotInstance.isSelected()));
        chkAzureSpotInstance.setSelected(false);

        final ButtonGroup evictionTypeGroup = new ButtonGroup();
        evictionTypeGroup.add(rdoCapacityOnly);
        evictionTypeGroup.add(rdoPriceOrCapacity);

        final ButtonGroup evictionPolicyGroup = new ButtonGroup();
        evictionPolicyGroup.add(rdoStopAndDeallocate);
        evictionPolicyGroup.add(rdoDelete);

        cbSubscription.addItemListener(e -> onSubscriptionChanged(e));
        txtCertificate.addActionListener(new ComponentWithBrowseButton.BrowseFolderActionListener("Select cert for your VM", SSH_PUBLIC_KEY_DESCRIPTION, txtCertificate, project, FileChooserDescriptorFactory.createSingleLocalFileDescriptor(), TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT));

        unifyComponentsStyle();
    }

    private void onSubscriptionChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Subscription) {
            final Subscription subscription = (Subscription) e.getItem();
            this.cbResourceGroup.setSubscription(subscription);
            this.cbRegion.setSubscription(subscription);
        }
    }

    private void unifyComponentsStyle() {
        final List<JLabel> collect = Arrays.stream(this.getClass().getDeclaredFields())
                .map(field -> {
                    try {
                        return field.get(this);
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .filter(value -> value instanceof JLabel)
                .map(value -> (JLabel) value)
                .collect(Collectors.toList());
        final int maxWidth = collect.stream().map(JLabel::getPreferredSize).map(Dimension::getWidth).max(Double::compare).map(Double::intValue).get();
        final int maxHeight = collect.stream().map(JLabel::getPreferredSize).map(Dimension::getHeight).max(Double::compare).map(Double::intValue).get();
        collect.forEach(field -> {
            Dimension dimension = new Dimension(maxWidth, Math.max(maxHeight, cbSecurityGroup.getPreferredSize().height));
            field.setPreferredSize(dimension);
            field.setMinimumSize(dimension);
            field.setMaximumSize(dimension);
        });
    }

    private void toggleAzureSpotInstance(boolean enableAzureSpotInstance) {
        lblEvictionType.setVisible(enableAzureSpotInstance);
        rdoCapacityOnly.setVisible(enableAzureSpotInstance);
        rdoPriceOrCapacity.setVisible(enableAzureSpotInstance);
        lblEvictionPolicy.setVisible(enableAzureSpotInstance);
        rdoStopAndDeallocate.setVisible(enableAzureSpotInstance);
        rdoDelete.setVisible(enableAzureSpotInstance);
        lblMaximumPrice.setVisible(enableAzureSpotInstance);
        txtMaximumPrice.setVisible(enableAzureSpotInstance);
    }

    private void toggleSecurityGroup(SecurityGroupPolicy policy) {
        lblPublicInboundPorts.setVisible(policy == SecurityGroupPolicy.Basic);
        pnlPublicInboundsRadios.setVisible(policy == SecurityGroupPolicy.Basic);
        rdoNoneInboundPorts.setVisible(policy == SecurityGroupPolicy.Basic);
        rdoAllowSelectedInboundPorts.setVisible(policy == SecurityGroupPolicy.Basic);
        lblSelectInboundPorts.setVisible(policy == SecurityGroupPolicy.Basic);
        cbSelectInboundPorts.setVisible(policy == SecurityGroupPolicy.Basic);
        lblConfigureSecurityGroup.setVisible(policy == SecurityGroupPolicy.Advanced);
        cbSecurityGroup.setVisible(policy == SecurityGroupPolicy.Advanced);
    }

    private void toggleAuthenticationType(boolean isSSH) {
        lblPassword.setVisible(!isSSH);
        txtPassword.setVisible(!isSSH);
        lblConfirmPassword.setVisible(!isSSH);
        txtConfirmPassword.setVisible(!isSSH);
        lblCertificate.setVisible(isSSH);
        txtCertificate.setVisible(isSSH);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        this.cbSubscription = new SubscriptionComboBox();
        this.cbSubscription.refreshItems();
    }

    @Override
    public AzureForm<Object> getForm() {
        return new AzureForm<Object>() {
            @Override
            public Object getData() {
                return null;
            }

            @Override
            public void setData(Object data) {

            }

            @Override
            public List<AzureFormInput<?>> getInputs() {
                return Collections.emptyList();
            }
        };
    }

    @Override
    protected String getDialogTitle() {
        return "Create Virtual Machine";
    }

    enum SecurityGroupPolicy {
        None,
        Basic,
        Advanced
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return rootPane;
    }

    private void $$$setupUI$$$() {
    }
}
