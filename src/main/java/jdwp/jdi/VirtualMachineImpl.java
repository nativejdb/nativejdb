/*
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 * Copyright (C) 2018 JetBrains s.r.o.
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License v2 with Classpath Exception.
 * The text of the license is available in the file LICENSE.TXT.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See LICENSE.TXT for more details.
 *
 * You may contact JetBrains s.r.o. at Na Hřebenech II 1718/10, 140 00 Prague,
 * Czech Republic or at legal@jetbrains.com.
 *
 * Copyright (C) 2022 IBM Corporation
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License v2 with Classpath Exception.
 * The text of the license is available in the file LICENSE.TXT.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See LICENSE.TXT for more details.
 *
 */

package jdwp.jdi;

import com.sun.jdi.Bootstrap;
import jdwp.JDWP;
import com.sun.jdi.VirtualMachineManager;
import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.memory.SystemDictionary;
import sun.jvm.hotspot.memory.Universe;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.runtime.JavaThread;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.utilities.Assert;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.*;

public class VirtualMachineImpl {

    private final HotSpotAgent     saAgent = new HotSpotAgent();
    private VM               saVM;
    private Universe         saUniverse;
    private SystemDictionary saSystemDictionary;
    private ObjectHeap       saObjectHeap;

    VM saVM() {
        return saVM;
    }

    SystemDictionary saSystemDictionary() {
        return saSystemDictionary;
    }
    
    Universe saUniverse() {
        return saUniverse;
    }

    ObjectHeap saObjectHeap() {
        return saObjectHeap;
    }

    VirtualMachineManager vmmgr;

    // Per-vm singletons for primitive types and for void.
    private final PrimitiveTypeImpl theBooleanType = new PrimitiveTypeImpl(JDWP.Tag.BOOLEAN);
    private final PrimitiveTypeImpl theByteType = new PrimitiveTypeImpl(JDWP.Tag.BYTE);
    private final PrimitiveTypeImpl theCharType = new PrimitiveTypeImpl(JDWP.Tag.CHAR);
    private final PrimitiveTypeImpl theShortType = new PrimitiveTypeImpl(JDWP.Tag.SHORT);
    private final PrimitiveTypeImpl theIntegerType = new PrimitiveTypeImpl(JDWP.Tag.INT);
    private final PrimitiveTypeImpl theLongType = new PrimitiveTypeImpl(JDWP.Tag.LONG);
    private final PrimitiveTypeImpl theFloatType = new PrimitiveTypeImpl(JDWP.Tag.FLOAT);
    private final PrimitiveTypeImpl theDoubleType = new PrimitiveTypeImpl(JDWP.Tag.DOUBLE);

    final VoidValueImpl voidVal = new VoidValueImpl();

    private final Map<Long, ReferenceTypeImpl>  typesById = new HashMap<Long, ReferenceTypeImpl>();
    private boolean   retrievedAllTypes = false;
    private List<ReferenceTypeImpl>      bootstrapClasses;      // all bootstrap classes
    private ArrayList<ThreadReferenceImpl> allThreads;
    private ArrayList<ThreadGroupReferenceImpl> topLevelGroups;
    final   int       sequenceNumber;

    // ObjectReference cache
    // "objectsByID" protected by "synchronized(this)".
    private final Map<Long, SoftObjectReference>            objectsByID = new HashMap<Long, SoftObjectReference>();
    private final ReferenceQueue referenceQueue = new ReferenceQueue();

    // names of some well-known classes to jdwp.jdi
    private final String javaLangString = "java/lang/String";
    private final String javaLangThread = "java/lang/Thread";
    private final String javaLangThreadGroup = "java/lang/ThreadGroup";
    private final String javaLangClass = "java/lang/Class";
    private final String javaLangClassLoader = "java/lang/ClassLoader";

    // used in ReferenceTypeImpl.isThrowableBacktraceField
    final String javaLangThrowable = "java/lang/Throwable";

    // names of classes used in array assignment check
    // refer to ArrayTypeImpl.isAssignableTo
    final String javaLangObject = "java/lang/Object";
    final String javaLangCloneable = "java/lang/Cloneable";
    final String javaIoSerializable = "java/io/Serializable";

    private void init() {
        saVM = VM.getVM();
        saUniverse = saVM.getUniverse();
        saSystemDictionary = saVM.getSystemDictionary();
        saObjectHeap = saVM.getObjectHeap();
    }

