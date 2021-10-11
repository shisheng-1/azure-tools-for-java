/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.common.utils.TailingDebouncer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AzureComboBox<T> extends ComboViewer implements AzureFormInput<T> {
    private static final int DEBOUNCE_DELAY = 500;
    private final TailingDebouncer refresher;
    @Getter
    @Setter
    private boolean required;
    private Object value;
    private boolean valueNotSet = true;
    protected boolean enabled = true;
    @Getter
    @Setter
    private Supplier<? extends List<? extends T>> itemsLoader;

    private AzureComboBox(Composite parent, int style, boolean refresh) {
        super(parent, style);
        this.init();
        this.refresher = new TailingDebouncer(this::doRefreshItems, DEBOUNCE_DELAY);
        if (refresh) {
            this.refreshItems();
        }
    }

    public AzureComboBox(Composite parent) {
        this(parent, SWT.DROP_DOWN, true);
    }

    public AzureComboBox(Composite parent, @Nonnull Supplier<? extends List<? extends T>> itemsLoader) {
        this(parent, SWT.DROP_DOWN, true);
        this.itemsLoader = itemsLoader;
    }

    public AzureComboBox(Composite parent, @Nonnull Supplier<? extends List<? extends T>> itemsLoader, boolean refresh) {
        this(parent, SWT.DROP_DOWN, refresh);
        this.itemsLoader = itemsLoader;
    }

    protected void init() {
        this.setEditable(true);
        this.toggleLoadingSpinner(false);
        super.setContentProvider(ArrayContentProvider.getInstance());
        super.setLabelProvider(new LabelProvider() {
            @Override
            public Image getImage(Object element) {
                return AzureComboBox.this.getItemIcon(element);
            }

            @Override
            public String getText(Object element) {
                return AzureComboBox.this.getItemText(element);
            }
        });
        this.addPostSelectionChangedListener((e) -> {
            if (!e.getSelection().isEmpty()) {
                this.refreshValue();
            }
        });
    }

    @Override
    public T getValue() {
        return ((T) super.getStructuredSelection().getFirstElement());
    }

    @Override
    public void setValue(final T val) {
        this.setValue(val, null);
    }

    public void setValue(final T val, final Boolean fixed) {
        Optional.ofNullable(fixed).ifPresent(f -> {
            this.setEnabled(!f);
            this.setEditable(!f);
        });
        this.valueNotSet = false;
        this.value = val;
        this.refreshValue();
    }

    public void setValue(final ItemReference<T> val) {
        this.setValue(val, null);
    }

    public void setValue(final ItemReference<T> val, final Boolean fixed) {
        Optional.ofNullable(fixed).ifPresent(f -> {
            this.setEnabled(!f);
            this.setEditable(!f);
        });
        this.valueNotSet = false;
        this.value = val;
        this.refreshValue();
    }

    private void refreshValue() {
        if (this.valueNotSet) {
            if (super.listGetItemCount() > 0 && super.listGetSelectionIndices()[0] != 0) {
                super.listSetSelection(new int[]{0});
            }
        } else {
            final Object selected = super.getStructuredSelection().getFirstElement();
            if (Objects.equals(selected, this.value) || (this.value instanceof ItemReference && ((ItemReference<?>) this.value).is(selected))) {
                return;
            }
            final List<T> items = this.getItems();
            if (this.value instanceof AzureComboBox.ItemReference) {
                items.stream().filter(i -> ((ItemReference<?>) this.value).is(i)).findFirst().ifPresent(this::setValue);
            } else if (items.contains(this.value)) {
                super.setSelection(new StructuredSelection(this.value));
            } else {
                super.setSelection(StructuredSelection.EMPTY);
            }
        }
    }

    public void refreshItems() {
        this.refresher.debounce();
    }

    @AzureOperation(
            name = "common|combobox.load_items",
            params = {"this.getLabel()"},
            type = AzureOperation.Type.ACTION
    )
    private void doRefreshItems() {
        this.setLoading(true);
        this.setItems(this.loadItemsInner());
        this.setLoading(false);
    }

    public List<T> getItems() {
        final List<T> result = new ArrayList<>();
        final Object input = super.getInput();
        if (input instanceof Collection) {
            result.addAll((Collection<T>) input);
        }
        return result;
    }

    protected synchronized void setItems(final List<? extends T> items) {
        AzureTaskManager.getInstance().runLater(() -> {
            super.setInput(items);
            this.refreshValue();
        });
    }

    public void clear() {
        this.value = null;
        this.valueNotSet = true;
        this.setInput(Collections.emptyList());
        this.refreshValue();
    }

    protected void setLoading(final boolean loading) {
        AzureTaskManager.getInstance().runLater(() -> {
            if (loading) {
                super.getControl().setEnabled(false);
                this.toggleLoadingSpinner(true);
            } else {
                super.getControl().setEnabled(this.enabled);
                this.toggleLoadingSpinner(false);
            }
            this.getControl().redraw();
        });
    }

    private void toggleLoadingSpinner(boolean b) {

    }

    private void setEditable(boolean b) {

    }

    public void setEnabled(boolean b) {
        this.enabled = b;
        super.getControl().setEnabled(b);
    }

    public boolean isEnabled() {
        return this.enabled || super.getControl().isEnabled();
    }

    protected String getItemText(Object item) {
        if (item == null) {
            return StringUtils.EMPTY;
        }
        return item.toString();
    }

    @Nullable
    protected Image getItemIcon(Object item) {
        return null;
    }

    @Nullable
    protected Control getExtension() {
        return null;
    }

    protected Mono<? extends List<? extends T>> loadItemsAsync() {
        return Mono.fromCallable(this::loadItemsInner).subscribeOn(Schedulers.boundedElastic());
    }

    protected final List<? extends T> loadItemsInner() {
        try {
            if (Objects.nonNull(this.itemsLoader)) {
                return this.itemsLoader.get();
            } else {
                return this.loadItems();
            }
        } catch (final Exception e) {
            final Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (!(rootCause instanceof InterruptedIOException) && !(rootCause instanceof InterruptedException)) {
                return Collections.emptyList();
            }
            this.handleLoadingError(e);
            return Collections.emptyList();
        }
    }

    @Nonnull
    protected List<? extends T> loadItems() {
        return Collections.emptyList();
    }

    @Nullable
    protected T getDefaultValue() {
        return null;
    }

    protected void handleLoadingError(Throwable e) {
        final Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause instanceof InterruptedIOException || rootCause instanceof InterruptedException) {
            // Swallow interrupted exception caused by unsubscribe
            return;
        }
        AzureMessager.getMessager().error(e);
    }

    public String getLabel() {
        return this.getClass().getSimpleName();
    }

    public static class ItemReference<T> {
        private final Predicate<? super T> predicate;

        public ItemReference(@Nonnull Predicate<? super T> predicate) {
            this.predicate = predicate;
        }

        public ItemReference(@Nonnull Object val, Function<T, ?> mapper) {
            this.predicate = t -> Objects.equals(val, mapper.apply(t));
        }

        public boolean is(Object obj) {
            if (Objects.isNull(obj)) {
                return false;
            }
            return this.predicate.test((T) obj);
        }
    }
}
