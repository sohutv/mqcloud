#!/bin/sh
/usr/sbin/sshd -D &
sh /opt/mqcloud/ns/bin/mqnamesrv -c /opt/mqcloud/ns/ns.conf >> /opt/mqcloud/ns/logs/startup.log 2>&1 &
sh /opt/mqcloud/broker-a/bin/mqbroker -c /opt/mqcloud/broker-a/broker.conf >> /opt/mqcloud/broker-a/logs/startup.log 2>&1 &
java -jar -Dfile.encoding=UTF-8 -Dmqcloud.env=demo -Dspring.datasource.data=classpath:h2db/demo.sql -DPROJECT_DIR=/ /mq-cloud.war