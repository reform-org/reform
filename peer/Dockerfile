FROM docker.io/sbtscala/scala-sbt:eclipse-temurin-jammy-19.0.1_10_1.8.2_3.2.2 AS sbt

WORKDIR /app
COPY build.sbt ./
COPY project project
COPY src src

RUN sbt reformJVM/assembly


FROM openjdk:17-alpine

WORKDIR /app
COPY --from=sbt /app/src/jvm/target/scala-3.3.0/reform-assembly-0.1.0-SNAPSHOT.jar ./

CMD java -jar reform-assembly-0.1.0-SNAPSHOT.jar
