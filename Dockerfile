FROM enmasseproject/java-base:11-0

ARG version=latest
ENV VERSION ${version}
ADD target/openshift-cert-validator-${VERSION}.jar /openshift-cert-validator.jar
ENV JAVA_OPTS "-DLOG_LEVEL=info"

EXPOSE 8080/tcp

CMD ["/opt/run-java/launch_java.sh", "/openshift-cert-validator.jar"]