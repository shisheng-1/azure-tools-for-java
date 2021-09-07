/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.intellij;

import com.microsoft.azure.toolkit.ide.common.store.IFileStore;
import com.microsoft.azuretools.azurecommons.util.ParserXMLUtility;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileStore implements IFileStore {
    private String dataFile;
    private Map<String, String> map = new HashMap<>();

    public FileStore(String dataFile) {
        this.dataFile = dataFile;
        if (Files.exists(Paths.get(dataFile))) {
            try {
                load();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    @Nullable
    public String getProperty(@javax.annotation.Nullable String service, @Nonnull String key) {
        return map.get(combineKey(service, key));
    }

    @Nullable
    public String getProperty(@javax.annotation.Nullable String service, @Nonnull String key, @Nullable String defaultValue) {
        return StringUtils.firstNonBlank(map.get(combineKey(service, key)), defaultValue);
    }

    public void setProperty(@javax.annotation.Nullable String service, @Nonnull String key, @Nullable String value) {
        map.put(combineKey(service, key), value);
        save();
    }

    private static String combineKey(String service, String key) {
        return StringUtils.isBlank(service) ? key : String.format("%s.%s", service, key);
    }

    public void load() {
        try {
            final Document doc = ParserXMLUtility.parseXMLFile(dataFile);
            if (doc != null) {
                final String expression = "/data/property[@name and @value]";
                final XPath xPath = XPathFactory.newInstance().newXPath();
                final NodeList list = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
                for (int i = 0; i < list.getLength(); i++) {
                    final Node name = list.item(i).getAttributes().getNamedItem("name");
                    final Node value = list.item(i).getAttributes().getNamedItem("value");
                    String keyText = name.getTextContent();
                    String valueText = value.getTextContent();
                    if (StringUtils.isNoneBlank(keyText, valueText)) {
                        map.put(keyText, valueText);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void save() {
        try {
            Document doc = new File(dataFile).exists() ? ParserXMLUtility.parseXMLFile(dataFile) : createNewDoc();
            map.forEach((k, v) -> {
                updatePropertyValue(doc, k, v);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updatePropertyValue(Document doc, String propertyName, String value) {
        try {
            String nodeExpr = String.format("/data/property[@name='%s']", propertyName);
            final Map<String, String> nodeAttributes = new HashMap();
            nodeAttributes.put("name", propertyName);
            nodeAttributes.put("value", value);
            if (!ParserXMLUtility.doesNodeExists(doc, "/data")) {
                Element data = doc.createElement("data");
                doc.appendChild(data);
            }
            ParserXMLUtility.updateOrCreateElement(doc, nodeExpr, "/data", "property", true, nodeAttributes);
            ParserXMLUtility.saveXMLFile(dataFile, doc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Document createNewDoc() throws ParserConfigurationException {
        final DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        return documentBuilder.newDocument();
    }
}
