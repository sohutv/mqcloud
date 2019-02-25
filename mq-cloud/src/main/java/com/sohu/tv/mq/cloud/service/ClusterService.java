package com.sohu.tv.mq.cloud.service;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.dao.ClusterDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 集群服务
 * 
 * @author yongfeigao
 * @date 2018年10月10日
 */
@Service
public class ClusterService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ClusterDao clusterDao;
    
    // cluster持有，初始化后即缓存到内存
    private volatile Cluster[] mqClusterArray;
    
    @PostConstruct
    public void refresh() {
        Result<List<Cluster>> clusterListResult = queryAll();
        if(clusterListResult.isEmpty()) {
            logger.error("no cluster data found!");
            return;
        }
        List<Cluster> list = clusterListResult.getResult();
        if(mqClusterArray == null || mqClusterArray.length != list.size()) {
            logger.info("cluster config refreshed, old:{} new:{}", Arrays.toString(mqClusterArray), list);
            mqClusterArray = clusterListResult.getResult().toArray(new Cluster[list.size()]);
        }
    }
    
    /**
     * 查询所有集群
     * @return
     */
    public Result<List<Cluster>> queryAll(){
        List<Cluster> list = null;
        try {
            list = clusterDao.select();
        } catch (Exception e) {
            logger.error("queryAll", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
    
    public Cluster[] getAllMQCluster() {
        return mqClusterArray;
    }
    
    /**
     * 根据id查找集群
     * @param id
     * @return
     */
    public Cluster getMQClusterById(long id) {
        for (Cluster mqCluster : getAllMQCluster()) {
            if (id == mqCluster.getId()) {
                return mqCluster;
            }
        }
        return null;
    }
    
    /**
     * 保存数据
     * @return
     */
    public Result<List<Cluster>> save(Cluster cluster){
        Integer result = null;
        try {
            result = clusterDao.insert(cluster);
            refresh();
        } catch (Exception e) {
            logger.error("save:{}", cluster, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
}
