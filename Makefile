JDWPSERVICE := nativejdb
CLASSNAME := Hello
NATIVEEXEC := apps/${CLASSNAME}
NATIVESRC := apps/${CLASSNAME}sources
ISQUARKUS := false
ASMLINE := 10

all: nativejdb

##@ General

# The help target prints out all targets with their descriptions organized
# beneath their categories. The categories are represented by '##@' and the
# target descriptions by '##'. The awk commands is responsible for reading the
# entire set of makefiles included in this invocation, looking for lines of the
# file as xyz: ## something, and then pretty-format the target and help. Then,
# if there's a line with ##@ something, that gets pretty-printed as a category.
# More info on the usage of ANSI control characters for terminal formatting:
# https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_parameters
# More info on the awk command:
# http://linuxcommand.org/lc3_adv_awk.php

help: ## Display this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

graalvm: ## Untar graalvm binary using downloaded tarfile in this current directory.
	mkdir -p graalvm
	tar -xzf graalvm-ce-java11-linux-amd64-*.tar.gz -C graalvm --strip-components=1

compile: ## Build the NativeJDB source code.
	mvn clean compile package

nativejdb: ## Run a JDWPServer to debug a native image executable for CLASS_NAME app.
	docker stop $(JDWPSERVICE) && docker rm $(JDWPSERVICE) || exit 0;
	docker build -t $(JDWPSERVICE) --build-arg CLASS_NAME=$(CLASSNAME) --build-arg NATIVE_EXEC=${NATIVEEXEC} --build-arg NATIVE_SRC=${NATIVESRC} --build-arg IS_QUARKUS=$(ISQUARKUS) --build-arg ASM_LINE=$(ASMLINE) -f Dockerfile .
	docker run --privileged --name $(JDWPSERVICE) -v $(PWD)/apps:/jdwp/apps -p 8082:8082 -p 8081:8081 $(JDWPSERVICE)

nativejdbqbicc: ## Run a JDWPServer to debug a native image executable for CLASS_NAME app.
	docker stop $(JDWPSERVICE) && docker rm $(JDWPSERVICE) || exit 0;
	docker build -t $(JDWPSERVICE) --build-arg CLASS_NAME=$(CLASSNAME) --build-arg NATIVE_EXEC=${NATIVEEXEC}qbicc --build-arg NATIVE_SRC=${NATIVESRC} --build-arg IS_QUARKUS=$(ISQUARKUS) --build-arg ASM_LINE=$(ASMLINE) -f Dockerfile.qbicc .
	docker run --privileged --name $(JDWPSERVICE) -v $(PWD)/apps:/jdwp/apps -p 8082:8082 -p 8081:8081 $(JDWPSERVICE)

exec: ## Exec into NativeJDB container.
	docker exec -it $(JDWPSERVICE) /bin/bash

build: ## Build NativeJDB image.
	docker build -t $(JDWPSERVICE) --build-arg CLASS_NAME=$(CLASSNAME) --build-arg NATIVE_EXEC=${NATIVEEXEC} --build-arg NATIVE_SRC=${NATIVESRC} --build-arg IS_QUARKUS=$(ISQUARKUS) -f Dockerfile .

run: ## Start NativeJDB container.
	docker run --privileged --name $(JDWPSERVICE) -v $(PWD)/apps:/jdwp/apps -p 8082:8082 -p 8081:8081 $(JDWPSERVICE)

stop: ## Stop NativeJDB container.
	docker stop $(JDWPSERVICE) && docker rm $(JDWPSERVICE) || exit 0;

clean: ## Clean docker images.
	docker system prune --volumes --force

login: ## Login to Docker Hub.
	docker login -u $(DOCKER_USER) -p $(DOCKER_PASS)
