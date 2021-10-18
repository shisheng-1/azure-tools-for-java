package com.microsoft.azure.toolkit.eclipse.common.component.resourcegroup;

import com.microsoft.azure.toolkit.eclipse.common.component.Draft;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;

public class DraftResourceGroup extends ResourceGroup implements Draft {

    public DraftResourceGroup(String name) {
        super(builder().name(name));
    }
}
