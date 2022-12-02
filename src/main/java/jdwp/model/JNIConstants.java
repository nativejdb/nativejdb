package jdwp.model;

import java.util.HashMap;
import java.util.Map;

public final class JNIConstants {
    public static final char ARRAY = '[';
    public static final char BYTE = 'B';
    public static final char CHAR = 'C';
    public static final char OBJECT = 'L';
    public static final char FLOAT = 'F';
    public static final char DOUBLE = 'D';
    public static final char INT = 'I';
    public static final char LONG = 'J';
    public static final char SHORT = 'S';
    public static final char VOID = 'V';
    public static final char BOOLEAN = 'Z';

    private static final Map<Character, String> types = Map.ofEntries(
            Map.entry(BYTE, "byte"),
            Map.entry(CHAR, "char"),
            Map.entry(FLOAT, "float"),
            Map.entry(DOUBLE, "double"),
            Map.entry(INT, "int"),
            Map.entry(LONG, "long"),
            Map.entry(SHORT, "short"),
            Map.entry(VOID, "void"),
            Map.entry(BOOLEAN, "boolean")
    );

    public static String fromTag(char tag) {
        return types.get(tag);
    }
}
