package jdwp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
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
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("booleanMethod", "void",
                Collections.singletonList("boolean"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(4, lines.iterator().next());
    }

    @Test
    public void testBooleanMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("booleanMethod", "void",
                Collections.singletonList("java.lang.Boolean"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(5, lines.iterator().next());
    }

    @Test
    public void testPrimitiveByteMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("byteMethod", "void",
                Collections.singletonList("byte"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(6, lines.iterator().next());
    }

    @Test
    public void testByteMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("byteMethod", "void",
                Collections.singletonList("java.lang.Byte"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(7, lines.iterator().next());
    }

    @Test
    public void testPrimitiveCharMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("charMethod", "void",
                Collections.singletonList("char"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(8, lines.iterator().next());
    }

    @Test
    public void testCharMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("charMethod", "void",
                Collections.singletonList("java.lang.Character"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(9, lines.iterator().next());
    }

    @Test
    public void testPrimitiveShortMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("shortMethod", "void",
                Collections.singletonList("short"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(10, lines.iterator().next());
    }

    @Test
    public void testShortMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("shortMethod", "void",
                Collections.singletonList("java.lang.Short"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(11, lines.iterator().next());
    }

    @Test
    public void testPrimitiveIntMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("intMethod", "void",
                Collections.singletonList("int"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(12, lines.iterator().next());
    }

    @Test
    public void testIntMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("intMethod", "void",
                Collections.singletonList("java.lang.Integer"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(13, lines.iterator().next());
    }

    @Test
    public void testPrimitiveLongMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("longMethod", "void",
                Collections.singletonList("long"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(14, lines.iterator().next());
    }

    @Test
    public void testLongMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("longMethod", "void",
                Collections.singletonList("java.lang.Long"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(15, lines.iterator().next());
    }

    @Test
    public void testPrimitiveFloatMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("floatMethod", "void",
                Collections.singletonList("float"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(16, lines.iterator().next());
    }

    @Test
    public void testFloatMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("floatMethod", "void",
                Collections.singletonList("java.lang.Float"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(17, lines.iterator().next());
    }

    @Test
    public void testPrimitiveDoubleMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("doubleMethod", "void",
                Collections.singletonList("double"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(18, lines.iterator().next());
    }

    @Test
    public void testDoubleMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("doubleMethod", "void",
                Collections.singletonList("java.lang.Double"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(19, lines.iterator().next());
    }

    @Test
    public void testObjectMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("objectMethod", "void",
                Collections.singletonList("java.lang.Object"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(20, lines.iterator().next());
    }

    @Test
    public void testMethodSpanningSeveralLines() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("object1Method", "void",
                Collections.singletonList("java.lang.Object"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 3);
        var iterator = lines.iterator();
        assertEquals(21, iterator.next());
        assertEquals(22, iterator.next());
        assertEquals(23, iterator.next());
    }

    @Test
    public void testClassParameterMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("classParameterTypeMethod", "void",
                Collections.singletonList("java.lang.Object"), false);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(24, lines.iterator().next());
    }

    @Test
    public void testMethodParameterMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("methodParameterTypeMethod", "void",
                Collections.singletonList("java.lang.Object"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(25, lines.iterator().next());
    }

    @Test
    public void testParameterTypeMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("parameterTypeMethod", "void",
                Collections.singletonList(Sample.class.getName()), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(26, lines.iterator().next());
    }

    @Test
    public void testParameterTypeWithExtendsMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("parameterTypeMethod1", "void",
                Collections.singletonList(Sample.class.getName()), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(27, lines.iterator().next());
    }

    @Test
    public void testParameterTypeWithSuperMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("parameterTypeMethod2", "void",
                Collections.singletonList(Sample.class.getName()), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(28, lines.iterator().next());
    }

    @Test
    public void testMethodWith2Parameters() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("methodWith2Parameters", "void",
                List.of(Sample.class.getName(), "int"), true);
        var info = new MethodInfo(type, signature);
        var lines = info.getLines().get();
        assertNotNull(lines);
        assertTrue(lines.size() == 1);
        assertEquals(29, lines.iterator().next());
    }
}
