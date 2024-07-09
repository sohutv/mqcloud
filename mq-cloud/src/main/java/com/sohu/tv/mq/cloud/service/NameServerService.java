package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.NameServer;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.dao.NameServerDao;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import org.apache.rocketmq.common.namesrv.NamesrvUtil;
import org.apache.rocketmq.remoting.RemotingClient;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.header.namesrv.PutKVConfigRequestHeader;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * name server
 * 
 * @author yongfeigao
 * @date 2018年10月23日
 */
@Service
public class NameServerService {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private NameServerDao nameServerDao;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TopicService topicService;

    /**
     * 保存记录
     * 
     * @return 返回Result
     */
    public Result<?> save(int cid, String addr) {
        return save(cid, addr, null);
    }
    
    /**
     * 保存记录
     * 
     * @return 返回Result
     */
    public Result<?> save(int cid, String addr, String baseDir) {
        try {
            Result result = Result.getResult(nameServerDao.insert(cid, addr, baseDir));
            if (result.isOK()) {
                initOrderTopicConfig(cid, addr);
            }
            return result;
        } catch (Exception e) {
            logger.error("insert err, cid:{}, addr:{}, baseDir:{}", cid, addr, baseDir, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 初始化order topic配置
     *
     * @param cid
     * @param addr
     */
    private void initOrderTopicConfig(int cid, String addr) {
        String kvConfig = mqCloudConfigHelper.getOrderTopicKVConfig(String.valueOf(cid));
        if (kvConfig == null || kvConfig.isEmpty()) {
            return;
        }
        Result<List<String>> listResult = topicService.queryOrderedTopicList(cid);
        if (listResult.isEmpty()) {
            return;
        }
        mqAdminTemplate.execute(new MQAdminCallback<Void>() {
            public Void callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                RemotingClient remotingClient = sohuMQAdmin.getMQClientInstance().getMQClientAPIImpl().getRemotingClient();
                List<String> topics = listResult.getResult();
                for (String topic : topics) {
                    PutKVConfigRequestHeader requestHeader = new PutKVConfigRequestHeader();
                    requestHeader.setNamespace(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG);
                    requestHeader.setKey(topic);
                    requestHeader.setValue(kvConfig);
                    RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.PUT_KV_CONFIG, requestHeader);
                    remotingClient.invokeSync(addr, request, 5000);
                }
                return null;
            }

            public Void exception(Exception e) throws Exception {
                throw e;
            }

            public Cluster mqCluster() {
                return clusterService.getMQClusterById(cid);
            }
        });
    }
    
    /**
     * 查询集群的name server
     * 
     * @return Result<List<NameServer>>
     */
    public Result<List<NameServer>> query(int cid) {
        try {
            return Result.getResult(nameServerDao.selectByClusterId(cid));
        } catch (Exception e) {
            logger.error("query cid:{} err", cid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询name server
     *
     * @return Result<NameServer>
     */
    public Result<NameServer> query(String addr) {
        try {
            return Result.getResult(nameServerDao.selectByAddr(addr));
        } catch (Exception e) {
            logger.error("query addr:{} err", addr, e);
            return Result.getDBErrorResult(e);
        }
    }
    
    /**
     * 查询全部name server
     * 
     * @return Result<List<NameServer>>
     */
    public Result<List<NameServer>> queryAll() {
        try {
            return Result.getResult(nameServerDao.selectAll());
        } catch (Exception e) {
            logger.error("query all err", e);
            return Result.getDBErrorResult(e);
        }
    }
    
    /**
     * 删除记录
     * 
     * @return 返回Result
     */
    public Result<?> delete(int cid, String addr) {
        try {
            return Result.getResult(nameServerDao.delete(cid, addr));
        } catch (Exception e) {
            logger.error("delete err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
    }
    
    
    /**
     * 更新记录
     * @param cid
     * @param addr
     * @return
     */
    public Result<?> update(int cid, String addr, CheckStatusEnum checkStatusEnum) {
        try {
            return Result.getResult(nameServerDao.update(cid, addr, checkStatusEnum.getStatus()));
        } catch (Exception e) {
            logger.error("update err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 健康检查
     *
     * @param cluster
     * @param addr
     * @return
     */
    public Result<?> healthCheck(Cluster cluster, String addr) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<?>>() {
            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                try {
                    mqAdmin.getNameServerConfig(Arrays.asList(addr));
                    return Result.getOKResult();
                } catch (Exception e) {
                    return Result.getDBErrorResult(e).setMessage("addr:" + addr + ";Exception: " + e.getMessage());
                }
            }

            public Cluster mqCluster() {
                return cluster;
            }

            @Override
            public Result<?> exception(Exception e) throws Exception {
                return Result.getDBErrorResult(e).setMessage("Exception: " + e.getMessage());
            }
        });
    }
}
