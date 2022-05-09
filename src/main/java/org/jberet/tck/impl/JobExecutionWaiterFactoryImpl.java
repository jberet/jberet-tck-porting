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

import jakarta.batch.operations.JobOperator;

import com.ibm.jbatch.tck.spi.JobExecutionWaiter;
import com.ibm.jbatch.tck.spi.JobExecutionWaiterFactory;

public final class JobExecutionWaiterFactoryImpl implements JobExecutionWaiterFactory {
    @Override
    public JobExecutionWaiter createWaiter(final long executionId, final JobOperator jobOp, final long sleepTime) {
        return new JobExecutionWaiterImpl(executionId, jobOp, sleepTime);
    }
}
