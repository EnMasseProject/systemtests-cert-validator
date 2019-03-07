FROM enmasseproject/java-base:11-0

ARG version=latest
ENV VERSION ${version}
ADD target/ocp-enmasse-${VERSION}.jar /ocp-enmasse.jar
ENV JAVA_OPTS "-DLOG_LEVEL=info"

EXPOSE 8080/tcp

CMD ["/opt/run-java/launch_java.sh", "/ocp-enmasse.jar"]