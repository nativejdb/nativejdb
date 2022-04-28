# NativeJDB - Java Debugger for native images

This is a project that bridges the gap between the [Java Debugger](https://docs.oracle.com/en/java/javase/11/tools/jdb.html) (JDB) framework available in modern IDEs and native debugging via [GNU Project debugger](https://www.sourceware.org/gdb/) (GDB).

## Background

Quarkus is a cloud-native Java development framework. It allows Java code to be mapped to Kubernetes containers and compiled natively. 
Native compilation is useful for serverless computing, it avoids the overhead of running a JVM in containers and to execute serverless code directly. 
Today, natively compiled Java code can be debugged using GDB, which is a C/C++ debugger and is unfamiliar to Java developers. 
NativeJDB bridges the gap between the Java Debugger Framework available in modern IDEs and native debugging via GDB. It is part of our exploration to
improve developer experience and provide familiar tooling for Java cloud-native developers. NativeJDB is useful when the application behaves differently when
natively compiled. 

Here's a list of features:

 - Editor opens up automatically after launch/attach with sourcecode showing lines
 - Works with various familiar IDEs like IntelliJ, VSCode, and soon on Eclipse (Work-in-progress)
 - Works with GraalVM's natively compiled images and Qbicc's natively compiled images (Work-in-progress)
 - Debugging features using the IDE's Debug Console itself: 
    - Suspend / Resume
    - Set Breakpoints (Insert/Enable)
    - Clear Breakpoints (Delete/Disable)
    - Step Over / Step Into / Step Return
    - Stack Frames
    - Variables (Local + Static) values (Work-in-progress)
    - Thread info
   
See more details [here](./src/main/java/README.md)
    
Limitations today:

 - Single threaded programs only
 - Short running programs need a thread sleep (so program does not end before NativeJDB attaches)
 - Hot Code Replace not possible

## Requirements

 - JDK (Version 11) or GraalVM Community Edition for Java 11
 - Docker Desktop
 - IDE

## Getting Started 

### Run NativeJDB with our provided [Hello](Hello) example or with your own pre-built native executable:

Follow steps in [DEVELOPMENT.md](./DEVELOPMENT.md).

## Contributing

If you are interested in contributing, see [CONTRIBUTING.md](./CONTRIBUTING.md) for contributing information

## References

https://quarkus.io/guides/building-native-image#debugging-native-executable

https://sourceware.org/gdb/onlinedocs/gdb/GDB_002fMI.html

## Contact Us

To discuss more about this project, please open an issue. More contact info TBD.

## License

This project consists of a frontend package with a GPL-EC license (code derived from:  https://github.com/JetBrains/jdk-sa-jdwp) 
and a backend package with an EPLv2 license (code derived from: https://github.com/eclipse-cdt/cdt).
The top-level project has an MIT license.
