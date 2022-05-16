# NativeJDB - Java Debugger for Native Images

This is a project that bridges the gap between the [Java Debugger](https://docs.oracle.com/en/java/javase/11/tools/jdb.html) (JDB) framework available in modern IDEs and native debugging via [GNU Project debugger](https://www.sourceware.org/gdb/) (GDB).

## Background

Quarkus is a cloud-native Java development framework. It allows Java code to be mapped to Kubernetes containers and compiled natively. 
Native compilation is useful for serverless computing, it avoids the overhead of running a JVM in containers and to execute serverless code directly. 
Today, natively compiled Java code can be debugged using GDB, which is a C/C++ debugger and is unfamiliar to Java developers. 

NativeJDB bridges the gap between the Java Debugger Framework available in modern IDEs and native debugging via GDB. It is part of our exploration to
improve developer experience and provide familiar tooling for Java cloud-native developers. NativeJDB is useful when the application behaves differently when
natively compiled.

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
    - Stack Frames
    - Variables (Local + Static) values (Work-in-progress)
    - Thread info
    
## Current limitations:

 - Single threaded programs only
 - Short running programs need a thread sleep (so program does not end before NativeJDB attaches)
 - Hot Code Replace not possible

## Requirements

 - GraalVM Community Edition for Java 11
 - Docker Desktop
 - IDE

## Demo

:movie_camera: https://ibm.box.com/v/nativejdb-demo :movie_camera:

## Blog Post

:construction: Coming soon :construction:

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

For an already pre-built native image using [`nativejdbExamples`](https://github.com/nativejdb/nativejdbExamples) (like Hello), run this:
```
make nativejdb CLASSNAME=Hello 
```

For any other application that has your own pre-built native executable and debug sources,
add the executable and debug sources to [/apps](./apps) directory and then pass the following input args to make target:

```
make nativejdb CLASSNAME={nameofjarfile} NATIVEEXEC=apps/{nameofnativeexec} NATIVESRC=apps/{directorynameofdebugsources}
```

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
