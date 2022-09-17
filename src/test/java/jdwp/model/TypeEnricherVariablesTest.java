package jdwp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.nio.file.Paths;
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "booleanMethod(boolean)", "booleanMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "booleanMethod(java.lang.Boolean *)", "booleanMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "byteMethod(byte)", "byteMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "byteMethod(java.lang.Byte *)", "byteMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "charMethod(char)", "charMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "charMethod(java.lang.Character *)", "charMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "shortMethod(short)", "shortMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "shortMethod(java.lang.Short *)", "shortMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "intMethod(int)", "intMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "intMethod(java.lang.Integer *)", "intMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "longMethod(long)", "longMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "longMethod(java.lang.Long *)", "longMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "floatMethod(float)", "floatMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "floatMethod(java.lang.Float *)", "floatMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "doubleMethod(double)", "doubleMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "doubleMethod(java.lang.Double *)", "doubleMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "objectMethod(java.lang.Object *)", "objectMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "classParameterTypeMethod(java.lang.Object *)", "classParameterTypeMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "methodParameterTypeMethod(java.lang.Object *)", "methodParameterTypeMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "parameterTypeMethod(" + Sample.class.getName() + " *)",
                "parameterTypeMethod");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "parameterTypeMethod1(" + Sample.class.getName() + " *)",
                "parameterTypeMethod1");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "parameterTypeMethod2(" + Sample.class.getName() + " *)",
                "parameterTypeMethod2");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "methodWith2Parameters(" + Sample.class.getName() + " *, int)",
                "methodWith2Parameters");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "methodWithSingleArg(" + Sample.class.getName() + " *)",
                "methodWithSingleArg");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "methodWithTwoArgs(" + Sample.class.getName() + " *, int)",
                "methodWithTwoArgs");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "methodWithArgAndSingleVariable(" + Sample.class.getName() + " *)",
                "methodWithArgAndSingleVariable");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "methodWithArgAndTwoVariables(" + Sample.class.getName() + " *)",
                "methodWithArgAndTwoVariables");
        info.removeModifier(Modifier.STATIC);
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
        ReferenceType type = new ReferenceType(types, FILE_PATH, Sample.class.getName());
        MethodInfo info = new MethodInfo(type, "staticMethod()",
                "staticMethod");
        var variables = info.getVariables();
        assertNotNull(variables);
        assertEquals(1, variables.size());
        assertEquals("var1", variables.get(0).getName());
        assertEquals("I", variables.get(0).getJNISignature());
        assertEquals(43, variables.get(0).getStartLine());
        assertEquals(45, variables.get(0).getEndLine());
    }
}
