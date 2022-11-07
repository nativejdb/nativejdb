package jdwp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TypeEnricherVariablesTest {
    public static final String FILE_PATH = "jdwp/model/Sample.java";
    private ReferenceTypes types;

    @BeforeEach
    public void setup() {
        types = new ReferenceTypes(Paths.get("src/test/java"));
    }

    @Test
    public void testPrimitiveBooleanMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("booleanMethod","void",
                Collections.singletonList("boolean"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(4, variables.get(0).getStartLine());
        assertEquals(4, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Z", variables.get(1).getJNISignature());
        assertEquals(4, variables.get(1).getStartLine());
        assertEquals(4, variables.get(1).getEndLine());
    }

    @Test
    public void testBooleanMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("booleanMethod","void",
                Collections.singletonList("java.lang.Boolean"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(5, variables.get(0).getStartLine());
        assertEquals(5, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Ljava/lang/Boolean;", variables.get(1).getJNISignature());
        assertEquals(5, variables.get(1).getStartLine());
        assertEquals(5, variables.get(1).getEndLine());
    }

    @Test
    public void testPrimitiveByteMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("byteMethod","void",
                Collections.singletonList("byte"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(6, variables.get(0).getStartLine());
        assertEquals(6, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("B", variables.get(1).getJNISignature());
        assertEquals(6, variables.get(1).getStartLine());
        assertEquals(6, variables.get(1).getEndLine());
    }

    @Test
    public void testByteMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("byteMethod","void",
                Collections.singletonList("java.lang.Byte"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(7, variables.get(0).getStartLine());
        assertEquals(7, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Ljava/lang/²Byte;", variables.get(1).getJNISignature());
        assertEquals(7, variables.get(1).getStartLine());
        assertEquals(7, variables.get(1).getEndLine());
    }

    @Test
    public void testPrimitiveCharMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("charMethod","void",
                Collections.singletonList("char"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(8, variables.get(0).getStartLine());
        assertEquals(8, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("C", variables.get(1).getJNISignature());
        assertEquals(8, variables.get(1).getStartLine());
        assertEquals(8, variables.get(1).getEndLine());
    }

    @Test
    public void testCharMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("charMethod","void",
                Collections.singletonList("java.lang.Character"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(9, variables.get(0).getStartLine());
        assertEquals(9, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Ljava/lang/Character;", variables.get(1).getJNISignature());
        assertEquals(9, variables.get(1).getStartLine());
        assertEquals(9, variables.get(1).getEndLine());
    }

    @Test
    public void testPrimitiveShortMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("shortMethod","void",
                Collections.singletonList("short"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(10, variables.get(0).getStartLine());
        assertEquals(10, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("S", variables.get(1).getJNISignature());
        assertEquals(10, variables.get(1).getStartLine());
        assertEquals(10, variables.get(1).getEndLine());
    }

    @Test
    public void testShortMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB((Sample.class.getName())));
        var signature = new MethodSignature("shortMethod","void",
                Collections.singletonList("java.lang.Short"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(11, variables.get(0).getStartLine());
        assertEquals(11, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Ljava/lang/Short;", variables.get(1).getJNISignature());
        assertEquals(11, variables.get(1).getStartLine());
        assertEquals(11, variables.get(1).getEndLine());
    }

    @Test
    public void testPrimitiveIntMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("intMethod","void",
                Collections.singletonList("int"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(12, variables.get(0).getStartLine());
        assertEquals(12, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("I", variables.get(1).getJNISignature());
        assertEquals(12, variables.get(1).getStartLine());
        assertEquals(12, variables.get(1).getEndLine());
    }

    @Test
    public void testIntMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB((Sample.class.getName())));
        var signature = new MethodSignature("intMethod","void",
                Collections.singletonList("java.lang.Integer"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(13, variables.get(0).getStartLine());
        assertEquals(13, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Ljava/lang/Integer;", variables.get(1).getJNISignature());
        assertEquals(13, variables.get(1).getStartLine());
        assertEquals(13, variables.get(1).getEndLine());
    }

    @Test
    public void testPrimitiveLongMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("longMethod","void",
                Collections.singletonList("long"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(14, variables.get(0).getStartLine());
        assertEquals(14, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("J", variables.get(1).getJNISignature());
        assertEquals(14, variables.get(1).getStartLine());
        assertEquals(14, variables.get(1).getEndLine());
    }

    @Test
    public void testLongMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("longMethod","void",
                Collections.singletonList("java.lang.Long"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(15, variables.get(0).getStartLine());
        assertEquals(15, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Ljava/lang/Long;", variables.get(1).getJNISignature());
        assertEquals(15, variables.get(1).getStartLine());
        assertEquals(15, variables.get(1).getEndLine());
    }

    @Test
    public void testPrimitiveFloatMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("floatMethod","void",
                Collections.singletonList("float"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(16, variables.get(0).getStartLine());
        assertEquals(16, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("F", variables.get(1).getJNISignature());
        assertEquals(16, variables.get(1).getStartLine());
        assertEquals(16, variables.get(1).getEndLine());
    }

    @Test
    public void testFloatMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("floatMethod","void",
                Collections.singletonList("java.lang.Float"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(17, variables.get(0).getStartLine());
        assertEquals(17, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Ljava/lang/Float;", variables.get(1).getJNISignature());
        assertEquals(17, variables.get(1).getStartLine());
        assertEquals(17, variables.get(1).getEndLine());
    }

    @Test
    public void testPrimitiveDoubleMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("doubleMethod","void",
                Collections.singletonList("double"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(18, variables.get(0).getStartLine());
        assertEquals(18, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("D", variables.get(1).getJNISignature());
        assertEquals(18, variables.get(1).getStartLine());
        assertEquals(18, variables.get(1).getEndLine());
    }

    @Test
    public void testDoubleMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("doubleMethod","void",
                Collections.singletonList("java.lang.Double"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(19, variables.get(0).getStartLine());
        assertEquals(19, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Ljava/lang/Double;", variables.get(1).getJNISignature());
        assertEquals(19, variables.get(1).getStartLine());
        assertEquals(19, variables.get(1).getEndLine());
    }

    @Test
    public void testObjectMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("objectMethod","void",
                Collections.singletonList("java.lang.Object"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(20, variables.get(0).getStartLine());
        assertEquals(20, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Ljava/lang/Object;", variables.get(1).getJNISignature());
        assertEquals(20, variables.get(1).getStartLine());
        assertEquals(20, variables.get(1).getEndLine());
    }

    @Test
    public void testClassParameterMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("classParameterTypeMethod","void",
                Collections.singletonList("java.lang.Object"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(24, variables.get(0).getStartLine());
        assertEquals(24, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Ljava/lang/Object;", variables.get(1).getJNISignature());
        assertEquals(24, variables.get(1).getStartLine());
        assertEquals(24, variables.get(1).getEndLine());
    }

    @Test
    public void testMethodParameterMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("methodParameterTypeMethod","void",
                Collections.singletonList("java.lang.Object"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(25, variables.get(0).getStartLine());
        assertEquals(25, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("Ljava/lang/Object;", variables.get(1).getJNISignature());
        assertEquals(25, variables.get(1).getStartLine());
        assertEquals(25, variables.get(1).getEndLine());
    }

    @Test
    public void testParameterTypeMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("parameterTypeMethod","void",
                Collections.singletonList(Sample.class.getName()), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(26, variables.get(0).getStartLine());
        assertEquals(26, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(1).getJNISignature());
        assertEquals(26, variables.get(1).getStartLine());
        assertEquals(26, variables.get(1).getEndLine());
    }

    @Test
    public void testParameterTypeWithExtendsMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("parameterTypeMethod1","void",
                Collections.singletonList(Sample.class.getName()), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(27, variables.get(0).getStartLine());
        assertEquals(27, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(1).getJNISignature());
        assertEquals(27, variables.get(1).getStartLine());
        assertEquals(27, variables.get(1).getEndLine());
    }

    @Test
    public void testParameterTypeWithSuperMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("parameterTypeMethod2","void",
                Collections.singletonList(Sample.class.getName()), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(28, variables.get(0).getStartLine());
        assertEquals(28, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(1).getJNISignature());
        assertEquals(28, variables.get(1).getStartLine());
        assertEquals(28, variables.get(1).getEndLine());
    }

    @Test
    public void testMethodWith2Parameters() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("methodWith2Parameters","void",
                List.of(Sample.class.getName(), "int"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 3);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(29, variables.get(0).getStartLine());
        assertEquals(29, variables.get(0).getEndLine());

        assertEquals("arg1", variables.get(1).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(1).getJNISignature());
        assertEquals(29, variables.get(1).getStartLine());
        assertEquals(29, variables.get(1).getEndLine());

        assertEquals("arg2", variables.get(2).getName());
        assertEquals("I", variables.get(2).getJNISignature());
        assertEquals(29, variables.get(2).getStartLine());
        assertEquals(29, variables.get(2).getEndLine());
    }

    @Test
    public void testMethodWithSingleArg() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("methodWithSingleArg","void",
                Collections.singletonList(Sample.class.getName()), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 2);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(30, variables.get(0).getStartLine());
        assertEquals(30, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(1).getJNISignature());
        assertEquals(30, variables.get(1).getStartLine());
        assertEquals(30, variables.get(1).getEndLine());
    }

    @Test
    public void testMethodWithTwoArgs() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("methodWithTwoArgs","void",
                List.of(Sample.class.getName(), "int"), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 3);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(32, variables.get(0).getStartLine());
        assertEquals(32, variables.get(0).getEndLine());

        assertEquals("arg1", variables.get(1).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(1).getJNISignature());
        assertEquals(32, variables.get(1).getStartLine());
        assertEquals(32, variables.get(1).getEndLine());

        assertEquals("arg2", variables.get(2).getName());
        assertEquals("I", variables.get(2).getJNISignature());
        assertEquals(32, variables.get(2).getStartLine());
        assertEquals(32, variables.get(2).getEndLine());
    }

    @Test
    public void testMethodWithArgAndSingleVariable() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("methodWithArgAndSingleVariable","void",
                Collections.singletonList(Sample.class.getName()), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 3);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(33, variables.get(0).getStartLine());
        assertEquals(35, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(1).getJNISignature());
        assertEquals(33, variables.get(1).getStartLine());
        assertEquals(35, variables.get(1).getEndLine());

        assertEquals("var1", variables.get(2).getName());
        assertEquals("I", variables.get(2).getJNISignature());
        assertEquals(34, variables.get(2).getStartLine());
        assertEquals(35, variables.get(2).getEndLine());
    }

    @Test
    public void testMethodWithArgAndTwoVariables() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("methodWithArgAndTwoVariables","void",
                Collections.singletonList(Sample.class.getName()), true);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertTrue(variables.size() == 4);
        assertEquals("this", variables.get(0).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(0).getJNISignature());
        assertEquals(36, variables.get(0).getStartLine());
        assertEquals(41, variables.get(0).getEndLine());

        assertEquals("arg", variables.get(1).getName());
        assertEquals("L" + Sample.class.getName().replace('.', '/') + ';',
                variables.get(1).getJNISignature());
        assertEquals(36, variables.get(1).getStartLine());
        assertEquals(41, variables.get(1).getEndLine());

        assertEquals("var1", variables.get(2).getName());
        assertEquals("I", variables.get(2).getJNISignature());
        assertEquals(37, variables.get(2).getStartLine());
        assertEquals(41, variables.get(2).getEndLine());

        assertEquals("var2", variables.get(3).getName());
        assertEquals("I", variables.get(3).getJNISignature());
        assertEquals(38, variables.get(3).getStartLine());
        assertEquals(40, variables.get(3).getEndLine());
    }

    @Test
    public void testStaticMethod() {
        var type = new ReferenceType(types, FILE_PATH, ClassName.fromGDB(Sample.class.getName()));
        var signature = new MethodSignature("staticMethod","void",
                Collections.emptyList(), false);
        var info = new MethodInfo(type, signature);
        var variables = info.getVariables();
        assertNotNull(variables);
        assertEquals(1, variables.size());
        assertEquals("var1", variables.get(0).getName());
        assertEquals("I", variables.get(0).getJNISignature());
        assertEquals(43, variables.get(0).getStartLine());
        assertEquals(45, variables.get(0).getEndLine());
    }
}
