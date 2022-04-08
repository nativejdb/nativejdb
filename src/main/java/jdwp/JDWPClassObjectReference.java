package jdwp;

import jdwp.jdi.ClassObjectReferenceImpl;
import jdwp.jdi.ReferenceTypeImpl;

public class JDWPClassObjectReference {

    static class ClassObjectReference {
        static final int COMMAND_SET = 17;
        private ClassObjectReference() {}  // hide constructor

        /**
         * Returns the reference type reflected by this class object.
         */
        static class ReflectedType implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
//                ClassObjectReferenceImpl reference = command.readClassObjectReference();
//                ReferenceTypeImpl type = reference.reflectedType();
//                if (type == null) {
//                    answer.writeByte(JDWP.TypeTag.CLASS);
//                    answer.writeClassRef(0);
//                }
//                else {
//                    answer.writeByte(type.tag());
//                    answer.writeClassRef(type.uniqueID());
//                }
            }
        }
    }
}
