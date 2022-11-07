package jdwp.model;

import jdwp.Translator;

import java.util.Objects;

/**
 * A unique representation of a class name as it may comes from either GDB (printable) or JNI (encoded).
 */
public class ClassName {
    public static final ClassName JAVA_LANG_STRING = ClassName.fromGDB(String.class.getName());
    private final String printable;
    private final String JNI;

    private ClassName(String printable, String JNI) {
        this.printable = printable;
        this.JNI = JNI;
    }

    public static ClassName fromGDB(String gdbType) {
        var type = Translator.normalizeType(gdbType);
        return new ClassName(type, Translator.gdb2JNI(type));
    }

    /**
     * Get a ClassName from the value stored into the GDB hub name. This is because the syntax of this field is a mix
     * between printable class names (eg java.lang.String) and pseudo JNI names (eg [Ljava.lang.String;)
     *
     * @param hubName the value stored in GDB hub name
     * @return the class name
     */
    public static ClassName fromHub(String hubName) {
        if (hubName.charAt(0) == JNIConstants.ARRAY) {
            return fromJNI(hubName.replace('.', '/'));
        }
        else {
            return fromGDB(hubName);
        }
    }

    public static ClassName fromJNI(String JNI) {
        return new ClassName(Translator.JNI2gdb(JNI), JNI);
    }

    public String getPrintable() {
        return printable;
    }

    public String getJNI() {
        return JNI;
    }

    public boolean isArray() {
        return JNI.charAt(0) == JNIConstants.ARRAY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassName className = (ClassName) o;
        return JNI.equals(className.JNI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(JNI);
    }
}
