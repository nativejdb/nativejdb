package jdwp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TypeEnricherLinesTest {
    public static final String FILE_PATH = "jdwp/model/Sample.java";
    private ReferenceTypes types;

    @BeforeEach
    public void setup() {
        types = new ReferenceTypes(Paths.get("src/test/java"));
    }

    @Test
    public void testPrimitiveBooleanMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "booleanMethod(boolean)", "booleanMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(4, lines.iterator().next());
    }

    @Test
    public void testBooleanMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "booleanMethod(java.lang.Boolean *)", "booleanMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(5, lines.iterator().next());
    }

    @Test
    public void testPrimitiveByteMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "byteMethod(byte)", "byteMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(6, lines.iterator().next());
    }

    @Test
    public void testByteMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "byteMethod(java.lang.Byte *)", "byteMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(7, lines.iterator().next());
    }

    @Test
    public void testPrimitiveCharMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "charMethod(char)", "charMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(8, lines.iterator().next());
    }

    @Test
    public void testCharMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "charMethod(java.lang.Character *)", "charMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(9, lines.iterator().next());
    }

    @Test
    public void testPrimitiveShortMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "shortMethod(short)", "shortMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(10, lines.iterator().next());
    }

    @Test
    public void testShortMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "shortMethod(java.lang.Short *)", "shortMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(11, lines.iterator().next());
    }

    @Test
    public void testPrimitiveIntMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "intMethod(int)", "intMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(12, lines.iterator().next());
    }

    @Test
    public void testIntMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "intMethod(java.lang.Integer *)", "intMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(13, lines.iterator().next());
    }

    @Test
    public void testPrimitiveLongMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "longMethod(long)", "longMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(14, lines.iterator().next());
    }

    @Test
    public void testLongMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "longMethod(java.lang.Long *)", "longMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(15, lines.iterator().next());
    }

    @Test
    public void testPrimitiveFloatMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "floatMethod(float)", "floatMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(16, lines.iterator().next());
    }

    @Test
    public void testFloatMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "floatMethod(java.lang.Float *)", "floatMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(17, lines.iterator().next());
    }

    @Test
    public void testPrimitiveDoubleMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "doubleMethod(double)", "doubleMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(18, lines.iterator().next());
    }

    @Test
    public void testDoubleMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "doubleMethod(java.lang.Double *)", "doubleMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(19, lines.iterator().next());
    }

    @Test
    public void testObjectMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "objectMethod(java.lang.Object *)", "objectMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(20, lines.iterator().next());
    }

    @Test
    public void testMethodSpanningSeveralLines() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "object1Method(java.lang.Object *)", "object1Method");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 3);
        var iterator = lines.iterator();
        assertEquals(21, iterator.next());
        assertEquals(22, iterator.next());
        assertEquals(23, iterator.next());
    }

    @Test
    public void testClassParameterMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "classParameterTypeMethod(java.lang.Object *)",
                "classParameterTypeMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(24, lines.iterator().next());
    }

    @Test
    public void testMethodParameterMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "methodParameterTypeMethod(java.lang.Object *)",
                "methodParameterTypeMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(25, lines.iterator().next());
    }

    @Test
    public void testParameterTypeMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "parameterTypeMethod(" + Sample.class.getName() + " *)",
                "parameterTypeMethod");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(26, lines.iterator().next());
    }

    @Test
    public void testParameterTypeWithExtendsMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "parameterTypeMethod1(" + Sample.class.getName() + " *)",
                "parameterTypeMethod1");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(27, lines.iterator().next());
    }

    @Test
    public void testParameterTypeWithSuperMethod() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "parameterTypeMethod2(" + Sample.class.getName() + " *)",
                "parameterTypeMethod2");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(28, lines.iterator().next());
    }

    @Test
    public void testMethodWith2Parameters() {
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "methodWith2Parameters(" + Sample.class.getName() + " *, int)",
                "methodWith2Parameters");
        Set<Integer> lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(29, lines.iterator().next());
    }
}
