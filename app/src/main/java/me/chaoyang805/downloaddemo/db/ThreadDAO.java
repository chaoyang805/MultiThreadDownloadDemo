package me.chaoyang805.downloaddemo.db;

import java.util.List;

import me.chaoyang805.downloaddemo.entities.ThreadInfo;

/**
 * Created by chaoyang805 on 2015/8/6.
 */
public interface ThreadDAO {
    /**
     * 插入线程
     * @param threadInfo
     */
    public void insertThread(ThreadInfo threadInfo);

    /**
     * 删除线程
     * @param url
     */
    public void deleteThread(String url);

    /**
     * 更新线程信息
     * @param url
     * @param threadId
     * @param finished
     */
    public void updateThread(String url, int threadId, int finished);

    /**
     * 通过文件URL查询所有的线程信息
     * @param url
     * @return
     */
    public List<ThreadInfo> getThreads(String url);

    /**
     * 判断线程是否存在于数据库中
     * @param url
     * @param threadId
     * @return
     */
    public boolean isExists(String url, int threadId);
}
