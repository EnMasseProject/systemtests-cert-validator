VERSION     ?= 1.0-SNAPSHOT
DOCKER_ORG	?= docker.io/famargon
TAG ?= latest

all: clean_java package_java docker_build docker_tag docker_push

image: clean_java package_java docker_build

package_java:
	mvn -U clean package -DskipTests

clean_java:
	mvn clean
	rm -rf build target

clean: clean_java

docker_build: package_java
	if [ -f Dockerfile ]; then docker build --build-arg version=$(VERSION) -t ocp-enmasse-app:$(TAG) . ; fi
	docker images | grep ocp-enmasse-app

docker_push:
	docker push $(DOCKER_ORG)/ocp-enmasse-app:$(TAG)

docker_tag:
	docker tag ocp-enmasse-app:$(TAG) $(DOCKER_ORG)/ocp-enmasse-app:$(TAG)

.PHONY: clean_java package_java docker_build docker_tag docker_push