# Hello

Hello.java is an example Java code used for debugging with NativeJDB.

This directory contains the following:
- **[Hello.class](Hello.class)** Compiled in a Linux environment (within Docker container). Check [shell script](../startProcesses.sh)
- **[debugeeImg](debugeeImg)** Native executable for Hello generated in a Linux environment (within Docker container). Check [shell script](../startProcesses.sh)
- **[sources](sources)** Debug symbols generated for the native executable in a Linux environment (within Docker container) to be used with GDB. Run with `native-image -g`
- **[debugeeImg.build_artifacts.txt](debugeeImg.build_artifacts.txt)** Build artifact
