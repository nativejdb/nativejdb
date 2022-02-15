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

/**
 * @author egor
 */
class CompatibilityHelper {
    static final Compatibility INSTANCE;

    static {
        Compatibility instance = null;
        String version = System.getProperty("java.specification.version");
            String compatibility = "13";
            if (version.equals("1.8") || version.equals("9")) {
                compatibility = "8";
            } else if (version.equals("10") || version.equals("11") || version.equals("12")) {
                compatibility = "10";
            }
            try {
                instance = (Compatibility) Class.forName("jdwp.jdi.CompatibilityHelper" + compatibility).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        INSTANCE = instance;
    }
}
