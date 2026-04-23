package com.sohu.tv.mq.cloud.util;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.rocksdb.*;
import org.rocksdb.AbstractCompactionFilter.Context;
import org.rocksdb.util.SizeUnit;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ConsumeQueueRocksdbAnalyser {
    public static void main(String[] args) throws Exception {
        String columnFamily = "default";
        String path1 = "D:/tmp/consumequeue";
        String path2 = "D:/tmp/consumequeue2";
        //analyseDiff(path1, path2, columnFamily);
        diff(path1, path2, columnFamily);
        // String path2Tmp = "D:/tmp/consumequeue2tmp";
        // compaction(path2Tmp);
    }

    public static void compaction(String path) throws Exception {
        RocksDB db = buildRocksDB(path, false, 8427799576576L);
        db.compactRange();
    }

    public static void diff(String path1, String path2, String columnFamily) throws Exception {
        RocksDB db1 = buildRocksDB(path1);
        RocksDB db2 = buildRocksDB(path2);
        RocksIterator it = db2.newIterator(getColumnFamilyHandle(db2, columnFamily));
        Map<String, AtomicInteger> topicQueueCounter = new HashMap<>();
        for (it.seekToFirst(); it.isValid(); it.next()) {
            byte[] key = it.key();
            byte[] value = db1.get(key);
            byte[] value2 = db2.get(key);
            if (value2 == null) {
                System.out.println("has deleted, key=" + toQueueOffset(key));
            }
            if (value == null) {
                QueueOffset queueOffset = toQueueOffset(key);
                topicQueueCounter.computeIfAbsent(queueOffset.topic, k -> new AtomicInteger()).incrementAndGet();
            }
        }
        topicQueueCounter.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, AtomicInteger> e) -> e.getValue().get()).reversed())
                .forEach(System.out::println);
        db1.close();
        db2.close();
    }

    public static void analyseDiff(String path1, String path2, String columnFamily) throws Exception {
        Set<byte[]> data1 = loadData(path1, columnFamily);
        Set<byte[]> data2 = loadData(path2, columnFamily);
        Pair<String, Set<byte[]>> pair1 = ImmutablePair.of(path1, data1);
        Pair<String, Set<byte[]>> pair2 = ImmutablePair.of(path2, data2);
        diff(pair1, pair2);
        diff(pair2, pair1);
    }

    public static void diff(Pair<String, Set<byte[]>> pair1, Pair<String, Set<byte[]>> pair2) {
        Set<byte[]> data1NotInData2 = new HashSet<>();
        for (byte[] data : pair1.getValue()) {
            if (!pair2.getValue().contains(data)) {
                data1NotInData2.add(data);
            }
        }
        System.out.println(pair1.getKey() + " not it " + pair2.getKey() + ", size=" + data1NotInData2.size());
        data1NotInData2.stream().forEach(d -> {
            System.out.println(toQueueOffset(d));
        });
    }

    public static Set<byte[]> loadData(String path, String columnFamily) throws Exception {
        RocksDB db = buildRocksDB(path);
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle(db, columnFamily);
        RocksIterator it = db.newIterator(columnFamilyHandle);
        Set<byte[]> result = new HashSet<>();
        for (it.seekToFirst(); it.isValid(); it.next()) {
            byte[] key = it.key();
            if (!result.add(key)) {
                System.out.println("path:" + path + ", columnFamily:" + columnFamily + ", exist:" + toQueueOffset(key));
            }
        }
        db.close();
        System.out.println("path:" + path + ", columnFamily:" + columnFamily + ", size=" + result.size());
        return result;
    }

    public static ColumnFamilyHandle getColumnFamilyHandle(RocksDB db, String columnFamily) throws Exception {
        Field field = RocksDB.class.getDeclaredField("ownedColumnFamilyHandles");
        field.setAccessible(true);
        List<ColumnFamilyHandle> columnFamilyHandles = (List<ColumnFamilyHandle>) field.get(db);
        for (ColumnFamilyHandle handle : columnFamilyHandles) {
            if (columnFamily.equals(new String(handle.getName()))) {
                return handle;
            }
        }
        return null;
    }

    public static RocksDB buildRocksDB(String path) throws Exception {
        return buildRocksDB(path, true, 0);
    }

    public static RocksDB buildRocksDB(String path, boolean readOnly, long minPhyOffset) throws Exception {
        List<byte[]> cfNames = RocksDB.listColumnFamilies(new Options(), path);
        List<ColumnFamilyDescriptor> cfDescriptors = cfNames.stream()
                .map(name -> {
                    System.out.println(path + "'s columnFamily:" + new String(name));
                    ColumnFamilyOptions columnFamilyOptions = new ColumnFamilyOptions();
                    columnFamilyOptions
                            .setCompactionFilterFactory(new AbstractCompactionFilterFactory() {
                                public AbstractCompactionFilter<?> createCompactionFilter(Context context) {
                                    return new RemoveConsumeQueueCompactionFilter(minPhyOffset);
                                }

                                public String name() {
                                    return "ConsumeQueueCompactionFilterFactory";
                                }
                            })
                            .setTargetFileSizeBase(256 * SizeUnit.MB);
                    return new ColumnFamilyDescriptor(name, columnFamilyOptions);
                })
                .collect(Collectors.toList());
        List<ColumnFamilyHandle> handles = new ArrayList<>();
        DBOptions dbOptions = new DBOptions()
                .setCreateIfMissing(false)
                .setDbLogDir("d:/tmp/rocksdblog")
                .setInfoLogLevel(InfoLogLevel.DEBUG_LEVEL);
        if (readOnly) {
            return RocksDB.openReadOnly(dbOptions, path, cfDescriptors, handles);
        }
        return RocksDB.open(dbOptions, path, cfDescriptors, handles);
    }

    public static QueueOffset toQueueOffset(byte[] key) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(key);
        int topicSize = byteBuffer.getInt();
        byteBuffer.get();
        byte[] topicByte = new byte[topicSize];
        byteBuffer.get(topicByte);
        String topic = new String(topicByte);
        byteBuffer.get();
        int queueId = byteBuffer.getInt();
        byteBuffer.get();
        long queueOffset = byteBuffer.getLong();
        QueueOffset queueOffsetObj = new QueueOffset();
        queueOffsetObj.topic = topic;
        queueOffsetObj.queueId = queueId;
        queueOffsetObj.offset = queueOffset;
        return queueOffsetObj;
    }

    public static class QueueOffset {
        String topic;
        int queueId;
        long offset;

        @Override
        public String toString() {
            return "QueueOffset{" +
                    "topic='" + topic + '\'' +
                    ", queueId=" + queueId +
                    ", offset=" + offset +
                    '}';
        }
    }
}
