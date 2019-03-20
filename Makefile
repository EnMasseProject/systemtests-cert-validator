VERSION     ?= 1.0-SNAPSHOT
DOCKER_ORG	?= docker.io/famargon
TAG ?= latest
PROJECT_NAME ?= openshift-cert-validator

all: clean_java package_java docker_build docker_tag docker_push

image: clean_java package_java docker_build

package_java:
	mvn -U clean package -DskipTests

clean_java:
	mvn clean
	rm -rf build target

clean: clean_java

docker_build: package_java
	if [ -f Dockerfile ]; then docker build --build-arg version=$(VERSION) -t $(PROJECT_NAME):$(TAG) . ; fi
	docker images | grep $(PROJECT_NAME)

docker_push:
	docker push $(DOCKER_ORG)/$(PROJECT_NAME):$(TAG)

docker_tag:
	docker tag $(PROJECT_NAME):$(TAG) $(DOCKER_ORG)/$(PROJECT_NAME):$(TAG)

.PHONY: clean_java package_java docker_build docker_tag docker_push