FROM openjdk:8-jdk-alpine

# 更新源、安装openssh 并修改配置文件和生成key 并且同步时间
RUN apk update && \
    apk add openssh-server tzdata bash sudo net-tools && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    sed -i "s/#PermitRootLogin.*/PermitRootLogin yes/g" /etc/ssh/sshd_config && \
    sed -i 's/#StrictModes yes/StrictModes no/' /etc/ssh/sshd_config && \
    sed -i "s/Subsystem.*/Subsystem   sftp    internal-sftp/g" /etc/ssh/sshd_config && \
    sed -i 's/ash/bash/g' /etc/passwd && \
    ssh-keygen -t rsa -P "" -f /etc/ssh/ssh_host_rsa_key && \
    ssh-keygen -t ecdsa -P "" -f /etc/ssh/ssh_host_ecdsa_key && \
    ssh-keygen -t ed25519 -P "" -f /etc/ssh/ssh_host_ed25519_key && \
    echo "root:admin" | chpasswd && \
    echo "JAVA_HOME=/usr/lib/jvm/default-jvm" >> /etc/profile && \
    echo "PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/profile && \
    echo "export JAVA_HOME PATH" >> /etc/profile

USER root

# 添加mqcloud用户并设置权限
ARG user=mqcloud
ARG passwd=9j7t4SDJOIusddca+Mzd6Q==
ARG ssh_path=/home/mqcloud/.ssh
RUN mkdir -p $ssh_path && \
    adduser -D $user && echo "$user:$passwd" | chpasswd && \
    echo "mqcloud     ALL=(ALL)       NOPASSWD: ALL" >> /etc/sudoers && \
    sed -i "s/Defaults    requiretty/#Defaults    requiretty/g" /etc/sudoers

# 拷贝rocketmq文件
ARG ROCKETMQ_FILE
COPY ${ROCKETMQ_FILE} /tmp/rmq.zip

# 安装ns
ARG ns_path=/opt/mqcloud/ns
RUN mkdir -p $ns_path $ns_path/data/config $ns_path/logs
RUN chown mqcloud:mqcloud -R $ns_path
RUN unzip /tmp/rmq.zip -d $ns_path
ADD docker/ns.conf $ns_path/ns.conf

# 安装broker
ARG broker_path=/opt/mqcloud/broker-a
RUN mkdir -p $broker_path $broker_path/data/consumequeue $broker_path/data/commitlog $broker_path/logs
RUN unzip /tmp/rmq.zip -d $broker_path
RUN chown mqcloud:mqcloud -R $broker_path
ADD docker/broker.conf $broker_path/broker.conf

# mqcloud
ARG JAR_FILE
ADD ${JAR_FILE} /mq-cloud.war
ADD docker/nmon /tmp/nmon
ADD docker/run.sh /

ENTRYPOINT ["sh","/run.sh"]
EXPOSE 8080
EXPOSE 22