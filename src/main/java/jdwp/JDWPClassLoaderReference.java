package jdwp;

import jdwp.jdi.ClassLoaderReferenceImpl;
import jdwp.jdi.ReferenceTypeImpl;

import java.util.List;

public class JDWPClassLoaderReference {
    static class ClassLoaderReference {
        static final int COMMAND_SET = 14;
        private ClassLoaderReference() {}  // hide constructor

        /**
         * Returns a list of all classes which this class loader has
         * been requested to load. This class loader is considered to be
         * an <i>initiating</i> class loader for each class in the returned
         * list. The list contains each
         * reference type defined by this loader and any types for which
         * loading was delegated by this class loader to another class loader.
         * <p>
         * The visible class list has useful properties with respect to
         * the type namespace. A particular type name will occur at most
         * once in the list. Each field or variable declared with that
         * type name in a class defined by
         * this class loader must be resolved to that single type.
         * <p>
         * No ordering of the returned list is guaranteed.
         */
        static class VisibleClasses implements Command  {
            static final int COMMAND = 1;

            static class ClassInfo {

                public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ClassLoaderReferenceImpl classLoaderReference = command.readClassLoaderReference();
                List<ReferenceTypeImpl> visibleClasses = classLoaderReference.visibleClasses();
                answer.writeInt(visibleClasses.size());
                for (ReferenceTypeImpl visibleClass : visibleClasses) {
                    answer.writeByte(visibleClass.tag());
                    answer.writeClassRef(visibleClass.uniqueID());
                }
            }
        }
    }
}
