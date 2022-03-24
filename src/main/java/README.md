# JDWP - GDB COMMANDS TABLE

https://docs.oracle.com/en/java/javase/11/docs/specs/jdwp/jdwp-protocol.html

https://sourceware.org/gdb/current/onlinedocs/gdb/GDB_002fMI.html#GDB_002fMI

| JDWP CommandSet  | JDWP Command | Implemented | VM (static info) | GDB (dynamic info) |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| VirtualMachine (1)
| | Version (1) | ✔️ |✔️| |
| | ClassesBySignature (2) | ✔️|✔️ | |
| | AllClasses (3) |✔️ |✔️ | |
| | AllThreads (4) |✔️ | |✔️ -thread-info |
| | TopLevelThreadGroups (5) |✔️ | ✔️| |
| | Dispose (6) |✔️ | ✔️| |
| | IDSizes (7) |✔️ | ✔️| |
| | Suspend (8) |✔️ | |✔️ -exec-interrupt|
| | Resume (9) |✔️ | |✔️ -exec-continue|
| | Exit (10) |✔️ | |✔️ -gdb-exit|
| | CreateString (11) | ❌ 
| | Capabilities (12) |✔️ | ✔️| |
| | ClassPaths (13) |✔️ | ✔️| |
| | DisposeObjects (14) | ❌ 
| | HoldEvents (15) | ❌ 
| | ReleaseEvents (16) | ❌ 
| | CapabilitiesNew (17) |✔️ | ✔️| |
| | RedefineClasses (18) | ❌ 
| | SetDefaultStratum (19) | ❌ 
| | AllClassesWithGeneric (20) |✔️ | ✔️| |
| | InstanceCounts (21) |✔️ | ✔️| |
| | AllModules (22) | ❌ 
| ReferenceType (2)
| | Signature (1) |✔️ | ✔️| |
| | ClassLoader (2) |✔️ | ✔️| |
| | Modifiers (3) |✔️ | ✔️| |
| | Fields (4) |✔️ | ✔️| |
| | Methods (5) |✔️ | ✔️| |
| | GetValues (6) |✔️ | ✔️| |
| | SourceFile (7) |✔️ | ✔️| |
| | NestedTypes (8) |✔️ | ✔️| |
| | Status (9) |✔️ | ✔️| |
| | Interfaces (10) |✔️ | ✔️| |
| | ClassObject (11) |✔️ | ✔️| |
| | SourceDebugExtension (12) |✔️ | ✔️| |
| | SignatureWithGeneric (13) |✔️ | ✔️| |
| | FieldsWithGeneric (14) |✔️ | ✔️| |
| | MethodsWithGeneric (15) |✔️ | ✔️| |
| | Instances (16) |✔️ | ✔️| |
| | ClassFileVersion (17) |✔️ | ✔️| |
| | ConstantPool (18) |✔️ | ✔️| |
| | Method (19) | ❌ 
| ClassType (3)
| | Superclass (1)|✔️ | ✔️| |
| | SetValues (2)| ❌ 
| | InvokeMethod (3) | ❌ 
| | NewInstance (4) | ❌ 
| ArrayType (4)
| | NewInstance (1)| ❌ 
| InterfaceType (5)
| | InvokeMethod (1)| ❌ 
| Method (6)
| | LineTable (1) |✔️ | ✔️| |
| | VariableTable (2) |✔️ | ✔️| |
| | Bytecodes (3)|✔️ | ✔️| |
| | IsObsolete (4)|✔️ | ✔️| |
| | VariableTableWithGeneric (5)|✔️ | ✔️| |
| Field (8)
| ObjectReference (9)
| | ReferenceType (1)|✔️ | ✔️| |
| | GetValues (2)|✔️ | ✔️| |
| | SetValues (3)| ❌ 
| | MonitorInfo (5)|✔️ | ✔️| |
| | InvokeMethod (6)| ❌ 
| | DisableCollection (7)| ❌ 
| | EnableCollection (8)| ❌ 
| | IsCollected (9)| ❌ 
| | ReferringObjects (10)|✔️ | ✔️| |
| StringReference (10)
| | Value (1)|✔️ | ✔️| |
| ThreadReference (11)
| | Name (1)|✔️ | ✔️| |
| | Suspend (2)| ❌ 
| | Resume (3)| ❌ 
| | Status (4)|✔️ | | SHOULD DO |
| | ThreadGroup (5)|✔️ | ✔️| |
| | Frames (6)|✔️ | |✔️ -stack-list-frames |
| | FrameCount (7)|✔️ | |✔️ -stack-list-frames|
| | OwnedMonitors (8)|✔️ | ✔️| |
| | CurrentContendedMonitor (9)|✔️ | ✔️| |
| | Stop (10)| ❌ 
| | Interrupt (11)| ❌ 
| | SuspendCount (12)|✔️ | ✔️| |
| | OwnedMonitorsStackDepthInfo (13)|✔️ | ✔️| |
| | ForceEarlyReturn (14)| ❌ 
| ThreadGroupReference (12)
| | Name (1)|✔️ | ✔️| |
| | Parent (2)|✔️ | ✔️| |
| | Children (3)|✔️ | ✔️| |
| ArrayReference (13)
| | Length (1)|✔️ | ✔️| |
| | GetValues (2)|✔️ | ✔️| |
| | SetValues (3)| ❌ 
| ClassLoaderReference (14)
| | VisibleClasses (1)|✔️ | ✔️| |
| EventRequest (15)
| | Set (1)
| | SINGLE_STEP	1	 |✔️ | |✔️ |
| | BREAKPOINT	2	  |✔️ | |✔️ |
| | FRAME_POP	3	 
| | EXCEPTION	4	 
| | USER_DEFINED	5	 
| | THREAD_START	6	 
| | THREAD_DEATH	7	 
| | THREAD_END	7	obsolete - was used in jvmdi  
| | CLASS_PREPARE	8	  |✔️ | |✔️ |
| | CLASS_UNLOAD	9	 
| | CLASS_LOAD	10	 
| | FIELD_ACCESS	20	 
| | FIELD_MODIFICATION	21	 
| | EXCEPTION_CATCH	30	 
| | METHOD_ENTRY	40	 
| | METHOD_EXIT	41	 
| | METHOD_EXIT_WITH_RETURN_VALUE	42	 
| | MONITOR_CONTENDED_ENTER	43	 
| | MONITOR_CONTENDED_ENTERED	44	 
| | MONITOR_WAIT	45	 
| | MONITOR_WAITED	46	 
| | VM_START	90	 
| | VM_DEATH	99	 
| | VM_DISCONNECTED
| | Clear (2)
| | BREAKPOINT	2	  |✔️ | |✔️ |
| | ClearAllBreakpoints (3)
| StackFrame (16)
| | GetValues (1) |✔️ | | WIP |
| | SetValues (2) |✔️ | | WIP |
| | ThisObject (3) |✔️ | | WIP |
| | PopFrames (4) | ❌ 
| ClassObjectReference (17)
| | ReflectedType (1)|✔️ | ✔️| |
| ModuleReference (18)
| | Name (1)| ❌ 
| | ClassLoader (2)| ❌ 
| Event (64)
| | Composite (100)
| | VMStart |✔️ | | WIP |
| | SingleStep |✔️ | | ✔️|
| | Breakpoint |✔️ | | ✔️|
| | MethodEntry
| | MethodExit
| | MethodExitWithReturnValue
| | MonitorContendedEnter
| | MonitorContendedEntered
| | MonitorWait
| | MonitorWaited
| | Exception
| | ThreadStart
| | ThreadDeath
| | ClassPrepare |✔️ | | WIP |
| | ClassUnload
| | FieldAccess
| | FieldModification
| | VMDeath |✔️ | | ✔️|
