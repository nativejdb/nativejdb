/*******************************************************************************
 * Copyright (c) 2000, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     ENEA Software AB - CLI command extension - fix for bug 190277
 *     Ericsson - Implementation for DSF-GDB
 *     Anna Dushistova (Mentor Graphics) - [318322] Add set solib-absolute-prefix
 *     Vladimir Prus (CodeSourcery) - Support for -data-read-memory-bytes (bug 322658)
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *     Abeer Bagul - Support for -exec-arguments (bug 337687)
 *     Marc Khouzam (Ericsson) - New methods for new MIDataDisassemble (Bug 357073)
 *     Marc Khouzam (Ericsson) - New method for new MIGDBSetPythonPrintStack (Bug 367788)
 *     Mathias Kunter - New methods for handling different charsets (Bug 370462)
 *     Anton Gorenkov - A preference to use RTTI for variable types determination (Bug 377536)
 *     Vladimir Prus (Mentor Graphics) - Support for -info-os (Bug 360314)
 *     John Dallaway - Support for -data-write-memory-bytes (Bug 387793)
 *     Alvaro Sanchez-Leon (Ericsson) - Make Registers View specific to a frame (Bug (323552)
 *     Philippe Gil (AdaCore) - Add show/set language CLI commands (Bug 421541)
 *     Dmitry Kozlov (Mentor Graphics) - New trace-related methods (Bug 390827)
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *     Marc Khouzam (Ericsson) - Support for dynamic printf (Bug 400638)
 *     Marc Khouzam (Ericsson) - Support for -gdb-version (Bug 455408)
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *     Samuel Hultgren (STMicroelectronics) - Bug 533771
 *
 * Copyright (C) 2022 IBM Corporation
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/


package gdb.mi.service.command;

import gdb.mi.service.command.commands.*;
import gdb.mi.service.command.output.*;

/**
 * Factory to create MI/CLI commands for NativeJDB POC. More to be added as use cases increase
 *
 */

public class CommandFactory {

	public MICommand<MIInfo> createMIBreakAfter(String breakpoint, int ignoreCount) {
		return new MIBreakAfter(breakpoint, ignoreCount);
	}

	public MICommand<MIInfo> createMIBreakCommands(String breakpoint,
												   String[] commands) {
		return new MIBreakCommands(breakpoint, commands);
	}

	public MICommand<MIInfo> createMIBreakCondition(String breakpoint,
													String condition) {
		return new MIBreakCondition(breakpoint, condition);
	}

	public MICommand<MIInfo> createMIBreakDelete(String... array) {
		return new MIBreakDelete(array);
	}

	public MICommand<MIInfo> createMIBreakDisable(String... array) {
		return new MIBreakDisable(array);
	}

	public MICommand<MIInfo> createMIBreakEnable(String... array) {
		return new MIBreakEnable(array);
	}

	public MICommand<MIBreakInsertInfo> createMIBreakInsert(String func) {
		return new MIBreakInsert(func, false);
	}

	public MICommand<MIBreakInsertInfo> createMIBreakInsert(boolean isTemporary,
															boolean isHardware, String condition, int ignoreCount, String line, String tid) {
		return new MIBreakInsert(isTemporary, isHardware, condition, ignoreCount, line, tid, false);
	}

	public MICommand<MIBreakInsertInfo> createMIBreakInsert(boolean isTemporary,
															boolean isHardware, String condition, int ignoreCount, String location, String tid, boolean disabled,
															boolean isTracepoint) {
		return new MIBreakInsert(isTemporary, isHardware, condition, ignoreCount, location, tid, disabled,
				isTracepoint, false);
	}

	public MICommand<MIBreakListInfo> createMIBreakList() {
		return new MIBreakList();
	}

	public MICommand<MIBreakInsertInfo> createMIBreakWatch(boolean isRead,
														   boolean isWrite, String expression) {
		return new MIBreakWatch(isRead, isWrite, expression);
	}

	public MICommand<MIInfo> createMIExecContinue() {
		return new MIExecContinue();
	}

	public MICommand<MIInfo> createMIExecContinue(boolean allThreads) {
		return new MIExecContinue(allThreads);
	}

	public MICommand<MIInfo> createMIExecContinue(String groupId) {
		return new MIExecContinue(groupId);
	}

	public MICommand<MIInfo> createMIExecInterrupt() {
		return new MIExecInterrupt();
	}

	public MICommand<MIInfo> createMIExecInterrupt(boolean allThreads) {
		return new MIExecInterrupt(allThreads);
	}

	public MICommand<MIInfo> createMIExecInterrupt(String groupId) {
		return new MIExecInterrupt(groupId);
	}

	public MICommand<MIInfo> createMIExecJump(String location) {
		return new MIExecJump(location);
	}

	public MICommand<MIInfo> createMIExecNext() {
		return new MIExecNext();
	}

	public MICommand<MIInfo> createMIExecNext(int count) {
		return new MIExecNext(count);
	}

	public MICommand<MIInfo> createMIExecNextInstruction() {
		return new MIExecNextInstruction();
	}

	public MICommand<MIInfo> createMIExecNextInstruction(int count) {
		return new MIExecNextInstruction(count);
	}

	public MICommand<MIInfo> createMIExecReturn() {
		return new MIExecReturn();
	}

	public MICommand<MIInfo> createMIExecReturn(String arg) {
		return new MIExecReturn(arg);
	}