    static public VirtualMachineImpl createVirtualMachineForPID(int pid,
                                                                int sequenceNumber)
        throws Exception {
        VirtualMachineImpl myvm = new VirtualMachineImpl(Bootstrap.virtualMachineManager(), sequenceNumber);
        try {
            myvm.saAgent.attach(pid);
            myvm.init();
        } catch (Exception ee) {
            myvm.saAgent.detach();
            throw ee;
        }
        return myvm;
    }


    VirtualMachineImpl(VirtualMachineManager mgr, int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        this.vmmgr = mgr;

        // By default SA agent classes prefer Windows process debugger
        // to windbg debugger. SA expects special properties to be set
        // to choose other debuggers. We will set those here before
        // attaching to SA agent.

        System.setProperty("sun.jvm.hotspot.debugger.useWindbgDebugger", "true");
    }

    public boolean equals(Object obj) {
        // Oh boy; big recursion troubles if we don't have this!
        // See MirrorImpl.equals
        return this == obj;
    }

    public int hashCode() {
        // big recursion if we don't have this. See MirrorImpl.hashCode
        return System.identityHashCode(this);
    }

    public List<ReferenceTypeImpl> allClasses() {
        if (!retrievedAllTypes) {
            for (Klass saKlass : CompatibilityHelper.INSTANCE.allClasses(saSystemDictionary, saVM)) {
                referenceType(saKlass);
            }
            retrievedAllTypes = true;
        }
        return Collections.unmodifiableList(new ArrayList<ReferenceTypeImpl>(typesById.values()));
    }

    // classes loaded by bootstrap loader
    List<ReferenceTypeImpl> bootstrapClasses() {
        if (bootstrapClasses == null) {
            bootstrapClasses = new ArrayList<ReferenceTypeImpl>();
            for (ReferenceTypeImpl type : allClasses()) {
                if (type.classLoader() == null) {
                    bootstrapClasses.add(type);
                }
            }
        }
        return bootstrapClasses;
    }

    public List<ReferenceTypeImpl> findReferenceTypes(String signature) {
        // The signature could be Lx/y/z; or [....
        // If it is Lx/y/z; the internal type name is x/y/x
        // for array klasses internal type name is same as
        // signature
        String typeName;
        if (signature.charAt(0) == 'L') {
            typeName = signature.substring(1, signature.length() - 1);
        } else {
            typeName = signature;
        }

        List<ReferenceTypeImpl> list = new ArrayList<ReferenceTypeImpl>(1);
        for (ReferenceTypeImpl type : allClasses()) {
            if (type.name().equals(typeName)) {
                list.add(type);
            }
        }
        return list;
    }

    ReferenceTypeImpl referenceType(Klass kk) {
        ReferenceTypeImpl retType = typesById.get(ReferenceTypeImpl.uniqueID(kk, this));
        if (retType == null) {
            retType = addReferenceType(kk);
        }
        return retType;
    }

    private ReferenceTypeImpl addReferenceType(Klass kk) {
        ReferenceTypeImpl newRefType;
        if (kk instanceof ObjArrayKlass || kk instanceof TypeArrayKlass) {
            newRefType = new ArrayTypeImpl(this, (ArrayKlass)kk);
        } else if (kk instanceof InstanceKlass) {
            if (kk.isInterface()) {
                newRefType = new InterfaceTypeImpl(this, (InstanceKlass)kk);
            } else {
                newRefType = new ClassTypeImpl(this, (InstanceKlass)kk);
            }
        } else {
            throw new RuntimeException("should not reach here:" + kk);
        }

        typesById.put(newRefType.uniqueID(), newRefType);
        return newRefType;
    }

    private List<ThreadReferenceImpl> getAllThreads() {
        if (allThreads == null) {
            allThreads = new ArrayList<ThreadReferenceImpl>(10);  // Might be enough, might not be
            for (JavaThread thread : CompatibilityHelper.INSTANCE.getThreads(saVM)) {
                // refer to JvmtiEnv::GetAllThreads in jvmtiEnv.cpp.
                // filter out the hidden-from-external-view threads.
                if (!thread.isHiddenFromExternalView()) {
                    ThreadReferenceImpl myThread = threadMirror(thread);
                    allThreads.add(myThread);
                }
            }
        }
        return allThreads;
    }

    public List<ThreadReferenceImpl> allThreads() { //fixme jjh
        return Collections.unmodifiableList(getAllThreads());
    }

