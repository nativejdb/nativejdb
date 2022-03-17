import java.io.*;

public class Hello{
        public static void main(String[] args) throws InterruptedException {
                BufferedOutputStream writer = null;
                try {
                        Thread.sleep(10000);
                        writer = new BufferedOutputStream(new FileOutputStream("hello.txt"));
                        for (int j = 0; j < 10000; j++) {
                          writer.write(String.valueOf(j).getBytes());
                          writer.write("\n".getBytes());
                          hello(j);
                          writer.flush();
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                } finally {
                        if (writer != null) {
                                try {
                                        writer.close();
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }
                        }
                }
        }

        private static String hello() {
                return "hello!!";
        }
        private static  String hello(int i) {
                String myString = "Hello " + i;
                return myString;
        }
}