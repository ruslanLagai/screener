# syntax = docker/dockerfile:experimental

# ------------------------------------------------------------------------------
# BUILD STAGE
# ------------------------------------------------------------------------------

FROM gradle:7.3.2-jdk17-alpine as build

ARG ARTIFACT_VERSION=0.1
ARG GRADLE_OPTS

WORKDIR /workspace/

COPY build.gradle settings.gradle gradlew ./

#COPY settings.gradle settings.gradle
#COPY build.gradle build.gradle
COPY banner.txt banner.txt

COPY gradlew gradlew
COPY src src

RUN --mount=type=cache,target=/root/.m2/ \
    --mount=type=cache,sharing=locked,target=/root/.gradle \
    gradle --no-daemon -s -i bootJar
# ------------------------------------------------------------------------------
# RUNTIME STAGE (deployment)
# ------------------------------------------------------------------------------

FROM openjdk:17.0.2-slim

ARG ARTIFACT_VERSION=1.0
ENV app_name=screener
ENV app_user=appuser

RUN addgroup ${app_user} && adduser --ingroup ${app_user} ${app_user}

RUN mkdir -p /opt/logs \
    && chown ${app_user}:${app_user} /opt/logs -R \
    && mkdir -p /opt/software/${app_name} \
    && chown ${app_user}:${app_user} /opt/software/${app_name} -R

COPY --from=build /workspace/build/libs/${app_name}.jar /opt/software/${app_name}.jar

WORKDIR /opt/software/

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$PROFILE -jar ${app_name}.jar"]
