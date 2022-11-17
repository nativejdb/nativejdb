# How to setup NativeJDB to debug your Quarkus application

## Before starting

### System requirements

The following guide is based on a Debian system and is presented as is. As such, it should work in other operating systems but there are no guarantees. Please submit an issue or a PR for any hiccups you find while using this walk-through with your own system.

### Prerequisites

You will need the following in your machine:

1. [`Docker or Docker Desktop`](https://docs.docker.com/desktop/install/ubuntu/): Will launch NativeJDB.
2. [`IntelliJ`](https://www.jetbrains.com/idea/download/): For debugging native exec via an IDE. VSCode and Eclipse support are on the [way](https://github.com/nativejdb/nativejdb/issues/60)
3. [`Java11`](https://adoptopenjdk.net/). Make sure that Java 11 is the one actively being used in the Path. You can check or change this in Ubuntu running `update-alternatives --config java`
4. [`Git`](https://help.github.com/articles/set-up-git/): As we will have to clone NativeJDB repo.
5. [`NativeJDB repository`](https://github.com/nativejdb/nativejdb): We will be using the repository directory directly.
6. [`GraalVM tar`](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.2.0/graalvm-ce-java11-linux-amd64-22.2.0.tar.gz): Needed by NativeJDB to work. Don't untar it!
7. Your own Quarkus project: You can also use any of the official examples. For example the [quickstart applications](https://github.com/quarkusio/quarkus-quickstarts) or the [Camel Quarkus examples](https://github.com/apache/camel-quarkus-examples). This walkthrough was built using the [timer-log application](https://github.com/apache/camel-quarkus-examples/tree/main/timer-log-main) in Camel Quarkus examples.

## Directory setup

First of all, we will need to setup NativeJDB with GraalVM.

1. Clone NativeJDB repository in any directory you'd like

`git clone https://github.com/nativejdb/nativejdb`

2. Copy GraalVM's tar file. Put it at the root of NativeJDB directory.

3. Run `make compile`

4. Run `make graalvm`

_If you find any errors when running either command, make sure you are actually using Java 11 and not some other version_

## Setting your Quarkus application to be debugged by NativeJDB

Next, we will have to provide NativeJDB with the executables and debug information necessary for it to run the application and send debug information back to the IDE.

1. First, compile your Quarkus application in native mode with debug information. 

`mvn package -Dnative -Dquarkus.native.debug.enabled -Dquarkus.native.additional-build-args=-H:-OmitInlinedMethodDebugLineInfo`

2. That will create several artifacts under the `/target` directory of your Quarkus project. You will need to copy the following files and directories:

- At root of `/target` directory:
    - `{appName}-runner`
    - `{appName}-runner.debug`
- Inside `/target/{appName}-native-image-source-jar` directory
    - `/lib` directory
    - `/sources` directory
    - `{appName}-runner.build_artifacts.txt`
    - `{appName}-runner.jar`

3. Copy all these files to the `/apps` directory inside NativeJDB directory

4. Rename the `/sources` directory to `{appName}-runnersources`
    - For simplicity, you can copy the name of the `{appName}-runner` executable. Then, just rename the `sources` directory and paste the executable name at the beginning (without removing the original `sources` name, but appending it at the beginning). That will produce the correct name

5. The only thing left is to launch NativeJDB. Log into a console inside NativeJDB directory and run the following command.

`make nativejdb CLASSNAME={appName}-runner ISQUARKUS=true`

_As a tip, you can copy the runner executable name and put it in CLASSNAME_

6. NativeJDB will start Docker and try to build and launch the image with the native application on it.

7. If everything goes right, the last message will be an address and a port. Take note of that port because that's how we connect to it.

Example of what you should see. The port would be 8082

`Waiting for debugger on: 98ba22bc0c6c:8082`

## Debugging your application

_So far this only works in IDEA. There are plans to bring support for VSCode and Eclipse. [Issue](https://github.com/nativejdb/nativejdb/issues/60)_

1. First, you will need to create a debug configuration of type `Remote JVM Debug`
2. Change the port number to whatever port NativeJDB told you it was listening on.
3. Change the host to `0.0.0.0`
4. Launch the debug configuration.

After launching the debug configuration you should be able to see NativeJDB reacting and showing more logs. After a bit of time, it'll start the JVM and your app should stop at any set breakpoints.
