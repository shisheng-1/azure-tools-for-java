package com.microsoft.azuretools.core.utils;

import com.microsoft.applicationinsights.core.dependencies.javaxannotation.Nonnull;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class EclipseAzureTaskManager extends AzureTaskManager {
    @Override
    protected void doRead(Runnable runnable, final AzureTask<?> task) {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    protected void doWrite(final Runnable runnable, final AzureTask<?> task) {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    protected void doRunLater(Runnable runnable, AzureTask<?> azureTask) {
        Display.getDefault().asyncExec(runnable);
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Override
    protected void doRunOnPooledThread(Runnable runnable, AzureTask<?> azureTask) {
        executorService.submit(runnable);
    }

    @Override
    protected void doRunAndWait(Runnable runnable, AzureTask<?> azureTask) {
        Display.getDefault().syncExec(runnable);
    }
    public static class AzureEclipseTask<T> extends AzureTask<T> {
        public AzureEclipseTask(@Nullable Object project, @Nonnull AzureString title, boolean cancellable, @Nonnull Supplier<T> supplier) {
            super(project, title, cancellable, supplier, Modality.DEFAULT);
        }
        @Setter
        @Getter
        IProgressMonitor monitor;
    }

    @Override
    protected void doRunInBackground(Runnable runnable, AzureTask<?> azureTask) {
        Job job = new Job((azureTask == null || azureTask.getTitle() == null) ? "Loading...": azureTask.getTitle().getString()) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(azureTask.getName(), IProgressMonitor.UNKNOWN);
                SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
                if (azureTask instanceof AzureEclipseTask) {

                }
                try {
                    runnable.run();
                } catch (Exception ex) {
                    monitor.done();
                    return Status.CANCEL_STATUS;
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    @Override
    protected void doRunInModal(Runnable runnable, AzureTask<?> azureTask) {

    }
}
