# Development

This document explains how to setup a development environment so you can get started [contributing](./CONTRIBUTING.md) to `NativeJDB`.

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

### (Dev mode) Or Checkout your NativeJDB fork (IF NOT CLONED ALREADY)

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

For existing example application (Hello), run this:
```
make nativejdb 
```

For any other application, pass the class name as input arg to make target:
```
make nativejdb CLASSNAME=****
```

- Run the following command via a terminal to ssh into docker container:

```
make exec
```

- Run the following command via a terminal to stop and remove existing docker container:

```
make stop
```

### Connect IntelliJ Debugger to running Docker container:

On IntelliJ: Run ---> Remote JVM Debug --> [Hello](./.run/Hello.run.xml)

To debug NativeJDB code itself: Run ---> Remote JVM Debug --> [JDWPDebug](./.run/JDWPDebug.run.xml)



## Contributing

Please check [contribution guidelines](./CONTRIBUTING.md).
