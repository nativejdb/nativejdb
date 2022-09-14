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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the Translator class for converting C/C++ info to Java.
 */
public class TestTranslator {

    @Test
    public void testNormalizeFunc() {

        testNormalizePrimitives();

        // Tests objects as parameters
        String input = "HelloMethod.HelloMethod::main(java.lang.String[] *)";
        String expected = "HelloMethod/HelloMethod::main([Ljava/lang/String;)";
        String actual = Translator.normalizeFunc(input);
        assertEquals("Primitive signature tests for one object parameter", expected, actual);
    }

    /**
     * Tests for multiple primitive parameters
     */


    /**
     * Tests the parameters for all the signatures
     */
    @Test
    public void testNormalizePrimitives() {

        String[] types = new String[]{Translator.JAVA_BOOLEAN, Translator.JAVA_BYTE,
                Translator.JAVA_CHAR, Translator.JAVA_SHORT,
                Translator.JAVA_INT, Translator.JAVA_LONG,
                Translator.JAVA_FLOAT, Translator.JAVA_DOUBLE,
                Translator.JAVA_VOID};
        StringBuilder message = new StringBuilder("Primitive signature for boolean should be converted to Z");
        StringBuilder gdbInput = new StringBuilder("HelloMethod.HelloMethod::hello(boolean)");
        StringBuilder expected = new StringBuilder("HelloMethod/HelloMethod::hello(Z)");
        String        actual;

        int msgIndex = 24;                          // the index for b in boolean
        int inputIndex = gdbInput.indexOf("(") + 1; // the index for b in boolean
        int expectedIndex = expected.indexOf("(") + 1;
        int replaceLen = types[0].length();

        for (String currType : types) {
            // Update boolean and Z in message
            message.replace(msgIndex, msgIndex + replaceLen, currType);
            message.replace(message.length() - 1, message.length(), Translator.typeSignature.get(currType));

            // Update parameter type in gdbInput and expected output
            gdbInput.replace(inputIndex, inputIndex + replaceLen, currType);
            expected.replace(expectedIndex, expectedIndex + 1, Translator.typeSignature.get(currType));
            actual = Translator.normalizeFunc(gdbInput.toString());
            assertEquals(message.toString(), expected.toString(), actual);
            replaceLen = currType.length();
        }
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
}
