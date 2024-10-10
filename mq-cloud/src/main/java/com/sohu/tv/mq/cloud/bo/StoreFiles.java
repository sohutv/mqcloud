package com.sohu.tv.mq.cloud.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sohu.tv.mq.cloud.common.util.WebUtil;
import com.sohu.tv.mq.util.JSONUtil;

/**
 * 存储文件
 * 
 * @author yongfeigao
 * @date 2020年11月27日
 */
public class StoreFiles {
    // 文件存储map
    private Map<StoreFileType, List<Entry>> storeEntryMap = new EnumMap<>(StoreFileType.class);

    // 总大小
    private long totalBytes;

    /**
     * 添加文件
     * 
     * @param file
     * @param size
     */
    public void addStoreFile(String file, long size) {
        totalBytes += size;
        // 获取文件类型
        StoreFileType storeFileType = StoreFileType.findStoreFileType(file);
        if (storeFileType == null) {
            return;
        }
        // 存储到map中
        List<Entry> entryList = storeEntryMap.get(storeFileType);
        if (entryList == null) {
            entryList = new ArrayList<>();
            storeEntryMap.put(storeFileType, entryList);
        }
        // 去除文件夹路径
        file = file.substring(storeFileType.getPath().length() + 1);
        if (!storeFileType.isFolder()) {
            Entry storeEntry = new StoreFile(file, size, storeFileType);
            storeEntry.addToList(entryList);
        } else {
            // 处理子文件
            for (Entry entry : entryList) {
                String folder = file.substring(0, file.indexOf("/"));
                if (entry.getName().equals(folder)) {
                    entry.addSubEntry(file, size);
                    entry.setSize(size);
                    return;
                }
            }
            Entry storeEntry = new StoreFolder(file, size, storeFileType);
            storeEntry.addToList(entryList);
            storeEntry.addSubEntry(file, size);
        }
    }

    public String getSize(StoreFileType storeFileType) {
        long size = 0;
        for (Entry entry : storeEntryMap.get(storeFileType)) {
            size += entry.getSize();
        }
        return WebUtil.sizeFormat(size);
    }

    public String toHumanReadableTotalBytes() {
        return WebUtil.sizeFormat(totalBytes);
    }

    public Map<StoreFileType, List<Entry>> getStoreEntryMap() {
        return storeEntryMap;
    }
    
    public List<Entry> getEntryList(StoreFileType storeFileType) {
        return storeEntryMap.get(storeFileType);
    }

    public Set<StoreFileType> keySet() {
        return storeEntryMap.keySet();
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public void sort() {
        for (StoreFileType storeFileType : storeEntryMap.keySet()) {
            List<Entry> entryList = storeEntryMap.get(storeFileType);
            Collections.sort(entryList);
            if (storeFileType.isFolder()) {
                for (Entry entry : entryList) {
                    Collections.sort(entry.subEntryList());
                }
            }
        }
    }

    /**
     * 存储文件类型
     * 
     * @author yongfeigao
     * @date 2020年12月10日
     */
    public static enum StoreFileType {
        CONFIG("/config"), 
        INDEX("/index"), 
        COMMITLOG("/commitlog"), 
        CONSUMEQUEUE("/consumequeue", true),
        ;
        private String path;
        private boolean folder;

        private StoreFileType(String path) {
            this.path = path;
        }
        
        private StoreFileType(String path, boolean folder) {
            this.path = path;
            this.folder = folder;
        }

        public String getPath() {
            return path;
        }
        
        public String getName() {
            return path.substring(1);
        }

        public boolean isFolder() {
            return folder;
        }

        public static StoreFileType findStoreFileType(String file) {
            for (StoreFileType storeFileType : values()) {
                if (file.startsWith(storeFileType.getPath())) {
                    return storeFileType;
                }
            }
            return null;
        }
        
        public static StoreFileType findStoreFileType(int ordinal) {
            for (StoreFileType storeFileType : values()) {
                if (storeFileType.ordinal() == ordinal) {
                    return storeFileType;
                }
            }
            return null;
        }
    }

    /**
     * 条目
     * 
     * @author yongfeigao
     * @date 2020年12月9日
     */
    public static abstract class Entry implements Comparable<Entry> {
        // entry id
        protected int id;
        // name
        protected String name;
        // size
        protected long size;
        // list
        protected List<Entry> subEntryList;

        protected int type;
        
        protected int subEntryListSize;

        public Entry() {
        }

        public Entry(String name, long size, StoreFileType type) {
            setName(name);
            setSize(size);
            this.type = type.ordinal();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public List<Entry> subEntryList() {
            return subEntryList;
        }

        public abstract Entry addSubEntry(String name, long size);

        public void setSubEntryList(List<Entry> subEntryList) {
            this.subEntryList = subEntryList;
        }
        
        public int getSubEntryListSize() {
            if (subEntryListSize != 0) {
                return subEntryListSize;
            }
            if (subEntryList == null) {
                return 0;
            }
            return subEntryList.size();
        }
        
        public void setSubEntryListSize(int subEntryListSize) {
            this.subEntryListSize = subEntryListSize;
        }

        public int getType() {
            return type;
        }
        
        public void addToList(List<Entry> entryList) {
            entryList.add(this);
            setId(entryList.size());
        }

        public void setType(int type) {
            this.type = type;
        }

        public String toHumanReadableSize() {
            return WebUtil.sizeFormat(size);
        }

        @Override
        public int compareTo(Entry o) {
            return name.compareTo(o.name);
        }

        public String toJsonString() {
            return JSONUtil.toJSONString(this);
        }
    }

    /**
     * 存储文件
     * 
     * @author yongfeigao
     * @date 2020年11月27日
     */
    public static class StoreFile extends Entry {
        
        private String parentName;
        
        public StoreFile() {
        }
        
        public StoreFile(String name, long size, StoreFileType storeFileType) {
            super(name, size, storeFileType);
        }

        @Override
        public Entry addSubEntry(String name, long size) {
            throw new UnsupportedOperationException();
        }

        public String toAbsoluteStorePath() {
            StoreFileType storeFileType = StoreFileType.findStoreFileType(type);
            if (parentName == null) {
                return storeFileType.getPath() + "/" + getName();
            }
            return storeFileType.getPath() + "/" + parentName + getName();
        }
        
        public String getParentName() {
            return parentName;
        }

        public void setParentName(String parentName) {
            this.parentName = parentName;
        }
    }

    public static class StoreFolder extends Entry {

        public StoreFolder(String name, long size, StoreFileType storeFileType) {
            super(name, size, storeFileType);
            setSubEntryList(new ArrayList<>());
        }

        public void setName(String name) {
            super.setName(name.substring(0, name.indexOf("/")));
        }

        public void setSize(long size) {
            super.setSize(getSize() + size);
        }

        public Entry addSubEntry(String name, long size) {
            StoreFile dataFile = new StoreFile(name.substring(getName().length()), size, StoreFileType.findStoreFileType(type));
            dataFile.setParentName(getName());
            subEntryList().add(dataFile);
            dataFile.setId(subEntryList().size());
            return dataFile;
        }
    }
}
