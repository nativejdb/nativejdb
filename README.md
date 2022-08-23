# NativeJDB - Java Debugger for Native Images

This is a project that bridges the gap between the [Java Debugger](https://docs.oracle.com/en/java/javase/11/tools/jdb.html) (JDB) framework available in modern IDEs and native debugging via [GNU Project debugger](https://www.sourceware.org/gdb/) (GDB).

## Background

[Quarkus](https://github.com/quarkusio/quarkus) is a cloud-native Java development framework. It allows Java code to be mapped to Kubernetes containers and compiled natively. 
Native compilation is useful for serverless computing, it avoids the overhead of running a JVM in containers and to execute serverless code directly. 
Today, natively compiled Java code can be debugged using GDB, which is a C/C++ debugger and is unfamiliar to Java developers. 

NativeJDB bridges the gap between the Java Debugger Framework available in modern IDEs and native debugging via GDB. It is part of our exploration to
improve developer experience and provide familiar tooling for Java cloud-native developers. NativeJDB is useful when the application behaves differently when natively compiled.

NativeJDB uses the [JDWP protocol](https://docs.oracle.com/en/java/javase/11/docs/specs/jdwp/jdwp-protocol.html) to communicate with an IDE, and acts as a server for it. It wraps a native executable controlled by GDB and makes it possible to control the native executable via JDWP commands. Because of this architecture, NativeJDB can in principle be used with any IDE, but has mostly been tested with IntelliJ. The current implementation of NativeJDB makes use of a "scaffolding VM", which is the same program being executed as a Java process. This allows NativeJDB to obtain certain static informations about the code that are harder to obtain from the GDB. In the future, this scaffolding can be removed.

## Features:

 - Editor opens up automatically after launch/attach with sourcecode showing lines
 - Works with familiar IDE like IntelliJ
 - Can work with VSCode, and Eclipse with some more exploration and development cycles
 - Works with [GraalVM's](https://www.graalvm.org/) natively compiled images
 - Can work with [Qbicc's](https://github.com/qbicc/qbicc) natively compiled images with some more exploration and development cycles
 - Debugging features using the IDE's Debug Console itself: 
    - Suspend / Resume
    - Set Breakpoints (Insert/Enable)
    - Clear Breakpoints (Delete/Disable)
    - Step Over / Step Into / Step Return
    - Stack Frames information in IDE debugger pane
    - Primitive variables (Local + Static) values (Work-in-progress)
    - View of assembly code within a stack frame
    - View of variables optimized out by native-image compilation
    - Thread info

## Requirements

 - GraalVM Community Edition for Java 11
 - Docker Desktop
 - IntelliJ IDE

## Published guides:

:pencil2: https://quarkus.io/blog/nativejdb-debugger-for-native-images/ :pencil2:

:movie_camera: https://www.youtube.com/watch?v=LhTR_ECSaAo :movie_camera:

:movie_camera: https://www.youtube.com/watch?v=_9ejxCtRAdg :movie_camera:

## Getting Started 

### Pre-requisites for NativeJDB

You must install these tools:

1. [`git`](https://help.github.com/articles/set-up-git/): For source control
2. [`IntelliJ`](https://www.jetbrains.com/idea/download/): For debugging native exec via an IDE
3. [`Docker Desktop`](https://www.docker.com/products/docker-desktop): For running NativeJDB in a container

In addition, you must also setup another repository that contains example applications to run via this NativeJDB server:

4. [`nativejdbExamples`](https://github.com/nativejdb/nativejdbExamples): Follow instructions in its [`README`](https://github.com/nativejdb/nativejdbExamples#readme) to generate a native image executable before proceeding

### Compiling your NativeJDB code

To build the NativeJDB, run:

```
make compile
```

This produces a jar file `NativeJDB-1.0-SNAPSHOT.jar` under `/target` which is used in Docker container.

### Running your NativeJDB code

#### 1. Download GraalVM

To generate a native executable within the Linux environment in the Docker container, you will need to download graalvm binary from https://github.com/graalvm/graalvm-ce-builds/releases and untar `graalvm-ce-java11-linux-amd64-*.tar.gz` into the same `NativeJDB` directory.

```
 make graalvm
```

#### 2. Run Docker container to start NativeJDB server to debug native executable (debuggee):

- Start Docker Desktop

- Run the following command via a terminal to deploy docker container running NativeJDB server to start debugging:

For an already pre-built native image using [`nativejdbExamples`](https://github.com/nativejdb/nativejdbExamples) (like `Hello`), run this (`ISQUARKUS` arg is defaulted to `false`):
```
make nativejdb CLASSNAME=Hello 
```

For any other application that has your own pre-built native executable and debug sources,
add the executable and debug sources to [/apps](./apps) directory and then pass the following input args to make target (`ISQUARKUS` arg is defaulted to `false`):
```
make nativejdb CLASSNAME={nameofjarfile} NATIVEEXEC=apps/{nameofnativeexec} NATIVESRC=apps/{directorynameofdebugsources}
```

For a Quarkus application that has its executable and debug sources in [/apps](./apps) directory (like `getting-started-1.0.0-SNAPSHOT-runner`), pass the following input args to make target, along with setting `ISQUARKUS` arg to `true`:
```
make nativejdb CLASSNAME=getting-started-1.0.0-SNAPSHOT-runner ISQUARKUS=true
```

This will start `nativejdb` in a running Docker container.

#### 3. Connect IntelliJ Debugger to running `nativejdb` Docker container:

To connect an IDE debugger to NativeJDB, the user needs to create a Debug configuration and attach to NativeJDB on the appropriate port.

Set breakpoints in the source code file for your example application in the `nativejdbExamples` project (for example: [nativejdbExamples/src/Hello/Hello.java](https://github.com/nativejdb/nativejdbExamples/blob/main/src/Hello/Hello.java)

On IntelliJ, from the `nativeJDBExamples` open project: Run ---> Edit Configurations --> Remote JVM Debug --> [nativejdbExamples/.run/Hello](https://github.com/nativejdb/nativejdbExamples/blob/main/.run/Hello.run.xml)


###  Additional options to maximize your experience 

- Customize the number of assembly instructions using `ASMLINE` flag. 

When running `make nativejdb`, specify the `ASMLINE=<num>` flag to modify the number of assembly instructions displayed. If nothing is passed in, it will be defaulted to 10.

Example: `make nativejdb CLASSNAME=HelloNested ASMLINE=20`

## Current limitations:

 - Short running programs need a thread sleep (so program does not end before NativeJDB has time to start a scaffolding VM and attach to it)
 - Hot Code Replace not possible
 - Known issue: breakpoints in loops work only once (related to [this](https://github.com/oracle/graal/issues/4379) graalvm issue)
 - Known issue: step operation sometimes continues instead



## Contributing

If you are interested in contributing, see [CONTRIBUTING.md](./CONTRIBUTING.md) for contributing information

## References

https://quarkus.io/guides/building-native-image#debugging-native-executable

https://sourceware.org/gdb/onlinedocs/gdb/GDB_002fMI.html

## Contact Us

To discuss more about this project, please open an issue.

## License

This project consists of a frontend package with a GPL-EC license (code derived from:  https://github.com/JetBrains/jdk-sa-jdwp) 
and a backend package with an EPLv2 license (code derived from: https://github.com/eclipse-cdt/cdt).
The top-level project has an MIT license.
