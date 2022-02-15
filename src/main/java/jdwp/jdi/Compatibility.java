/*
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
 */

package jdwp.jdi;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.memory.SystemDictionary;
import sun.jvm.hotspot.oops.InstanceKlass;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.Method;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.runtime.JavaThread;
import sun.jvm.hotspot.runtime.VM;

import java.util.List;

/**
 * @author egor
 */
public interface Compatibility {
    Address getAddress(Method method);

    Address getAddress(Klass klass);

    Klass asKlass(Oop ref);

    List<InstanceKlass> getTransitiveInterfaces(InstanceKlass saKlass);

    String getSourceDebugExtension(InstanceKlass saKlass);

    InstanceKlass getMethodHandleKlass();

    Klass getMethodHolder(Method method);

    Address getKlassAddress(Oop oop);

    List<Klass> allClasses(SystemDictionary systemDictionary, VM vm);

    List<ReferenceTypeImpl> visibleClasses(final Oop ref, final VirtualMachineImpl vm);

    List<JavaThread> getThreads(VM vm);
}
