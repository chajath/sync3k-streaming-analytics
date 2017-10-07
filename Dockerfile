FROM anapsix/alpine-java

MAINTAINER sync3k

ENV kafkaServer="kafka:9092"

ADD "target/scala-2.11/sync3k-streaming-analytics.jar" /opt/sync3k-streaming-analytics.jar
RUN chmod 400 /opt/sync3k-streaming-analytics.jar

ENTRYPOINT ["sh", "-c", "java -jar /opt/sync3k-streaming-analytics.jar --kafkaServer $kafkaServer"]
