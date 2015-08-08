package me.chaoyang805.downloaddemo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import me.chaoyang805.downloaddemo.Services.DownloadService;
import me.chaoyang805.downloaddemo.entities.FileInfo;

/**
 * Created by chaoyang805 on 2015/8/8.
 */
public class DownloadListAdapter extends BaseAdapter {

    private Context mContext;
    private List<FileInfo> mList;
    private ViewHolder holder = null;

    public DownloadListAdapter(Context context, List<FileInfo> list) {
        this.mContext = context;
        this.mList = list;
    }

    public void updateProgress(int id,int progress) {
        FileInfo fileInfo = getItem(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public FileInfo getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null, false);
            holder = new ViewHolder();
            holder.tvFileName = (TextView) convertView.findViewById(R.id.tv_file_name);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressbar);
            holder.btnStart = (Button) convertView.findViewById(R.id.btn_start);
            holder.btnStop = (Button) convertView.findViewById(R.id.btn_stop);

            holder.tvFileName.setText(getItem(position).getFileName());
            holder.progressBar.setMax(100);
            holder.btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileInfo fileInfo = getItem(position);
                    Intent i = new Intent(mContext, DownloadService.class);
                    i.setAction(DownloadService.ACTION_START);
                    i.putExtra("fileInfo", fileInfo);
                    mContext.startService(i);
                }
            });
            holder.btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileInfo fileInfo = getItem(position);
                    Intent i = new Intent(mContext, DownloadService.class);
                    i.setAction(DownloadService.ACTION_STOP);
                    i.putExtra("fileInfo", fileInfo);
                    mContext.startService(i);
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.progressBar.setProgress(getItem(position).getFinished());
        return convertView;
    }

    static class ViewHolder{

        TextView tvFileName;
        ProgressBar progressBar;
        Button btnStart,btnStop;

    }
}