    public List<ThreadGroupReferenceImpl> topLevelThreadGroups() { //fixme jjh
        // The doc for ThreadGroup says that The top-level thread group
        // is the only thread group whose parent is null.  This means there is
        // only one top level thread group.  There will be a thread in this
        // group so we will just find a thread whose threadgroup has no parent
        // and that will be it.

        if (topLevelGroups == null) {
            topLevelGroups = new ArrayList<ThreadGroupReferenceImpl>(1);
            for (ThreadReferenceImpl threadReference : getAllThreads()) {
                ThreadGroupReferenceImpl myGroup = threadReference.threadGroup();
                if (myGroup.parent() == null) {
                    topLevelGroups.add(myGroup);
                    break;
                }
            }
        }
        return  Collections.unmodifiableList(topLevelGroups);
    }

    public BooleanValueImpl mirrorOf(boolean value) {
        return new BooleanValueImpl(value);
    }

    public ByteValueImpl mirrorOf(byte value) {
        return new ByteValueImpl(value);
    }

    public CharValueImpl mirrorOf(char value) {
        return new CharValueImpl(value);
    }

    public ShortValueImpl mirrorOf(short value) {
        return new ShortValueImpl(value);
    }

    public IntegerValueImpl mirrorOf(int value) {
        return new IntegerValueImpl(value);
    }

    public LongValueImpl mirrorOf(long value) {
        return new LongValueImpl(value);
    }

    public FloatValueImpl mirrorOf(float value) {
        return new FloatValueImpl(value);
    }

    public DoubleValueImpl mirrorOf(double value) {
        return new DoubleValueImpl(value);
    }

    public void dispose() {
        saAgent.detach();
//        notifyDispose();
    }

    public boolean canWatchFieldModification() {
        return false;
    }

    public boolean canWatchFieldAccess() {
        return false;
    }

    public boolean canGetBytecodes() {
        return true;
    }

    public boolean canGetSyntheticAttribute() {
        return true;
    }

    // FIXME: For now, all monitor capabilities are disabled
    public boolean canGetOwnedMonitorInfo() {
        return false;
    }

    public boolean canGetCurrentContendedMonitor() {
        return false;
    }

    public boolean canGetMonitorInfo() {
        return false;
    }

    public boolean canUseInstanceFilters() {
        return false;
    }

    public boolean canRedefineClasses() {
        return false;
    }

    public boolean canAddMethod() {
        return false;
    }

    public boolean canUnrestrictedlyRedefineClasses() {
        return false;
    }

    public boolean canPopFrames() {
        return false;
    }

    public boolean canGetSourceDebugExtension() {
        // We can use InstanceKlass.getSourceDebugExtension only if
        // ClassFileParser parsed the info. But, ClassFileParser parses
        // SourceDebugExtension attribute only if corresponding JVMDI/TI
        // capability is set to true. Currently, vmStructs does not expose
        // JVMDI/TI capabilities and hence we conservatively assume false.
        return false;
    }

    public boolean canRequestVMDeathEvent() {
        return false;
    }

    // new method since 1.6
    public boolean canForceEarlyReturn() {
        return false;
    }

    // new method since 1.6
    public boolean canGetConstantPool() {
        return true;
    }

    // new method since 1.6
    public boolean canGetClassFileVersion() {
        return true;
    }

    // new method since 1.6
    // Real body will be supplied later.
    public boolean canGetInstanceInfo() {
        return true;
    }

    // new method since 1.6
    public boolean canUseSourceNameFilters() {
        return false;
    }

    // new method since 1.6.
    public boolean canRequestMonitorEvents() {
        return false;
    }

    // new method since 1.6.
    public boolean canGetMonitorFrameInfo() {
        return true;
    }

    // new method since 1.6
    // Real body will be supplied later.
    public long[] instanceCounts(List<? extends ReferenceTypeImpl> classes) {
        if (!canGetInstanceInfo()) {
            throw new UnsupportedOperationException(
                      "target does not support getting instances");
        }

        int size = classes.size();
        final Map<Address, Long> instanceMap = new HashMap<Address, Long>(size);

        boolean allAbstractClasses = true;
        for (ReferenceTypeImpl rti : classes) {
            instanceMap.put(CompatibilityHelper.INSTANCE.getAddress(rti.ref()), 0L);
            if (!(rti.isAbstract() || (rti instanceof InterfaceTypeImpl))) {
                allAbstractClasses = false;
            }
        }

        if (allAbstractClasses) {
            return new long[size];
        }

        saObjectHeap.iterate(new DefaultHeapVisitor() {
            public boolean doObj(Oop oop) {
                Address klassAddress = CompatibilityHelper.INSTANCE.getKlassAddress(oop);
                Long current = instanceMap.get(klassAddress);
                if (current != null) {
                    instanceMap.put(klassAddress, current + 1);
                }
                return false;
            }
        });

        final long[] retValue = new long[size] ;
        for (int i = 0; i < retValue.length; i++) {
            retValue[i] = instanceMap.get(CompatibilityHelper.INSTANCE.getAddress(classes.get(i).ref()));
        }

        return retValue;
    }

