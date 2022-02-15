# Development

This document explains how to setup a development environment so you can get started [contributing](./CONTRIBUTING.md) to `NativeJDB`.

## Getting started

### Pre-requisites for NativeJDB

You must install these tools:

1. [`git`](https://help.github.com/articles/set-up-git/): For source control
2. [`IntelliJ`](https://www.jetbrains.com/idea/download/): For debugging native exec via an IDE
3. [`Docker Desktop`](https://www.docker.com/products/docker-desktop): For running NativeJDB in a container

### Checkout your NativeJDB fork

To check out this repository:

1. Create your own [fork of this repo](https://help.github.com/articles/fork-a-repo/)
2. Clone it to your machine:

```shell
git clone git@github.com:${YOUR_GITHUB_USERNAME}/NativeJDB.git
cd NativeJDB
git remote add upstream https://github.com/nativejdb/NativeJDB.git
git remote set-url --push upstream no_push
```

_Adding the `upstream` remote sets you up nicely for regularly
[syncing your fork](https://help.github.com/articles/syncing-a-fork/)._

### Compiling your NativeJDB code

To build the NativeJDB, run:
```
mvn clean compile package
```

This produces a jar file `NativeJDB-1.0-SNAPSHOT.jar` under `/target` which is used in Docker container.

### Running your NativeJDB code

#### 1. Download GraalVM

To generate a native executable within the Linux environment in the Docker container, you will need to download graalvm binary from https://github.com/graalvm/graalvm-ce-builds/releases and untar `graalvm-ce-java11-linux-amd64-*.tar.gz` into the same `NativeJDB` directory.

```
 mkdir graalvm
 tar -xzf graalvm-ce-java11-linux-amd64-*.tar.gz -C graalvm --strip-components=1
```

#### 2. Deploy Docker container containing NativeJDB and native executable (debuggee):

- Start Docker Desktop
  
- Run either of the following command via a terminal to build docker image (`REBUILD_EXEC` is a Dockerfile input knob to control re-building of a native image executable (generation takes a few mins):
  
a. Build with the existing [Hello](./Hello) native executable and debug sources

```
docker build -t nativejdb --build-arg REBUILD_EXEC="no" .
```

OR

b. Build with a newly generated native executable for Hello and generate its debug sources

```
docker build -t nativejdb --build-arg REBUILD_EXEC="yes" .
```

(Delete any old exited nativejdb containers via Docker Dekstop GUI before next step)

- Run the following command via a terminal to deploy docker container:

```
docker run --privileged --name nativejdb -v $PWD/Hello:/jdwp/Hello -p 8080:8080 -p 8081:8081 nativejdb
```

- Run the following command via a terminal to ssh into docker container:

```
docker exec -it nativejdb /bin/bash
```

### Connect IntelliJ Debugger to running Docker container:

On IntelliJ: Run ---> Remote JVM Debug --> [Hello](./.run/Hello.run.xml)

To debug NativeJDB code itself: Run ---> Remote JVM Debug --> [JDWPDebug](./.run/JDWPDebug.run.xml)



## Contributing

Please check [contribution guidelines](./CONTRIBUTING.md).
