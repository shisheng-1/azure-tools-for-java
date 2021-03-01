/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import com.microsoft.azure.toolkit.intellij.link.LinkMySQLToModuleDialog;
import com.microsoft.azure.toolkit.intellij.link.base.LinkType;
import com.microsoft.azure.toolkit.intellij.link.po.LinkPO;
import com.microsoft.intellij.AzureLinkStorage;
import com.microsoft.intellij.helpers.AzureIconLoader;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class MySQLCompletionContributor extends CompletionContributor {

    public MySQLCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        resultSet.addElement(LookupElementBuilder
                                .create("spring.datasource.url")
                                .withIcon(AzureIconLoader.loadIcon(AzureIconSymbol.MySQL.BIND_INTO))
                                .withInsertHandler(new MyInsertHandler())
                                .withBoldness(true)
                                .withTypeText("String")
                                .withTailText(" (Link Azure Datasource for MySQL Service)")
                                .withAutoCompletionPolicy(AutoCompletionPolicy.SETTINGS_DEPENDENT)
                        );
                    }
                }
        );

    }

    private class MyInsertHandler implements InsertHandler<LookupElement> {

        @Override
        public void handleInsert(@NotNull InsertionContext insertionContext, @NotNull LookupElement lookupElement) {
            Module module = ModuleUtil.findModuleForFile(insertionContext.getFile().getVirtualFile(), insertionContext.getProject());
            List<LinkPO> moduleLinkerList = AzureLinkStorage.getProjectStorage(insertionContext.getProject()).getLinkersByModuleId(module.getName())
                    .stream()
                    .filter(e -> LinkType.SERVICE_WITH_MODULE.equals(e.getType()))
                    .collect(Collectors.toList());
            boolean insertRequired = true;
            if (CollectionUtils.isEmpty(moduleLinkerList)) {
                final LinkMySQLToModuleDialog dialog = new LinkMySQLToModuleDialog(insertionContext.getProject(), null, module);
                insertRequired = dialog.showAndGet();
            }
            if (insertRequired) {
                EditorModificationUtil.insertStringAtCaret(insertionContext.getEditor(), "=${AZURE_MYSQL_URL}" + StringUtils.LF +
                        "spring.datasource.username=${AZURE_MYSQL_USERNAME}" + StringUtils.LF +
                        "spring.datasource.password=${AZURE_MYSQL_PASSWORD}" + StringUtils.LF, true);
            } else {
                EditorModificationUtil.insertStringAtCaret(insertionContext.getEditor(), "=", true);
            }
        }
    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        super.beforeCompletion(context);
    }

    @Override
    public void duringCompletion(@NotNull CompletionInitializationContext context) {
        super.duringCompletion(context);
    }

}
