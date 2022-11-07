package jdwp.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassNameTest {
    private void verifyJNI(String JNI, String printable) {
        var cn = ClassName.fromJNI(JNI);
        assertEquals(printable, cn.getPrintable());
    }

    private void verifyGDB(String gdbType, String JNI, String printable) {
        var cn = ClassName.fromGDB(gdbType);
        assertEquals(JNI, cn.getJNI());
        assertEquals(printable, cn.getPrintable());
    }

    @Test
    public void checkJNIArray() {
        verifyJNI("[I", "int[]");
    }

    @Test
    public void checkJNIByte() {
        verifyJNI("B", "byte");
    }

    @Test
    public void checkJNIChar() {
        verifyJNI("C", "char");
    }

    @Test
    public void checkJNIObject() {
        verifyJNI("Ljava/lang/String;", "java.lang.String");
    }

    @Test
    public void checkJNIFloat() {
        verifyJNI("F", "float");
    }

    @Test
    public void checkJNIDouble() {
        verifyJNI("D", "double");
    }

    @Test
    public void checkJNIInt() {
        verifyJNI("I", "int");
    }

    @Test
    public void checkJNILong() {
        verifyJNI("J", "long");
    }

    @Test
    public void checkJNIShort() {
        verifyJNI("S", "short");
    }

    @Test
    public void checkJNIVoid() {
        verifyJNI("V", "void");
    }

    @Test
    public void checkJNIBoolean() {
        verifyJNI("Z", "boolean");
    }

    @Test
    public void checkGDBArray() {
        verifyGDB("int[]", "[I", "int[]");
    }

    @Test
    public void checkGDBByte() {
        verifyGDB("byte", "B", "byte");
    }

    @Test
    public void checkGDBChar() {
        verifyGDB("char", "C", "char");
    }

    @Test
    public void checkGDBObject() {
        verifyGDB("class java.lang.String *", "Ljava/lang/String;", "java.lang.String");
    }

    @Test
    public void checkGDBFloat() {
        verifyGDB("float", "F", "float");
    }

    @Test
    public void checkGDBDouble() {
        verifyGDB("double", "D", "double");
    }

    @Test
    public void checkGDBInt() {
        verifyGDB("int", "I", "int");
    }

    @Test
    public void checkGDBLong() {
        verifyGDB("long", "J", "long");
    }

    @Test
    public void checkGDBShort() {
        verifyGDB("short", "S", "short");
    }

    @Test
    public void checkGDBVoid() {
        verifyGDB("void", "V", "void");
    }

    @Test
    public void checkGDBBoolean() {
        verifyGDB("boolean", "Z", "boolean");
    }
}
