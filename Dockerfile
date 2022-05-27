FROM gradle:7.4.2-jdk8 AS build 
COPY --chown=gradle:gradle ./ /home/work/
WORKDIR /home/work 
COPY --chown=gradle:gradle ./server.build.gradle ./build.gradle
RUN gradle build --no-daemon 

FROM openjdk:8-jre-slim

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/MusicalChairsGame.jar /app/MusicalChairsGame.jar

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/MusicalChairsGame.jar"]
