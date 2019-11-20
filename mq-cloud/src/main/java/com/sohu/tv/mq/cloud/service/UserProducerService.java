package com.sohu.tv.mq.cloud.service;

import java.util.List;

import com.sohu.tv.mq.cloud.bo.User;
import org.apache.rocketmq.common.protocol.body.ProducerConnection;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.dao.UserProducerDao;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
/**
 * 用户生产者服务
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月23日
 */
@Service
public class UserProducerService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private UserProducerDao userProducerDao;
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    /**
     * 按照uid查询UserProducer
     */
    public Result<List<UserProducer>> queryUserProducer(long uid) {
        List<UserProducer> list = null;
        try {
            list = userProducerDao.selectByUid(uid);
        } catch (Exception e) {
            logger.error("queryUserProducer err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
    
    /**
     * 按照producer查询UserProducer
     */
    public Result<List<UserProducer>> queryUserProducer(String producer) {
        List<UserProducer> list = null;
        try {
            list = userProducerDao.selectByProducer(producer);
        } catch (Exception e) {
            logger.error("queryUserProducer err, producer:{}", producer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
    
    /**
     * 按照name查询UserProducer
     */
    public Result<UserProducer> queryUserProducer(long tid, String name) {
        UserProducer userProducer = null;
        try {
            userProducer = userProducerDao.selectByName(name, tid);
        } catch (Exception e) {
            logger.error("queryProducer err, tid:{}, name:{}", tid, name, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userProducer);
    }
    
    /**
     * 按照tid查询UserProducer
     */
    public Result<List<UserProducer>> queryUserProducerByTid(long tid) {
        List<UserProducer> userProducerList = null;
        try {
            userProducerList = userProducerDao.selectByTid(tid);
        } catch (Exception e) {
            logger.error("queryUserProducerByTid err, tid:{}", tid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userProducerList);
    }
    
    
    /**
     * 保存记录
     * 
     * @param UserProducer
     * @return 返回Integer
     */
    @Transactional
    public Integer save(UserProducer userProducer) {
        try {
            return userProducerDao.insert(userProducer);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", userProducer);
            throw e;
        } catch (Exception e) {
            logger.error("insert err, userProducer:{}", userProducer, e);
            throw e;
        }
    }
    
    /**
     * 删除记录
     * 
     * @param UserProducer
     * @return 返回Integer
     */
    @Transactional
    public Integer delete(long tid) {
        try {
            return userProducerDao.deleteByTid(tid);
        } catch (Exception e) {
            logger.error("delete err tid:{}", tid, e);
            throw e;
        }
    }
    
    /**
     * 保存记录
     * 
     * @param UserProducer
     * @return 返回Result
     */
    public Result<UserProducer> saveNoException(UserProducer userProducer) {
        try {
            userProducerDao.insert(userProducer);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", userProducer);
            return Result.getResult(Status.DB_DUPLICATE_KEY);
        } catch (Exception e) {
            logger.error("insert err, userProducer:{}", userProducer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userProducer);
    }
    
    /**
     * 获取生产者链接
     * @param producerGroup
     * @param topic
     * @param mqCluster
     * @return
     */
    public Result<ProducerConnection> examineProducerConnectionInfo(String producerGroup, String topic, Cluster mqCluster) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<ProducerConnection>>() {
            public Result<ProducerConnection> callback(MQAdminExt mqAdmin) throws Exception {
                ProducerConnection producerConnection = mqAdmin.examineProducerConnectionInfo(producerGroup, topic);
                return Result.getResult(producerConnection);
            }
            public Result<ProducerConnection> exception(Exception e) throws Exception {
                return Result.getDBErrorResult(e);
            }
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }
    
    /**
     * 删除UserProducer
     * @param userProducer
     * @return
     */
    public Result<?> deleteUserProducer(UserProducer userProducer) {
        try {
            // 第一步：删除userProducer记录
            Integer count = userProducerDao.deleteByID(userProducer.getId());
            if(count == null || count != 1) {
                return Result.getResult(Status.DB_ERROR);
            }           
        } catch (Exception e) {
            logger.error("deleteUserProducer uid:{} , producer:{}", userProducer.getUid(),userProducer.getProducer(), e);           
            return Result.getWebErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 按照pid查询UserProducer
     */
    public Result<UserProducer> findUserProducer(long pid) {
        UserProducer userProducer = null;
        try {
            userProducer = userProducerDao.selectByPid(pid);
        } catch (Exception e) {
            logger.error("queryUserProducer err, pid:{}", pid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userProducer);
    }
    
    /**
     * 按照uid和producer查询UserProducer
     * 适用于前台校验用户是否重复关联
     */
    public Result<List<UserProducer>> findUserProducer(long uid, String producer) {
        List<UserProducer> userProducerList = null;
        try {
            userProducerList = userProducerDao.selectByProducerAndUid(producer, uid);
        } catch (Exception e) {
            logger.error("findUserProducer err, pid:{}, producer:{}", uid, producer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userProducerList);
    }
    
    /**
     * 按照uid和tid查询UserProducer
     */
    public Result<UserProducer> findUserProducer(long uid, long tid) {
        UserProducer userProducer = null;
        try {
            userProducer = userProducerDao.selectByTidAndUid(uid, tid);
        } catch (Exception e) {
            logger.error("findUserProducer err, uid:{}, tid:{}", uid, tid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userProducer);
    }

    /**
     * 按照uid和producer查询topicId
     */
    public Result<List<Long>> findTopicIdList(User user, String producer) {
        if (user.isAdmin()) {
            return findTopicIdList(0, producer);
        } else {
            return findTopicIdList(user.getId(), producer);
        }
    }

    /**
     * 按照uid和producer查询topicId
     */
    public Result<List<Long>> findTopicIdList(long uid, String producer) {
        List<Long> topicIdList = null;
        try {
            topicIdList = userProducerDao.selectTidByProducerAndUid(uid, producer);
        } catch (Exception e) {
            logger.error("findTopicIdList err, uid:{}, producer:{}", uid, producer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicIdList);
    }
}