	public MICommand<MIInfo> createMIExecRun() {
		return new MIExecRun();
	}

	public MICommand<MIInfo> createMIExecRun(String[] args) {
		return new MIExecRun(args);
	}

	public MICommand<MIInfo> createMIExecStep() {
		return new MIExecStep();
	}

	public MICommand<MIInfo> createMIExecStep(int count) {
		return new MIExecStep(count);
	}

	public MICommand<MIInfo> createMIExecStepInstruction() {
		return new MIExecStepInstruction();
	}

	public MICommand<MIInfo> createMIExecStepInstruction(int count) {
		return new MIExecStepInstruction(count);
	}

	public MICommand<MIInfo> createMIGDBExit() {
		return new MIGDBExit();
	}

	public MICommand<MIGDBVersionInfo> createMIGDBVersion() {
		return new MIGDBVersion();
	}

	public MICommand<MIThreadInfoInfo> createMIThreadInfo() { return new MIThreadInfo(); }

	public MICommand<MIInfo> createMISelectThread(int threadNum) { return new MIThreadSelect(threadNum); }

	public MICommand<MIStackInfoDepthInfo> createMIStackInfoDepth(String threadId) { return new MIStackInfoDepth(threadId); }

	public MICommand<MIStackListArgumentsInfo> createMIStackListArguments(boolean showValues) {
		return new MIStackListArguments(showValues);
	}

	public MICommand<MIStackListArgumentsInfo> createMIStackListArguments(boolean showValues, int low, int high) {
		return new MIStackListArguments(showValues, low, high);
	}

	public MICommand<MIStackListFramesInfo> createMIStackListFrames(String threadId) { return new MIStackListFrames(threadId); }

	public MICommand<MIStackListVariablesInfo> createMIStackListVariables(boolean printValues) {
		return new MIStackListVariables(printValues);
	}

	public MICommand<MIStackListVariablesInfo> createMIStackListVariables(boolean printValues, String threadId, String frameId) {
		return new MIStackListVariables(printValues, threadId, frameId);
	}

	public MICommand<MIInfo> createMIStackSelectFrame(int frameNum) {
		return new MIStackSelectFrame(frameNum);
	}

	public MICommand<MIListThreadGroupsInfo> createMIMIListThreadGroups() { return new MIListThreadGroups(); }

	public MICommand<MiSourceFilesInfo> createMiFileListExecSourceFiles() {
		return new MIFileListExecSourceFiles();
	}

	public MICommand<MiSymbolInfoFunctionsInfo> createMiSymbolInfoFunctions() {
		return new MISymbolInfoFunctions();
	}

	public MICommand<MiSymbolInfoFunctionsInfo> createMiSymbolInfoFunctions(String typeRegExp, String nameRegExp, int maxResults, boolean includeNonDebug) {
		return new MISymbolInfoFunctions(typeRegExp, nameRegExp, maxResults, includeNonDebug);
	}

	public MICommand<MIVarAssignInfo> createMIVarAssign(String name, String expression) {
		return new MIVarAssign(name, expression);
	}

	public MICommand<MIVarCreateInfo> createMIVarCreate(String expression) {
		return new MIVarCreate(expression);
	}

	public MICommand<MIVarCreateInfo> createMIVarCreate(String name, String expression) {
		return new MIVarCreate(name, expression);
	}

	public MICommand<MIVarCreateInfo> createMIVarCreate(String name, String frameAddr,
													   String expression) {
		return new MIVarCreate(name, frameAddr, expression);
	}

	public MICommand<MIVarDeleteInfo> createMIVarDelete(String name) {
		return new MIVarDelete(name);
	}

	public MICommand<MIVarEvaluateExpressionInfo> createMIVarEvaluateExpression(String name) {
		return new MIVarEvaluateExpression(name);
	}

	public MICommand<MIVarInfoExpressionInfo> createMIVarInfoExpression(String name) {
		return new MIVarInfoExpression(name);
	}

	public MICommand<MIVarInfoNumChildrenInfo> createMIVarInfoNumChildren(String name) {
		return new MIVarInfoNumChildren(name);
	}

	public MICommand<MIVarInfoPathExpressionInfo> createMIVarInfoPathExpression( String name) {
		return new MIVarInfoPathExpression(name);
	}

	public MICommand<MIVarInfoTypeInfo> createMIVarInfoType(String name) {
		return new MIVarInfoType(name);
	}

	public MICommand<MIVarListChildrenInfo> createMIVarListChildren(String name) {
		return new MIVarListChildren(name);
	}

	/** @since 4.0 */
	public MICommand<MIVarListChildrenInfo> createMIVarListChildren(String name, int from,
																   int to) {
		return new MIVarListChildren(name, from, to);
	}

	public MICommand<MIVarSetFormatInfo> createMIVarSetFormat(String name, String fmt) {
		return new MIVarSetFormat(name, fmt);
	}

	/** @since 4.0 */
	public MICommand<MIInfo> createMIVarSetUpdateRange(String name, int from, int to) {
		return new MIVarSetUpdateRange(name, from, to);
	}

	public MICommand<MIVarShowAttributesInfo> createMIVarShowAttributes(String name) {
		return new MIVarShowAttributes(name);
	}

	public MICommand<MIVarShowFormatInfo> createMIVarShowFormat(String name) {
		return new MIVarShowFormat(name);
	}

	public MICommand<MIVarUpdateInfo> createMIVarUpdate(String name) {
		return new MIVarUpdate(name);
	}
}
