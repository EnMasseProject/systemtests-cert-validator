FROM enmasseproject/java-base:11-0

ARG version=latest
ENV VERSION ${version}
ADD target/systemtests-cert-validator-${VERSION}.jar /systemtests-cert-validator.jar
ENV JAVA_OPTS "-DLOG_LEVEL=info"

EXPOSE 8080/tcp

CMD ["/opt/run-java/launch_java.sh", "/systemtests-cert-validator.jar"]