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
 * Tests the Translator class for normalizing C/C++ and Java signatures.
 */
public class TestTranslator {

    @Test
    public void testNormalizeName() {

        String javaMethodWithParams = "HelloMethod/HelloMethod::hello(Z)";
        String javaMethodWithoutParams = "HelloMethod/HelloMethod::hello()";
        String gdbMethodWithParams = "HelloMethod/HelloMethod::hello(java.lang.String[] *)";
        String gdbMethodWithoutParams = "HelloMethod/HelloMethod::hello";
        String expected = "HelloMethod.HelloMethod::hello";

        String[] inputs = new String[]{javaMethodWithParams, javaMethodWithoutParams,
                                        gdbMethodWithParams, gdbMethodWithoutParams};

        for (String input : inputs) {
            assertEquals("Java and GDB signature of class method should be normalized to the same string",
                                    expected,
                                    Translator.normalizeMethodName(input));
        }
    }

}
