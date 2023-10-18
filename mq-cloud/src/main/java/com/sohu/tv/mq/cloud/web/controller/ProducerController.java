package com.sohu.tv.mq.cloud.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.AssociateProducerParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

import javax.validation.Valid;

/**
 * 生产者
 * 
 * @author yongfeigao
 * @date 2018年9月13日
 */
@Controller
@RequestMapping("/producer")
public class ProducerController extends ViewController {
    
    @Autowired
    private UserProducerService userProducerService;

    @Autowired
    private VerifyDataService verifyDataService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private TopicController topicController;

    @Autowired
    private UserService userService;

    /**
     * 状况展示
     * 
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/stats")
    public String stats(UserInfo userInfo, @RequestParam("producer") String producer, Map<String, Object> map)
            throws Exception {
        return viewModule() + "/stats";
    }
    
    /**
     * 消费者列表
     * 
     * @param topicParam
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/list")
    public Result<?> list(UserInfo userInfo, @RequestParam("tid") int tid) throws Exception {
        Result<List<UserProducer>> listResult = userProducerService.queryUserProducerByTid(tid);
        if (listResult.isNotEmpty()) {
            // 去重
            List<UserProducer> userProducerList = listResult.getResult();
            Set<UserProducer> userProducerSet = new HashSet<>();
            for (UserProducer userProducer : userProducerList) {
                userProducerSet.add(userProducer);
            }
            return Result.getResult(userProducerSet);
        }
        return Result.getWebResult(listResult);
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String add(Map<String, Object> map)
            throws Exception {
        setView(map, "add", "新建生产者");
        return view();
    }

    @RequestMapping(value = "/associate", method = RequestMethod.GET)
    public String associate(Map<String, Object> map)
            throws Exception {
        setView(map, "associate", "关联生产者");
        return view();
    }

    /**
     * 新建生产者
     *
     * @param topicParam
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> newProducer(UserInfo userInfo, @Valid AssociateProducerParam associateProducerParam)
            throws Exception {
        return newUserProducer(userInfo, associateProducerParam);
    }

    /**
     * 新建生产者
     *
     * @param userInfo
     * @param uid
     * @param tid
     * @param producer
     * @return
     */
    private Result<?> newUserProducer(UserInfo userInfo, AssociateProducerParam associateProducerParam) {
        long uid = userInfo.getUser().getId();
        long tid = associateProducerParam.getTid();
        String producer = associateProducerParam.getProducer();
        // 校验关联关系是否存在
        Result<?> isExist = verifyDataService.verifyUserProducerIsExist(Audit.TypeEnum.NEW_PRODUCER, uid, tid, producer);
        if (isExist.getStatus() != Status.OK.getKey()) {
            return isExist;
        }
        // 构建AuditAssociateProducer
        AuditAssociateProducer auditAssociateProducer = new AuditAssociateProducer();
        auditAssociateProducer.setProducer(producer);
        auditAssociateProducer.setTid(tid);
        auditAssociateProducer.setUid(uid);
        auditAssociateProducer.setProtocol(associateProducerParam.getProtocol());
        // 构建Audit
        Audit audit = new Audit();
        audit.setType(Audit.TypeEnum.NEW_PRODUCER.getType());
        audit.setStatus(Audit.StatusEnum.INIT.getStatus());
        audit.setUid(userInfo.getUser().getId());
        Result<Audit> result = auditService.saveAuditAndAssociateProducer(audit, auditAssociateProducer);
        if (result.isOK()) {
            // 根据申请的不同发送不同的消息
            String tip = topicController.getTopicTip(tid) + " producer:<b>" + producer + "</b>";
            alertService.sendAuditMail(userInfo.getUser(), Audit.TypeEnum.NEW_PRODUCER, tip);
        }
        return Result.getWebResult(result);
    }

    /**
     * 关联生产者
     *
     * @param topicParam
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/associate", method = RequestMethod.POST)
    public Result<?> associate(UserInfo userInfo, @Valid AssociateProducerParam associateProducerParam)
            throws Exception {
        return associateUserProducer(userInfo, userInfo.getUser().getId(), associateProducerParam.getTid(),
                associateProducerParam.getProducer());
    }

    /**
     * 授权关联
     *
     * @param userInfo
     * @param tid
     * @param uid
     * @param producer
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/auth/associate", method = RequestMethod.POST)
    public Result<?> authAssociate(UserInfo userInfo, @RequestParam("tid") long tid,
                                   @RequestParam("uid") long uid,
                                   @RequestParam("producer") String producer) throws Exception {
        if (tid < 1 || uid < 1 || producer == "") {
            return Result.getResult(Status.PARAM_ERROR);
        }
        return associateUserProducer(userInfo, uid, tid, producer);
    }

    /**
     * 复用之前的逻辑，
     *
     * @param userInfo
     * @param uid
     * @param tid
     * @param producer
     * @return
     */
    private Result<?> associateUserProducer(UserInfo userInfo, long uid, long tid, String producer) {
        // 校验关联关系是否存在
        Result<?> isExist = verifyDataService.verifyUserProducerIsExist(Audit.TypeEnum.ASSOCIATE_PRODUCER, uid, tid, producer);
        if (isExist.getStatus() != Status.OK.getKey()) {
            return isExist;
        }
        // 构建AuditAssociateProducer
        AuditAssociateProducer auditAssociateProducer = new AuditAssociateProducer();
        auditAssociateProducer.setProducer(producer);
        auditAssociateProducer.setTid(tid);
        auditAssociateProducer.setUid(uid);
        // 获取存在的生产者
        int protocol = 0;
        Result<List<UserProducer>> upListResult = userProducerService.queryUserProducer(producer);
        if (!upListResult.isEmpty()) {
            protocol = upListResult.getResult().get(0).getProtocol();
        }
        auditAssociateProducer.setProtocol(protocol);
        // 构建Audit
        Audit audit = new Audit();
        audit.setType(Audit.TypeEnum.ASSOCIATE_PRODUCER.getType());
        audit.setStatus(Audit.StatusEnum.INIT.getStatus());
        audit.setUid(userInfo.getUser().getId());
        Result<Audit> result = auditService.saveAuditAndAssociateProducer(audit, auditAssociateProducer);
        if (result.isOK()) {
            // 根据申请的不同发送不同的消息
            String tip = null;
            if (userInfo.getUser().getId() == uid) {
                tip = topicController.getTopicTip(tid) + " producer:<b>" + producer + "</b>";
            } else {
                Result<User> userResult = userService.query(uid);
                if (userResult.isOK()) {
                    tip = topicController.getTopicTip(tid) + " producer:<b>" + producer + "</b>  user:<b>"
                            + userResult.getResult().notBlankName() + "</b>";
                }
            }
            if (tip == null) {
                return Result.getResult(Status.EMAIL_SEND_ERR);
            }
            alertService.sendAuditMail(userInfo.getUser(), Audit.TypeEnum.ASSOCIATE_PRODUCER, tip);
        }
        return Result.getWebResult(result);
    }

    @Override
    public String viewModule() {
        return "producer";
    }
}
