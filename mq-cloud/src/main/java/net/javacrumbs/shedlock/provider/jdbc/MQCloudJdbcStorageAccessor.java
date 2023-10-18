package net.javacrumbs.shedlock.provider.jdbc;

import com.sohu.tv.mq.util.CommonUtil;
import org.apache.rocketmq.common.utils.NetworkUtil;

import javax.sql.DataSource;

/**
 * @Description: 为了获取hostname，重写了getHostname方法
 * @Auther: yongfeigao
 * @Date: 2023/9/21
 */
public class MQCloudJdbcStorageAccessor extends JdbcStorageAccessor {
    public MQCloudJdbcStorageAccessor(DataSource dataSource, String tableName) {
        super(dataSource, tableName);
    }

    @Override
    protected String getHostname() {
        if (CommonUtil.IP != null) {
            return CommonUtil.IP;
        }
        return super.getHostname();
    }
}
