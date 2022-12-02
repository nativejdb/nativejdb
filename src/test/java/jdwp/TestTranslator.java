/*
 * Copyright (C) 2022 IBM Corporation
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License v2 with Classpath Exception.
 * The text of the license is available in the file LICENSE.TXT.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See LICENSE.TXT for more details.
 */

package jdwp;

import jdwp.model.*;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Tests the Translator class for converting C/C++ info to Java.
 */
public class TestTranslator {

    @Test
    public void testNormalizeFuncName() {

        // Tests objects as parameters
        assertEquals("main", Translator.getClassAndMethodName("HelloMethod.HelloMethod::main(java.lang.String[] *)")[1]);
        assertEquals("main", Translator.getClassAndMethodName("main(java.lang.String[] *)")[1]);
        assertEquals("main", Translator.getClassAndMethodName("HelloMethod.HelloMethod::main")[1]);
        assertEquals("main", Translator.getClassAndMethodName("main")[1]);
    }

    private void testMethodSignature(String type, String jni) {
        ReferenceTypes types = new ReferenceTypes(Paths.get("src/test/java"));
        ReferenceType referenceType = new ReferenceType(types, "", ClassName.fromGDB(Sample.class.getName()));
        var signature = Translator.getSignature(type, "", "");
        var info = new MethodInfo(referenceType, signature);
        assertEquals(jni, info.getJNISignature());
    }

    @Test
    public void testVoidEmptySignature() {
        testMethodSignature("void (void)", "()V");
    }

    public void testBooleanEmptySignature() {
        testMethodSignature("boolean (void)", "()Z");
    }

    @Test
    public void testShortEmptySignature() {
        testMethodSignature("short (void)", "()S");
    }

    @Test
    public void testIntEmptySignature() {
        testMethodSignature("int (void)", "()I");
    }

    @Test
    public void testLongEmptySignature() {
        testMethodSignature("long (void)", "()J");
    }

    @Test
    public void testByteEmptySignature() {
        testMethodSignature("byte (void)", "()B");
    }

    @Test
    public void testCharEmptySignature() {
        testMethodSignature("char (void)", "()C");
    }

    @Test
    public void testDoubleEmptySignature() {
        testMethodSignature("double (void)", "()D");
    }

    @Test
    public void testFloatEmptySignature() {
        testMethodSignature("float (void)", "()F");
    }

    @Test
    public void testClassEmptySignature() {
        testMethodSignature("class java.lang.String *(void)", "()Ljava/lang/String;");
    }

    @Test
    public void testInterfaceEmptySignature() {
        testMethodSignature("union java.util.Collection *(void)", "()Ljava/util/Collection;");
    }

    @Test
    public void testOneDimensionArrayEmptySignature() {
        testMethodSignature("int[] (void)", "()[I");
    }

    @Test
    public void testTwoDimensionArrayEmptySignature() {
        testMethodSignature("int[][] (void)", "()[[I");
    }

    @Test
    public void testStaticMethod() {
        ReferenceTypes types = new ReferenceTypes(Paths.get("src/test/java"));
        ReferenceType referenceType = new ReferenceType(types, "", ClassName.fromGDB("classname"));
        var signature = Translator.getSignature("boolean (void)", "classname",
                "methodName");
        var info = new MethodInfo(referenceType, signature);
        assertTrue((info.getModifier() & Modifier.STATIC) == Modifier.STATIC);
    }

    @Test
    public void testInstanceMethod() {
        var types = new ReferenceTypes(Paths.get("src/test/java"));
        var referenceType = new ReferenceType(types, "", ClassName.fromGDB("classname"));
        var signature = Translator.getSignature("boolean (classname *)", "classname",
                "methodName");
        var info = new MethodInfo(referenceType, signature);
        assertFalse((info.getModifier() & Modifier.STATIC) == Modifier.STATIC);
    }

    @Test
    public void testGetQbiccFilenames() {
        String input = "_JHelloNested_HelloNested_main__3Ljava_lang_String_2_V";
        String output = "HelloNested/HelloNested.java";
        assertEquals(output, Translator.getQbiccFilename(input));
    }

    @Test
    public void testGetQbiccFilenamesWithObjects() {
        String input = "_JHelloNested_HelloNested_00024Greeter_greeter__3Ljava_lang_String_2_LHelloNested_HelloNested_00024Greeter_2";
        String output = "HelloNested/HelloNested.java";
        assertEquals(output, Translator.getQbiccFilename(input));
    }

    @Test
    public void testGetQbiccFilenamesWithJavaClasses() {
        String input = "_Jjava_util_Map_entry__Ljava_lang_Object_2Ljava_lang_Object_2_Ljava_util_Map_00024Entry_2";
        String output = "java/util/Map.java";
        assertEquals(output, Translator.getQbiccFilename(input));
    }

    @Test
    public void testAddressDecode8Bytes() {
        assertEquals(-2088992, Translator.decodeAddress("0xffffffffffe01fe0"));
    }

    @Test
    public void testAddressDecode1Byte() {
        assertEquals(255, Translator.decodeAddress("0xff"));
    }
}
