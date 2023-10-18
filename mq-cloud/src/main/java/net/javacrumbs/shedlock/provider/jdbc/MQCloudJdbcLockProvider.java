package net.javacrumbs.shedlock.provider.jdbc;

import net.javacrumbs.shedlock.support.StorageBasedLockProvider;

import javax.sql.DataSource;

/**
 * @Auther: yongfeigao
 * @Date: 2023/9/21
 */
public class MQCloudJdbcLockProvider extends StorageBasedLockProvider {
    public MQCloudJdbcLockProvider(DataSource datasource) {
        this(datasource, "shedlock");
    }

    public MQCloudJdbcLockProvider(DataSource datasource, String tableName) {
        super(new MQCloudJdbcStorageAccessor(datasource, tableName));
    }
}
