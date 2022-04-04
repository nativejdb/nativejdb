package jdwp;

import jdwp.jdi.ObjectReferenceImpl;
import jdwp.jdi.StringReferenceImpl;

public class JDWPStringReference {
    static class StringReference {
        static final int COMMAND_SET = 10;
        private StringReference() {}  // hide constructor

        /**
         * Returns the characters contained in the string.
         */
        static class Value implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ObjectReferenceImpl objectReference = command.readObjectReference();
                if (objectReference instanceof StringReferenceImpl) {
                    answer.writeString(((StringReferenceImpl) objectReference).value());
                }
                else {
                    answer.pkt.errorCode = JDWP.Error.INVALID_STRING;
                }
            }
        }
    }
}
