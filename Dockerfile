FROM openjdk:11-jdk-slim

WORKDIR /etc/pkb/

ADD target/WhatEverYouLikey-jar-with-dependencies.jar .

ENV PROJECT_ID=fhir-experiments-20210712
ENV TOPIC=kms-key-available-05
ENV SUBSCRIPTION=kms-key-available-05-kms

ENV EXTRA_JAVA_OPTS=""

CMD java -jar WhatEverYouLikey-jar-with-dependencies.jar ${EXTRA_JAVA_OPTS}
