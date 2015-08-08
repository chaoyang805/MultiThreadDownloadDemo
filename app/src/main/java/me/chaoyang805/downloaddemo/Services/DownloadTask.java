package me.chaoyang805.downloaddemo.Services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.chaoyang805.downloaddemo.db.ThreadDAO;
import me.chaoyang805.downloaddemo.db.ThreadDAOImpl;
import me.chaoyang805.downloaddemo.entities.FileInfo;
import me.chaoyang805.downloaddemo.entities.ThreadInfo;

/**
 * Created by chaoyang805 on 2015/8/6.
 */
public class DownloadTask {

    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDAO mThreadDao;
    private int mFinished = 0;
    public boolean isPause = false;
    private int mThreadCount;
    private List<DownloadThread> mThreadList;
    public static ExecutorService sExecytors = Executors.newCachedThreadPool();

    public DownloadTask(Context context, FileInfo fileInfo, int threadCount) {
        this.mThreadCount = threadCount;
        this.mContext = context;
        this.mFileInfo = fileInfo;
        mThreadDao = new ThreadDAOImpl(mContext);

    }

    public void download() {
        //读取数据库的线程信息
        List<ThreadInfo> threads = mThreadDao.getThreads(mFileInfo.getUrl());
        int length = mFileInfo.getLength() / mThreadCount;
        ThreadInfo threadInfo = null;
        if (threads.size() == 0) {
            threads = new ArrayList<>(mThreadCount);
            for (int i = 0; i < mThreadCount; i++) {
                threadInfo = new ThreadInfo(mFileInfo.getId(), mFileInfo.getUrl(),
                        i * length, (i + 1) * length - 1, mFileInfo.getFinished());
                if (i == mThreadCount - 1) {
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                threads.add(threadInfo);
                //向数据库插入线程信息
                mThreadDao.insertThread(threadInfo);
            }
        }
        mThreadList = new ArrayList<>();
        for (ThreadInfo info : threads) {
            DownloadThread thread = new DownloadThread(info);
            sExecytors.execute(thread);
            mThreadList.add(thread);

        }

    }

    public synchronized void checkAllThreadsFinished() {
        boolean allFinished = true;
        for (DownloadThread downloadThread : mThreadList) {
            if (!downloadThread.isFinished) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {
            Intent intent = new Intent(DownloadService.ACTION_FINISHED);
            intent.putExtra("fileInfo", mFileInfo);
            mContext.sendBroadcast(intent);
            //删除下载信息
            mThreadDao.deleteThread(mFileInfo.getUrl());
        }
    }

    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;
        private boolean isFinished = false;

        public DownloadThread(ThreadInfo threadInfo) {
            this.mThreadInfo = threadInfo;
        }

        @Override
        public void run() {

            //设置下载位置
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream in = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                //开始下载
                if (conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {
                    //读取数据
                    in = conn.getInputStream();
                    byte[] buffer = new byte[4 * 1024];
                    int length = -1;
                    mFinished += mThreadInfo.getFinished();
                    Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                    long time = System.currentTimeMillis();
                    while ((length = in.read(buffer)) != -1) {
                        //写入文件
                        //将buffer中的数据写入到raf中去
                        raf.write(buffer, 0, length);
                        //下载进度发送广播给Activity
                        mFinished += length; // 累加整个文件的完成进度
                        mThreadInfo.setFinished(mThreadInfo.getFinished() + length); //累计整个线程的完成进度
                        if (System.currentTimeMillis() - time > 1000) {
                            time = System.currentTimeMillis();
                            Log.d("TAG", "Finished:>>>>>>" + mFinished * 100 / mFileInfo.getLength());
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            intent.putExtra("id", mFileInfo.getId());
                            mContext.sendBroadcast(intent);
                        }
                        //下载暂停时保存下载进度
                        if (isPause) {
                            mThreadDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
                            return;
                        }
                    }
                    isFinished = true;

                    //检查下载任务是否执行完毕
                    checkAllThreadsFinished();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
                try {
                    in.close();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
