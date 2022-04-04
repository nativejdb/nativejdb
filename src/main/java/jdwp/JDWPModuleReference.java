package jdwp;

public class JDWPModuleReference {
    static class ModuleReference {
        static final int COMMAND_SET = 18;
        private ModuleReference() {}  // hide constructor

        /**
         * Returns the name of this module.
         * <p>Since JDWP version 9.
         */
        static class Name implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
            }
        }

        /**
         * Returns the class loader of this module.
         * <p>Since JDWP version 9.
         */
        static class ClassLoader implements Command  {
            static final int COMMAND = 2;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
            }
        }
    }
}