    private List<String> getPath (String pathName) {
        String cp = saVM.getSystemProperty(pathName);
        if (cp == null) {
            return Collections.emptyList();
        }
        String pathSep = saVM.getSystemProperty("path.separator");
        ArrayList<String> al = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(cp, pathSep);
        while (st.hasMoreTokens()) {
            al.add(st.nextToken());
        }
        al.trimToSize();
        return al;
    }

    public List<String> classPath() {
        return getPath("java.class.path");
    }

    public List<String> bootClassPath() {
        return getPath("sun.boot.class.path");
    }

    public String baseDirectory() {
        return saVM.getSystemProperty("user.dir");
    }

    public String description() {
//        String version_format = ResourceBundle.getBundle("com.sun.tools.jdwp.jdi.resources.jdwp.jdi").getString("version_format");
        String version_format = "Java Debug Interface (Reference Implementation) version {0}.{1} \\n{2}";
        return java.text.MessageFormat.format(version_format,
                                              "" + vmmgr.majorInterfaceVersion(),
                                              "" + vmmgr.minorInterfaceVersion(),
                                              name());
    }

    public String version() {
        String version = saVM.getSystemProperty("java.version");
        return version != null ? version : saVM.getVMRelease();
    }

    public String name() {
        return "JVM version " + version() +
                " (" + saVM.getSystemProperty("java.vm.name") + ", " + saVM.getSystemProperty("java.vm.info") + ")";
    }

    public int jdwpMajor() {
        return vmmgr.majorInterfaceVersion();
    }

    public int jdwpMinor() {
        return vmmgr.minorInterfaceVersion();
    }

    public String toString() {
        return name();
    }

    PrimitiveTypeImpl primitiveTypeMirror(char tag) {
        switch (tag) {
        case 'Z':
                return theBooleanType;
        case 'B':
                return theByteType;
        case 'C':
                return theCharType;
        case 'S':
                return theShortType;
        case 'I':
                return theIntegerType;
        case 'J':
                return theLongType;
        case 'F':
                return theFloatType;
        case 'D':
                return theDoubleType;
        default:
                throw new IllegalArgumentException("Unrecognized primitive tag " + tag);
        }
    }

    private void processQueue() {
        Reference ref;
        while ((ref = referenceQueue.poll()) != null) {
            removeObjectMirror((SoftObjectReference)ref);
        }
    }

    // Address value is used as uniqueID by ObjectReferenceImpl
    long getAddressValue(Address address) {
        return saVM.getDebugger().getAddressValue(address);
    }

    private ObjectReferenceImpl getCachedObjectMirror(long id) {
        // Handle any queue elements that are not strongly reachable
        processQueue();

        SoftObjectReference ref = objectsByID.get(id);
        return ref != null ? ref.object() : null;
    }

    private ObjectReferenceImpl createObjectMirror(long id, Oop key) {
        ObjectReferenceImpl object = null;
        Klass klass = key.getKlass();
        ReferenceTypeImpl type = referenceType(klass);
        if (key instanceof Instance) {
            // look for well-known classes
            Symbol classNameSymbol = klass.getName();
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(classNameSymbol != null, "Null class name");
            }
            String className = classNameSymbol.asString();
            Instance inst = (Instance) key;
            if (className.equals(javaLangString)) {
                object = new StringReferenceImpl(type, inst);
            } else if (className.equals(javaLangThread)) {
                object = new ThreadReferenceImpl(type, inst);
            } else if (className.equals(javaLangThreadGroup)) {
                object = new ThreadGroupReferenceImpl(type, inst);
            } else if (className.equals(javaLangClass)) {
                object = new ClassObjectReferenceImpl(type, inst);
            } else if (className.equals(javaLangClassLoader)) {
                object = new ClassLoaderReferenceImpl(type, inst);
            } else {
                // not a well-known class. But the base class may be
                // one of the known classes.
                Klass kls = klass.getSuper();
                while (kls != null) {
                    className = kls.getName().asString();
                    // java.lang.Class and java.lang.String are final classes
                    if (className.equals(javaLangThread)) {
                        object = new ThreadReferenceImpl(type, inst);
                        break;
                    } else if(className.equals(javaLangThreadGroup)) {
                        object = new ThreadGroupReferenceImpl(type, inst);
                        break;
                    } else if (className.equals(javaLangClassLoader)) {
                        object = new ClassLoaderReferenceImpl(type, inst);
                        break;
                    }
                    kls = kls.getSuper();
                }

                if (object == null) {
                    // create generic object reference
                    object = new ObjectReferenceImpl(type, inst);
                }
            }
        } else if (key instanceof TypeArray || key instanceof ObjArray) {
            object = new ArrayReferenceImpl(type, (Array) key);
        } else {
            throw new RuntimeException("unexpected object type " + key);
        }

