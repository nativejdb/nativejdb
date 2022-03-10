public class HelloNew {
    public static abstract class Greeter {
        public static Greeter greeter(String[] args) {
            if (args.length == 0) {
                return new DefaultGreeter();
            } else if (args.length == 1) {
                return new NamedGreeter(args[0]);
            } else {
                throw new RuntimeException("zero or one args expected");
            }

        }

        public abstract void greet();

    }


    public static class DefaultGreeter extends Greeter {
        public void greet() {
            System.out.println("Hello World!");

        }

    }

    public static class NamedGreeter extends Greeter {
        private String name;

        public NamedGreeter(String name) {
            this.name = name;

        }

        public void greet() {
            System.out.println("Hello " + name);
        }

    }

    public static void main(String[] args){
        Greeter greeter = Greeter.greeter(args);
        greeter.greet();
        System.exit(0);
    }
}



