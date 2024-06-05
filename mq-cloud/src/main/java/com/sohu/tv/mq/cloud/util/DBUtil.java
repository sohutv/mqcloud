package com.sohu.tv.mq.cloud.util;

import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * 数据库工具类
 *
 * @author yongfeigao
 * @date 2024年5月24日
 */
public class DBUtil {

    public static Logger LOGGER = LoggerFactory.getLogger(DBUtil.class);

    /**
     * 获取数量
     *
     * @param batchResults
     * @return
     */
    public static int getCount(List<BatchResult> batchResults) {
        if (batchResults == null) {
            return 0;
        }
        return batchResults.stream()
                .map(BatchResult::getUpdateCounts)
                .filter(Objects::nonNull)
                .flatMapToInt(IntStream::of)
                .sum();
    }

    /**
     * 批量更新
     *
     * @param sessionFactory
     * @param daoClass
     * @param daoConsumer
     * @param <T>
     * @return
     */
    public static <T> Result<Integer> batchUpdate(SqlSessionFactory sessionFactory, Class<T> daoClass, Consumer<T> daoConsumer) {
        try (SqlSession sqlSession = sessionFactory.openSession(ExecutorType.BATCH)) {
            daoConsumer.accept(sqlSession.getMapper(daoClass));
            List<BatchResult> batchResults = sqlSession.flushStatements();
            sqlSession.commit();
            return Result.getResult(DBUtil.getCount(batchResults));
        } catch (Exception e) {
            LOGGER.error("batchUpdate:{} error", daoClass.getName(), e);
            return Result.getDBErrorResult(e);
        }
    }
}
