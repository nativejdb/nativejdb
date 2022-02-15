# NativeJDB

This is a project that bridges the gap between the [Java Debugger](https://docs.oracle.com/en/java/javase/11/tools/jdb.html) (JDB) framework available in modern IDEs and native debugging via [GNU Project debugger](https://www.sourceware.org/gdb/) (GDB).

## Background

Quarkus is a cloud-native Java development framework. It allows Java code to be mapped to Kubernetes containers and compiled natively. 
Native compilation is useful for serverless computing, it avoids the overhead of running a JVM in containers and to execute serverless code directly. 
Today, natively compiled Java code can be debugged using GDB, which is a C/C++ debugger and is unfamiliar to Java developers. 
NativeJDB bridges the gap between the Java Debugger Framework available in modern IDEs and native debugging via GDB. It is part of our exploration to
improve developer experience and provide familiar tooling for Java cloud-native developers.

## Getting Started 

### Run NativeJDB with our provided [Hello](Hello) example:

Follow steps in [DEVELOPMENT.md](./DEVELOPMENT.md).

### Run NativeJDB with your own pre-built native executable [Under construction]:

Follow steps in [DEVELOPMENT.md](./DEVELOPMENT.md).

Set `DEBUGEE_ARG` input argument for Dockerfile with name of directory containing the native executable and all the debug source files. Native executable file must be named `debugeeImg`.
More instructions on this coming soon.

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
