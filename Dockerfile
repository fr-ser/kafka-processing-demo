FROM hseeberger/scala-sbt:11.0.9.1_1.4.6_2.13.4 AS builder
# Java 11.0.9.1., SBT 1.4.6, Scala 2.13.4

WORKDIR /app
COPY build.sbt /app
COPY project /app/project

RUN sbt update

COPY src /app/src
RUN sbt assembly

FROM adoptopenjdk/openjdk11:jre-11.0.9.1_1-alpine

COPY --from=builder /app/target /app

WORKDIR /app

ENTRYPOINT [ "java", "-jar", "reading_filter.jar" ]
