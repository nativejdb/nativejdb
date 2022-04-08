package jdwp;

import jdwp.jdi.ThreadGroupReferenceImpl;
import jdwp.jdi.ThreadReferenceImpl;

import java.util.List;

public class JDWPThreadGroupReference {
    static class ThreadGroupReference {
        static final int COMMAND_SET = 12;
        private ThreadGroupReference() {}  // hide constructor

        /**
         * Returns the thread group name.
         */
        static class Name implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ThreadGroupReferenceImpl group = command.readThreadGroupReference();
                answer.writeString(group.name());
            }
        }

        /**
         * Returns the thread group, if any, which contains a given thread group.
         */
        static class Parent implements Command  {
            static final int COMMAND = 2;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
//                ThreadGroupReferenceImpl group = command.readThreadGroupReference();
//                answer.writeThreadGroupReference(group.parent());
            }
        }

        /**
         * Returns the live threads and active thread groups directly contained
         * in this thread group. Threads and thread groups in child
         * thread groups are not included.
         * A thread is alive if it has been started and has not yet been stopped.
         * See <a href=../../../api/java/lang/ThreadGroup.html>java.lang.ThreadGroup </a>
         * for information about active ThreadGrouanswer.
         */
        static class Children implements Command  {
            static final int COMMAND = 3;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
//                ThreadGroupReferenceImpl group = command.readThreadGroupReference();
//                List<ThreadReferenceImpl> threads = group.threads();
//                answer.writeInt(threads.size());
//                for (ThreadReferenceImpl thread : threads) {
//                    answer.writeThreadReference(thread);
//                }
//
//                List<ThreadGroupReferenceImpl> threadGroups = group.threadGroups();
//                answer.writeInt(threadGroups.size());
//                for (ThreadGroupReferenceImpl threadGroup : threadGroups) {
//                    answer.writeThreadGroupReference(threadGroup);
//                }
            }
        }
    }
}
