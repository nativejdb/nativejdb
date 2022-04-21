/*
 * Copyright (c) 2019, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 */
package com.oracle.svm.core.jdk;

import org.graalvm.compiler.serviceprovider.JavaVersionUtil;
import org.graalvm.nativeimage.CurrentIsolate;
import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.nativeimage.impl.InternalPlatform;
import org.graalvm.word.PointerBase;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.core.c.CGlobalData;
import com.oracle.svm.core.c.CGlobalDataFactory;

public abstract class JNIPlatformNativeLibrarySupport extends PlatformNativeLibrarySupport {

    @Platforms(InternalPlatform.PLATFORM_JNI.class)
    protected void loadJavaLibrary() {
        System.loadLibrary("java");

        Target_java_io_FileDescriptor_JNI.initIDs();
        Target_java_io_FileInputStream_JNI.initIDs();
        Target_java_io_FileOutputStream_JNI.initIDs();

        initializeEncoding();
    }

    /**
     * Initializes the encoding used to convert from C strings to Java strings.
     * 
     * The JDK C code has a {@code fastEncoding} field that determines which algorithm to use to
     * convert between C strings and Java strings; the conversion is implemented in the JDK C code,
     * instead of going through a regular JNI conversion (hence the "fast encoding" name). The
     * {@code fastEncoding} field must be initialized before the encoding can be used. We need to
     * ensure that the initialization code runs early, for the following reasons:
     *
     * <ul>
     * <li>On JDK 8, the C method {@code initializeEncoding()} is called lazily from various places.
     * But unfortunately the implementation is not thread safe: a second thread can use
     * uninitialized state while the first thread is still in the initialization. So we need to
     * force initialization here where we are still single threaded.</li>
     *
     * <li>On JDK 11, the initialization is performed from Java during `System.initPhase1` by
     * `System.initProperties`. However, we do not invoke that part of the system initialization
     * because we already have many system properties pre-initialized in the image heap. So we need
     * to force initialization here.</li>
     * </ul>
     * 
     * The choice of {@code fastEncoding} depends on the value of the {@code sun.jnu.encoding}
     * system property. This encoding is also known as the <em>platform encoding</em> or <em>JNU
     * encoding</em>. It is platform-dependent, and is mainly used for platform-dependent tasks,
     * (e.g. for file paths, or in our case, converting from C to Java Strings).
     * 
     * Currently, we inherit the {@code sun.jnu.encoding} system property from image build time (see
     * {@link SystemPropertiesSupport}), i.e., we do not allow it to be specified at run time and
     * (more importantly) also do not look at environment variables to determine the encoding.
     */
    private static void initializeEncoding() {
        if (JavaVersionUtil.JAVA_SPEC >= 11) {
            /*
             * On JDK 11 and later, the method `InitializeEncoding` is an exported JNI function and
             * we can call it directly.
             */
            try (CTypeConversion.CCharPointerHolder name = CTypeConversion.toCString(System.getProperty("sun.jnu.encoding"))) {
                nativeInitializeEncoding(CurrentIsolate.getCurrentThread(), name.get());
            }
        } else {
            /*
             * On JDK 8, the method `initializeEncoding` is not an exported JNI function. We call an
             * exported function that unconditionally calls `initializeEncoding` to trigger the
             * initialization of `fastEncoding`.
             */
            nativeNewStringPlatform(CurrentIsolate.getCurrentThread(), EMPTY_C_STRING.get());
        }
    }

    @CFunction("InitializeEncoding")
    private static native void nativeInitializeEncoding(PointerBase env, CCharPointer name);

    private static final CGlobalData<CCharPointer> EMPTY_C_STRING = CGlobalDataFactory.createCString("");

    /**
     * Converts a C string to a Java String using the platform encoding. On JDK 8, initializes the
     * platform encoding first by reading the {@code sun.jnu.encoding} system property.
     */
    @CFunction("JNU_NewStringPlatform")
    private static native void nativeNewStringPlatform(PointerBase env, CCharPointer str);

    @Platforms(InternalPlatform.PLATFORM_JNI.class)
    protected void loadZipLibrary() {
        /*
         * On JDK 8, the zip library is loaded early during VM startup and not by individual class
         * initializers of classes that actually need the library. JDK 11 changed that behavior, the
         * zip library is properly loaded by classes that depend on it.
         *
         * Therefore, this helper method unconditionally loads the zip library for Java 8. The only
         * other alternative would be to substitute and modify all class initializers of classes
         * that depend on the zip library, which is complicated.
         */
        if (JavaVersionUtil.JAVA_SPEC == 8) {
            System.loadLibrary("zip");
        }
    }
}

@Platforms(InternalPlatform.PLATFORM_JNI.class)
@TargetClass(java.io.FileInputStream.class)
final class Target_java_io_FileInputStream_JNI {
    @Alias
    static native void initIDs();
}

@Platforms(InternalPlatform.PLATFORM_JNI.class)
@TargetClass(java.io.FileDescriptor.class)
final class Target_java_io_FileDescriptor_JNI {
    @Alias
    static native void initIDs();
}

@Platforms(InternalPlatform.PLATFORM_JNI.class)
@TargetClass(java.io.FileOutputStream.class)
final class Target_java_io_FileOutputStream_JNI {
    @Alias
    static native void initIDs();
}

@Platforms({Platform.DARWIN.class, Platform.LINUX.class})
@CLibrary("z")
class ZLib {
}
