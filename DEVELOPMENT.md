# Development

This document explains how to set up a development environment, so you can get started [contributing](./CONTRIBUTING.md) to `NativeJDB`.

## Getting started

### Pre-requisites for NativeJDB

You must install these tools:

1. [`git`](https://help.github.com/articles/set-up-git/): For source control
2. [`IntelliJ`](https://www.jetbrains.com/idea/download/): For debugging native exec via an IDE
3. [`Docker Desktop`](https://www.docker.com/products/docker-desktop): For running NativeJDB in a container

In addition, you must also setup another repository that contains example applications to run via this NativeJDB server:

4. [`nativejdbExamples`](https://github.com/nativejdb/nativejdbExamples): Follow instructions in its [`README`](https://github.com/nativejdb/nativejdbExamples#readme) to generate a native image executable before proceeding

### (User mode) Checkout NativeJDB without a fork (IF NOT CLONED ALREADY),

1. Clone it to your machine:

```shell
git clone git@github.com:nativejdb/nativejdb.git
cd nativejdb
```

### (Dev mode) Checkout your NativeJDB fork (IF NOT CLONED ALREADY)

To check out this repository:

1. Create your own [fork of this repo](https://help.github.com/articles/fork-a-repo/)
2. Clone it to your machine:

```shell
git clone git@github.com:${YOUR_GITHUB_USERNAME}/nativejdb.git
cd nativejdb
git remote add upstream https://github.com/nativejdb/nativejdb.git
git remote set-url --push upstream no_push
```

_Adding the `upstream` remote sets you up nicely for regularly
[syncing your fork](https://help.github.com/articles/syncing-a-fork/)._

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

- Run the following command via a terminal to ssh into docker container:

```
make exec
```

- Run the following command via a terminal to stop and remove existing docker container:

```
make stop
```

### (Dev mode) Connect IntelliJ Debugger to running Docker container:

To debug NativeJDB debugger code itself: Run ---> Remote JVM Debug --> [JDWPDebug](./.run/JDWPDebug.run.xml)

## Developer tools: Wireshark and jdwptracer

Wireshark is an open-source packet analyzer which has the capability to sniff network traffic, decode packets, and interpret them. 
To be able to contribute to this project, it is important to understand how JDWP works in a real-world setting.
Wireshark is thus helpful in providing the tools to break down the command and reply packets sent between the debugger and debuggee.

[jdwptracer](https://github.com/jeffmaury/jdwptracer) is a proxy that traces JDWP packets like Wireshark. It provides additional information 
that Wireshark does not, specifically dissecting the data portion for JDWP commands. 

Check out 
* this [section](tools/README.md#developer-tool-wireshark) for guidelines and resources for Wireshark, and 
* this [section](tools/README.md#developer-tool-jdwptracer) on using jdwptracer


## Contributing

Please check [contribution guidelines](./CONTRIBUTING.md).
