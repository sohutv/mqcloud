package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.StatsProducer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicTopology;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.cache.LocalCache;
import com.sohu.tv.mq.cloud.dao.ConsumerDao;
import com.sohu.tv.mq.cloud.dao.UserDao;
import com.sohu.tv.mq.cloud.util.Jointer;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;

/**
 * 用户服务
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年5月28日
 */
@Service
public class UserService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserProducerService userProducerService;

    @Autowired
    private ConsumerDao consumerDao;

    @Autowired
    private TopicService topicService;

    @Autowired
    private LocalCache<User> userLocalCache;

    @Autowired
    private LocalCache<Object> mqLocalCache;

    /**
     * 插入用户记录
     * 
     * @param user
     * @return 返回Result
     */
    public Result<User> save(User user) {
        try {
            userDao.insert(user);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", user);
            return Result.getResult(Status.DB_DUPLICATE_KEY);
        } catch (Exception e) {
            logger.error("insert err, user:{}", user, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(user);
    }

    /**
     * 查询用户记录
     * 
     * @param user
     */
    public Result<List<User>> queryAll() {
        List<User> userList = null;
        try {
            userList = userDao.selectAll();
        } catch (Exception e) {
            logger.error("selectAll err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userList);
    }

    /**
     * 查询管理员
     * 
     * @param user
     */
    public Result<List<User>> queryAdmin() {
        List<User> userList = null;
        try {
            userList = userDao.selectAdmin();
        } catch (Exception e) {
            logger.error("queryAdmin err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userList);
    }

    /**
     * 查询用户记录
     * 
     * @param email
     */
    public Result<User> queryByEmail(String email) {
        User user = null;
        try {
            user = userLocalCache.get(email);
            if (user == null) {
                user = userDao.selectByEmail(email);
                if (user != null) {
                    userLocalCache.put(email, user);
                }
            }
        } catch (Exception e) {
            logger.error("queryByEmail err, email:{}", email, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(user);
    }

    /**
     * 更新用户信息
     * 
     * @param user
     */
    public Result<Integer> update(User user) {
        Integer count = null;
        try {
            count = userDao.update(user);
            if(user.getEmail() != null) {
                userLocalCache.cleanUp(user.getEmail());
            }
        } catch (Exception e) {
            logger.error("update err, user:{}", user, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 查询用户的topic拓扑
     * 
     * @param user
     */
    public Result<TopicTopology> queryTopicTopology(User user, long topicId) {
        // 获取topic
        Result<Topic> topicResult = topicService.queryTopic(topicId);
        if (!topicResult.isOK()) {
            return Result.getResult(Status.NO_RESULT);
        }
        // 获取生产者关系
        Result<List<UserProducer>> userProducerListResult = userProducerService.queryUserProducerByTid(topicId);
        if (userProducerListResult.isNotOK()) {
            if (userProducerListResult.getException() != null) {
                return Result.getDBErrorResult(userProducerListResult.getException());
            }
            return Result.getResult(Status.NO_RESULT);
        }
        TopicTopology topicTopology = null;
        try {
            // admin和生成者获取所有消费者
            topicTopology = new TopicTopology();
            List<Consumer> consumerList = null;
            List<UserProducer> userProducerList = userProducerListResult.getResult();
            if (user.isAdmin() || isProducer(user, userProducerList)) {
                topicTopology.setOwn(true);
                consumerList = consumerDao.selectByTid(topicId);
            } else {
                // 否则，只获取该用户的消费者
                consumerList = consumerDao.select(user.getId(), topicId);
            }
            // 对象组装
            topicTopology.setUid(user.getId());
            topicTopology.setPrevProducerList(userProducerList);
            if (userProducerList != null && userProducerList.size() > 0) {
                Map<StatsProducer, List<UserProducer>> filterMap = uniqProducerList(userProducerList);
                topicTopology.setProducerFilterMap(filterMap);
            }
            topicTopology.setTopic(topicResult.getResult());
            topicTopology.setConsumerList(consumerList);
        } catch (Exception e) {
            logger.error("selectTopicTopology err, user:{}, tid:{}", user, topicId, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicTopology);
    }

    /**
     * userProducerList去重
     * 
     * @param userProducerList
     * @return
     */
    private Map<StatsProducer, List<UserProducer>> uniqProducerList(List<UserProducer> userProducerList) {
        Map<StatsProducer, List<UserProducer>> filterMap = new HashMap<StatsProducer, List<UserProducer>>();
        for (UserProducer userProducer : userProducerList) {
            StatsProducer statsProducer = new StatsProducer();
            statsProducer.setProducer(userProducer.getProducer());
            List<UserProducer> userProducerListCopy = filterMap.get(statsProducer);
            if (userProducerListCopy == null) {
                userProducerListCopy = new ArrayList<UserProducer>();
                filterMap.put(statsProducer, userProducerListCopy);
            }
            userProducerListCopy.add(userProducer);
        }
        return filterMap;
    }

    /**
     * 判断是否是生产者
     * 
     * @param user
     * @param userProducerList
     * @return boolean
     */
    private boolean isProducer(User user, List<UserProducer> userProducerList) {
        if (userProducerList == null) {
            return false;
        }
        for (UserProducer userProducer : userProducerList) {
            if (user.getId() == userProducer.getUid()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查询用户记录
     * 
     * @param user
     */
    public Result<List<User>> query(Collection<Long> idCollection) {
        List<User> userList = null;
        try {
            userList = userDao.selectByIdList(idCollection);
        } catch (Exception e) {
            logger.error("query err, idCollection:{}", idCollection, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userList);
    }

    /**
     * 查询用户记录
     * 
     * @param user
     */
    public Result<User> query(long id) {
        List<Long> idCollection = new ArrayList<Long>();
        idCollection.add(id);
        Result<List<User>> result = query(idCollection);
        if (result.isNotEmpty()) {
            return Result.getResult(result.getResult().get(0));
        }
        return Result.getResult(Status.NO_RESULT);
    }

    /**
     * 查询监控的用户邮件
     * 
     * @param user
     */
    public String queryMonitorEmail() {
        Object email = mqLocalCache.get("monitor");
        if (email == null) {
            List<User> userList = queryMonitorUser();
            if (userList != null && userList.size() > 0) {
                email = Jointer.BY_COMMA.join(userList, u -> u.getEmail());
                mqLocalCache.put("monitor", email);
            }
        }
        if (email == null) {
            return null;
        }
        return email.toString();
    }
    
    /**
     * 查询监控的用户手机
     * 
     * @param user
     */
    @SuppressWarnings("unchecked")
    public List<String> queryMonitorPhone() {
        List<String> phoneList = (List<String>) mqLocalCache.get("monitorPhone");
        if (phoneList == null) {
            List<User> userList = queryMonitorUser();
            phoneList = new ArrayList<String>();
            if (userList != null && userList.size() > 0) {
                for(User user : userList) {
                    phoneList.add(user.getMobile());
                }
            }
            mqLocalCache.put("monitorPhone", phoneList);
        }
        return phoneList;
    }
    
    /**
     * 查询监控的用户
     * 
     * @param user
     */
    public List<User> queryMonitorUser() {
        try {
            return userDao.selectMonitor();
        } catch (Exception e) {
            logger.error("queryMonitor err", e);
        }
        return null;
    }
    
    
    /**
     * 用户名密码查询用户记录
     * 
     * @param email
     */
    public Result<User> queryByEmailAndPassword(String email, String password) {
        User user = null;
        try {
            user = userDao.selectByEmailAndPassword(email, DigestUtils.md5Hex(password));
        } catch (Exception e) {
            logger.error("queryByEmailAndPassword err, email:{}", email, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(user);
    }
    
    /**
     * 重置密码
     * 
     * @param uid
     * @param password
     */
    public Result<Integer> resetPassword(long uid, String password) {
        Integer count = null;
        try {
            count = userDao.resetPassword(uid, DigestUtils.md5Hex(password));
        } catch (Exception e) {
            logger.error("resetPassword err, uid:{}, password:{}", uid, password, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 查询topic id关联的消费者用户列表
     * @param tid
     */
    public Result<List<User>> queryConsumerUserList(long tid) {
        List<User> userList = null;
        try {
            userList = userDao.selectConsumerUserListByTid(tid);
        } catch (Exception e) {
            logger.error("queryConsumerUserList err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userList);
    }

    /**
     * 查询topic id关联的生产者用户列表
     * @param tid
     */
    public Result<List<User>> queryProducerUserList(long tid) {
        List<User> userList = null;
        try {
            userList = userDao.selectProducerUserListByTid(tid);
        } catch (Exception e) {
            logger.error("queryProducerUserList err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userList);
    }
}
