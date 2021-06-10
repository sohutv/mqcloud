package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.ClientVersion;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.dao.ClientVersionDao;
import com.sohu.tv.mq.cloud.dao.UserDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 客户端版本
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月31日
 */
@Service
public class ClientVersionService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ClientVersionDao clientVersionDao;
    
    @Autowired
    private UserDao userDao;
    
    /**
     * 保存客户端版本
     * @param clientVersion
     * @return
     */
    public Result<Integer> save(ClientVersion clientVersion){
        Integer result = null;
        try {
            result = clientVersionDao.insert(clientVersion);
        } catch (Exception e) {
            logger.error("insert err, clientVersion:{}", clientVersion, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 查询所有客户端版本
     * @return
     */
    public Result<List<ClientVersion>> queryAll(){
        List<ClientVersion> list = null;
        try {
            list = clientVersionDao.selectAll();
            setOwners(list);
        } catch (Exception e) {
            logger.error("queryAll", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
    
    /**
     * 查询某个客户端版本
     * @return
     */
    public Result<ClientVersion> query(String topic, String consumer){
        ClientVersion clientVersion = null;
        try {
            clientVersion = clientVersionDao.selectClientVersion(topic, consumer, ClientVersion.CONSUMER);
        } catch (Exception e) {
            logger.error("query topic:{} consumer:{}", topic, consumer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(clientVersion);
    }
    
    /**
     * 设置客户端归属的用户
     * @param list
     */
    private void setOwners(List<ClientVersion> list) {
        setOwners(list, ClientVersion.PRODUCER);
        setOwners(list, ClientVersion.CONSUMER);
    }
    
    /**
     * 设置客户端归属的用户
     * @param list
     * @param role
     */
    private void setOwners(List<ClientVersion> list, int role) {
        List<String> clientList = getClientList(list, role);
        if(CollectionUtils.isEmpty(clientList)) {
            return;
        }
        List<User> userList = null;
        if(ClientVersion.PRODUCER == role) {
            userList = userDao.selectByProducerList(clientList);
        } else {
            userList = userDao.selectByConsumerList(clientList);
        }
        for(ClientVersion cv : list) {
            if(cv.getRole() != role) {
                continue;
            }
            for(User user : userList) {
                if(cv.getClient().equals(user.getPassword())) {
                    cv.addOwner(user);
                }
            }
        }
    }
    
    /**
     * 获取producer/consumer名字列表
     * @param list
     * @param role
     * @return
     */
    private List<String> getClientList(List<ClientVersion> list, int role){
        if(list == null) {
            return null;
        }
        List<String> producerList = new ArrayList<>();
        for(ClientVersion cv : list) {
            if(role == cv.getRole()) {
                producerList.add(cv.getClient());
            }
        }
        return producerList;
    }
}