        /*
         * If there was no previous entry in the table, we add one here
         * If the previous entry was cleared, we replace it here.
         */
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(id == object.uniqueID(), "Unique id does not match");
        }
        objectsByID.put(id, new SoftObjectReference(id, object, referenceQueue));

        return object;
    }

    public ObjectReferenceImpl objectMirror(long id) {
        ObjectReferenceImpl object = getCachedObjectMirror(id);
        if (object == null) {
            object = createObjectMirror(id, saObjectHeap.newOop(saVM.getDebugger().parseAddress("0x" + Long.toHexString(id)).addOffsetToAsOopHandle(0)));
        }
        return object;
    }

    ObjectReferenceImpl objectMirror(OopHandle handle) {
        if (handle == null) {
            return null;
        }
        long id = ObjectReferenceImpl.uniqueID(handle, this);
        ObjectReferenceImpl object = getCachedObjectMirror(id);
        if (object == null) {
            object = createObjectMirror(id, saObjectHeap.newOop(handle));
        }
        return object;
    }

    ObjectReferenceImpl objectMirror(Oop key) {
        if (key == null) {
            return null;
        }
        long id = ObjectReferenceImpl.uniqueID(key.getHandle(), this);
        ObjectReferenceImpl object = getCachedObjectMirror(id);
        if (object == null) {
            object = createObjectMirror(id, key);
        }
        return object;
    }

    private void removeObjectMirror(SoftObjectReference ref) {
        /*
         * This will remove the soft reference if it has not been
         * replaced in the cache.
         */
        objectsByID.remove(ref.key());
    }

    ThreadReferenceImpl threadMirror(JavaThread jt) {
        return (ThreadReferenceImpl) objectMirror(jt.getThreadObj());
    }

    ThreadGroupReferenceImpl threadGroupMirror(Instance id) {
        return (ThreadGroupReferenceImpl) objectMirror(id);
    }

    ClassLoaderReferenceImpl classLoaderMirror(Instance id) {
        return (ClassLoaderReferenceImpl) objectMirror(id);
    }

    ClassObjectReferenceImpl classObjectMirror(Instance id) {
        return (ClassObjectReferenceImpl) objectMirror(id);
    }

    // Use of soft refs and caching stuff here has to be re-examined.
    //  It might not make sense for JDI - SA.
    static private class SoftObjectReference extends SoftReference<ObjectReferenceImpl> {
       Long key;

       SoftObjectReference(Long key, ObjectReferenceImpl mirror, ReferenceQueue queue) {
           super(mirror, queue);
           this.key = key;
       }

        Long key() {
           return key;
       }

       ObjectReferenceImpl object() {
           return get();
       }
   }

    public ThreadReferenceImpl getThreadById(long id) {
        // Start Hack
        if (id == 1) { //asking for main thread
            for (ThreadReferenceImpl thread : allThreads()) {
                if ("main".equals(thread.name())) {
                    return thread;
                }
            }
        }
        // End Hack

        for (ThreadReferenceImpl thread : allThreads()) {
            if (thread.uniqueID() == id) {
                return thread;
            }
        }
        throw new IllegalStateException("Thread with id " + id + " not found");
    }

    public ReferenceTypeImpl getReferenceTypeById(long id) {
        ReferenceTypeImpl res = typesById.get(id);
        if (res == null) {
            throw new IllegalStateException("ReferenceType with id " + id + " not found");
        }
        return res;
    }

    public ThreadGroupReferenceImpl getThreadGroupReferenceById(long id) {
        for (ThreadReferenceImpl thread : allThreads()) {
            ThreadGroupReferenceImpl threadGroup = thread.threadGroup();
            if (threadGroup.uniqueID() == id) {
                return threadGroup;
            }
        }
        throw new IllegalStateException("ThreadGroup with id " + id + " not found");
    }
}
