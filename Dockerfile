FROM openjdk:17-alpine
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories && apk update && apk add busybox-extras
ENV TZ Asia/Shanghai
RUN apk add tzdata && cp /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo ${TZ} > /etc/timezone \
    && apk del tzdata
ADD ./target/memo-0.0.1-SNAPSHOT.jar /app/memo-0.0.1-SNAPSHOT.jar
ADD ./target/classes/application.properties /app/application.properties
CMD java -Xms512m -Xmx512m -jar /app/memo-0.0.1-SNAPSHOT.jar --spring.config.location=/app/application.properties