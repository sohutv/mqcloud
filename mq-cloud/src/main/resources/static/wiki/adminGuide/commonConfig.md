## 通用配置

配置项释义如下：

1. domain

   由于mqcloud采用官方推荐的[客户端寻址方式](https://github.com/apache/rocketmq/blob/master/docs/cn/best_practice.md#51-%E5%AE%A2%E6%88%B7%E7%AB%AF%E5%AF%BB%E5%9D%80%E6%96%B9%E5%BC%8F)，故必须配置该项，且必须携带端口号，示例：mqcloud.com:80，否则无法运行。

2. serverUser

   添加机器时，需要新建的用户名，一般无需修改。

3. serverPassword

   添加机器时，用户名对应的密码，一般无需修改。

4. privateKey

   如果需要公钥登录，需要设置privateKey，可以参照[添加机器](machine#addMachine)。需要注意一点，私钥的格式不能变，否则会解析异常。

5. serverPort

   机器ssh服务端口，一般无需修改。

6. serverConnectTimeout

   ssh链接建立超时时间，一般无需修改。

7. serverOPTimeout

   ssh操作超时时间，一般无需修改。

8. ciperKey

   密码加密因子，一般无需修改。

9. classList

   消息体是对象的topic列表，这些topic的消息对应的对象class需要放置于resources/msg-type文件夹下，为了实现隔离，mqcloud采用单独的类加载机制来加载这些class，解析消息时针对这些topic采用这些额外的class。

10. operatorContact

  运维人员json，参考现有的格式修改即可。

11. mail*

    mail相关配置，这里不再过多解释。

12. isOpenRegister

    是否开启注册功能，默认开启。不开启外部用户无法注册，但管理员可以从后台添加。

13. rocketmqFilePath

    rocketmq资源包路径，支持三种资源加载方式，默认为1：

    1. classpath:static/software/rocketmq.zip 
    2. file:///tmp/rocketmq.zip 
    3. http://mqcloud.domain.com/data/rocketmq.zip

14. adminAccessKey

    管理员访问机器用户名

15. adminSecretKey

    管理员访问机器密码

16. queryMessageFromSlave

    是否从slave查询消息

17. machineRoom

    机房节点列表

18. machineRoomColor

    机房节点颜色