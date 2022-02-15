import java.io.*;

public class Hello{
        public static void main(String[] args) {
                BufferedOutputStream writer = null;
                try {
                      while (true) {
                        writer = new BufferedOutputStream(new FileOutputStream("hello.txt"));
                        for (int j = 0; j < 10000; j++) {
                          writer.write(String.valueOf(j).getBytes());
                          writer.write("\n".getBytes());
                          writer.flush();
                        }
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

}