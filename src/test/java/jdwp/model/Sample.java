package jdwp.model;

public class Sample<T> {
    public void booleanMethod(boolean arg) {}
    public void booleanMethod(Boolean arg) {}
    public void byteMethod(byte arg) {}
    public void byteMethod(Byte arg) {}
    public void charMethod(char arg) {}
    public void charMethod(Character arg) {}
    public void shortMethod(short arg) {}
    public void shortMethod(Short arg) {}
    public void intMethod(int arg) {}
    public void intMethod(Integer arg) {}
    public void longMethod(long arg) {}
    public void longMethod(Long arg) {}
    public void floatMethod(float arg) {}
    public void floatMethod(Float arg) {}
    public void doubleMethod(double arg) {}
    public void doubleMethod(Double arg) {}
    public void objectMethod(Object arg) {}
    public void object1Method(Object arg) {

    }
    public void classParameterTypeMethod(T arg) {}
    public <M> void methodParameterTypeMethod(M arg) {}
    public void parameterTypeMethod(Sample<Integer> arg) {}
    public void parameterTypeMethod1(Sample<? extends Integer> arg) {}
    public void parameterTypeMethod2(Sample<? super Integer> arg) {}
    public void methodWith2Parameters(Sample<Integer> arg1, int arg2) {}
    public void methodWithSingleArg(Sample<Integer> arg) {}
    public void methodWithTwoArgs(Sample<Integer> arg1,
                                  int arg2) {}
    public void methodWithArgAndSingleVariable(Sample<Integer> arg) {
        var var1 = 0;
    }
    public void methodWithArgAndTwoVariables(Sample<Integer> arg) {
        var var1 = 0;
        for(var var2=0; var2 < var1;++var2) {
            System.out.println(var2);
        }
    }
    public static void staticMethod() {
        var var1 = 0;
        System.out.println(var1);
    }
}
