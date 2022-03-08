SERVICE := nativejdb
REBUILD := no

build:
	$(info Make: Building "$(SERVICE)" image.)
	docker build -t $(SERVICE) --build-arg REBUILD_EXEC=$(REBUILD) .

run:
	$(info Make: Starting "$(SERVICE)" container.)
	docker run --privileged --name $(SERVICE) -v $(PWD)/Hello:/jdwp/Hello -p 8080:8080 -p 8081:8081 $(SERVICE)

exec:
	$(info Make: Execing into "$(SERVICE)" container.)
	docker exec -it $(SERVICE) /bin/bash

stop:
	$(info Make: Stopping "$(SERVICE)" container.)
	docker stop $(SERVICE)
	docker rm $(SERVICE)

clean:
	docker system prune --volumes --force

login:
	$(info Make: Login to Docker Hub.)
	docker login -u $(DOCKER_USER) -p $(DOCKER_PASS)