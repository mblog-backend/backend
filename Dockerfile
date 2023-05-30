FROM openjdk:17-alpine
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories && apk update && apk add busybox-extras
RUN apk add tzdata && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone  && apk del tzdata
ADD ./target/memo-0.0.1-SNAPSHOT.jar /app/memo-0.0.1-SNAPSHOT.jar
ADD ./target/classes/*.properties /app/
ENV JAVA_OPTS="-Xms512m -Xmx512m"
ENV DB_TYPE="default"
CMD java $JAVA_OPTS -jar /app/memo-0.0.1-SNAPSHOT.jar --spring.config.location=/app/application-$DB_TYPE.properties