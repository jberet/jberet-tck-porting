/*********************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *********************************************************************/

package org.jberet.tck.impl;

import java.util.concurrent.TimeUnit;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.operations.JobSecurityException;
import jakarta.batch.operations.NoSuchJobExecutionException;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.StepExecution;

import com.google.common.base.Throwables;
import com.ibm.jbatch.tck.spi.JobExecutionTimeoutException;
import com.ibm.jbatch.tck.spi.JobExecutionWaiter;
import org.jberet.runtime.JobExecutionImpl;
import org.jberet.runtime.StepExecutionImpl;

public final class JobExecutionWaiterImpl implements JobExecutionWaiter {
    private final JobExecutionImpl jobExecution;
    private final long sleepTime;

    JobExecutionWaiterImpl(final long executionId, final JobOperator jobOp, final long sleepTime) {
        try {
            this.jobExecution = (JobExecutionImpl) jobOp.getJobExecution(executionId);
            this.sleepTime = sleepTime;
        } catch (JobSecurityException e) {
            throw new IllegalStateException("Failed to create JobExecutionWaiterImpl.", e);
        } catch (NoSuchJobExecutionException e) {
            throw new IllegalStateException("Failed to create JobExecutionWaiterImpl.", e);
        }
    }

    @Override
    public JobExecution awaitTermination() throws JobExecutionTimeoutException {
        System.out.printf("Before awaitTermination for JobExecution %s, timeout %d%n", jobExecution, sleepTime);
        try {
            jobExecution.awaitTermination(sleepTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            //unexpected interrupt, ignore.
            e.printStackTrace();
        }
        System.out.printf("After awaitTermination for jobName %s, jobExecution %s, BatchStatus %s, exitStatus %s, StepExecutions %s%n",
                jobExecution.getJobName(), jobExecution.getExecutionId(), jobExecution.getBatchStatus(), jobExecution.getExitStatus(),
                jobExecution.getStepExecutions());

        for (final StepExecution e : jobExecution.getStepExecutions()) {
            final StepExecutionImpl e2 = (StepExecutionImpl) e;
            final Exception exception = e2.getException();
            final String exceptionString = exception == null ? null : Throwables.getStackTraceAsString(exception);
            System.out.printf("StepExecution %s, step name %s, batch status %s, exit status %s, exception %s%n",
                    e2, e2.getStepName(), e2.getBatchStatus(), e2.getExitStatus(), exceptionString);
        }

        return jobExecution;
    }
}
