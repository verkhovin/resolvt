FROM openjdk:11.0.15-jdk as BUILD
COPY . /build
CMD chmod -R 777 /build
WORKDIR /build
RUN ./mvnw clean install

FROM openjdk:11.0.15-jre
EXPOSE 8080
RUN mkdir /app
COPY --from=build /build/target/*ithurts*.jar /app/ithurts.jar
ENTRYPOINT ["java","-jar","/app/ithurts.jar"]