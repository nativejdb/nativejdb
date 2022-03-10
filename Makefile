JDWPSERVICE := nativejdb
NATIVEIMAGE := nativeimage
CLASSNAME := Hello

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
	awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

graalvm: ## Untar graalvm binary using downloaded tarfile in this current directory.
	mkdir -p graalvm
	tar -xzf graalvm-ce-java11-linux-amd64-*.tar.gz -C graalvm --strip-components=1

nativejdb: ## Running JDWPServer to debug a native image executable.
	docker stop $(JDWPSERVICE) && docker rm $(JDWPSERVICE) || exit 0;
	docker build -t $(JDWPSERVICE) --build-arg CLASS_NAME=$(CLASSNAME) -f Dockerfile .
	docker run --privileged --name $(JDWPSERVICE) -v $(PWD)/apps:/jdwp/apps -p 8080:8080 -p 8081:8081 $(JDWPSERVICE)

nativeimage: ## Running a NativeImage container that generates native image and debug sources for $CLASS_NAME app.
	docker stop $(NATIVEIMAGE) && docker rm $(NATIVEIMAGE) || exit 0;
	docker build -t $(NATIVEIMAGE) --build-arg CLASS_NAME=$(CLASSNAME) -f Dockerfile.native .
	docker run --privileged --name $(NATIVEIMAGE) -v $(PWD)/apps:/jdwp/apps $(NATIVEIMAGE)

exec: ## Execing into NativeJDB container.
	docker exec -it $(JDWPSERVICE) /bin/bash

build: ## Building NativeJDB image.
	docker build -t $(JDWPSERVICE) --build-arg REBUILD_EXEC=$(REBUILD) .

run: ## Starting NativeJDB container.
	docker run --privileged --name $(JDWPSERVICE) -v $(PWD)/apps:/jdwp/apps -p 8080:8080 -p 8081:8081 $(JDWPSERVICE)

stop: ## Stopping NativeJDB container.
	docker stop $(JDWPSERVICE) && docker rm $(JDWPSERVICE) || exit 0;

clean: ## Clean docker images.
	docker system prune --volumes --force

login: ## Login to Docker Hub.
	docker login -u $(DOCKER_USER) -p $(DOCKER_PASS)
