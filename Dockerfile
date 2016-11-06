FROM anapsix/alpine-java
COPY target/ellamination-*.jar /app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
