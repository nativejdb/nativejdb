/*
 * Copyright (C) 2018 JetBrains s.r.o.
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License v2 with Classpath Exception.
 * The text of the license is available in the file LICENSE.TXT.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See LICENSE.TXT for more details.
 *
 * You may contact JetBrains s.r.o. at Na Hřebenech II 1718/10, 140 00 Prague,
 * Czech Republic or at legal@jetbrains.com.
 *
 * Copyright (C) 2022 IBM Corporation
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License v2 with Classpath Exception.
 * The text of the license is available in the file LICENSE.TXT.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See LICENSE.TXT for more details.
 */

package jdwp;

import gdb.mi.service.command.output.MIListThreadGroupsInfo;
import jdwp.jdi.ThreadGroupReferenceImpl;
import jdwp.jdi.ThreadReferenceImpl;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class JDWPThreadGroupReference {

    public static long threadGroupId = 100000;

    public static long getNewThreadGroupId() {
        return threadGroupId++;
    }

    public static Map<Long, MIListThreadGroupsInfo.IThreadGroupInfo> threadGroupById = new HashMap<>();
    public static Map<String, Long> threadGroupByName = new HashMap<>();


    static class ThreadGroupReference {
        static final int COMMAND_SET = 12;
        private ThreadGroupReference() {}  // hide constructor

        /**
         * Returns the thread group name.
         */
        static class Name implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
//                ThreadGroupReferenceImpl group = command.readThreadGroupReference();
//                answer.writeString(group.name());
//                long threadGroupId = command.readObjectRef();
//                String name = JDWPThreadGroupReference.threadGroupById.get(threadGroupId).getName();
//                answer.writeString(name);
                answer.writeString("Main Thread Group");
            }
        }

        /**
         * Returns the thread group, if any, which contains a given thread group.
         */
        static class Parent implements Command  {
            static final int COMMAND = 2;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ThreadGroupReferenceImpl group = command.readThreadGroupReference();
                answer.writeThreadGroupReference(group.parent());
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
                ThreadGroupReferenceImpl group = command.readThreadGroupReference();
                List<ThreadReferenceImpl> threads = group.threads();
                answer.writeInt(threads.size());
                for (ThreadReferenceImpl thread : threads) {
                    answer.writeThreadReference(thread);
                }

                List<ThreadGroupReferenceImpl> threadGroups = group.threadGroups();
                answer.writeInt(threadGroups.size());
                for (ThreadGroupReferenceImpl threadGroup : threadGroups) {
                    answer.writeThreadGroupReference(threadGroup);
                }
            }
        }
    }
}
