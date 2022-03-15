
/*******************************************************************************
 * Copyright (c) 2007, 2009 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/

package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIOutput;
import gdb.mi.service.command.output.MIStackInfoDepthInfo;

/**
 *
 *     -stack-info-depth [maxDepth]
 *
 *
 */
public class MIStackInfoDepth extends MICommand<MIStackInfoDepthInfo> {

	public MIStackInfoDepth(String threadId) {
		super("-stack-info-depth", new String[] {"--thread", threadId}); //$NON-NLS-1$
	}

	public MIStackInfoDepth(String threadId, int maxDepth) {
		super("-stack-info-depth", new String[] {"--thread", threadId, Integer.toString(maxDepth) }); //$NON-NLS-1$
	}

	@Override
	public MIStackInfoDepthInfo getResult(MIOutput out) {
		return new MIStackInfoDepthInfo(out);
	}
}
