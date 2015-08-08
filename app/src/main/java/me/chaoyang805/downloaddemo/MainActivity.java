package me.chaoyang805.downloaddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.chaoyang805.downloaddemo.Services.DownloadService;
import me.chaoyang805.downloaddemo.entities.FileInfo;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private DownloadListAdapter mAdapter;
    private List<FileInfo> mList;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DownloadService.ACTION_UPDATE)) {
                int finished = intent.getIntExtra("finished", 0);
                int id = intent.getIntExtra("id", 0);
                mAdapter.updateProgress(id, finished);
            } else if (action.equals(DownloadService.ACTION_FINISHED)) {
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                mAdapter.updateProgress(fileInfo.getId(), 100);
                Toast.makeText(MainActivity.this,mList.get(fileInfo.getId()).getFileName() + "下载完成",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listview);

        mList = new ArrayList<>();
        mList.add(new FileInfo(0, "http://www.imooc.com/mobile/imooc.apk", "imooc.apk", 0, 0));
        mList.add(new FileInfo(1, "http://www.imooc.com/download/BaiduPlayerNetSetup_100.exe", "BaiduPlayerNetSetup_100.exe", 0, 0));
        mList.add(new FileInfo(2, "http://www.imooc.com/download/SoftMgr_Setup_S40054_SLC_N.exe", "SoftMgr_Setup_S40054_SLC_N.exe", 0, 0));
        mList.add(new FileInfo(3, "http://www.imooc.com/download/Activator.exe", "Activator.exe", 0, 0));
        mAdapter = new DownloadListAdapter(this, mList);
        mListView.setAdapter(mAdapter);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

}
