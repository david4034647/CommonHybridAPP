package com.weiba.web.sharelibrary.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.weiba.web.sharelibrary.R;

import java.util.ArrayList;

/**
 * Created by lidong on 16/8/9.
 */
public class BVAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> data;

    public BVAdapter(Context context, ArrayList<String> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_bottom, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.item_img);
            holder.title = (TextView) convertView.findViewById(R.id.item_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(data.get(position));
        switch (holder.title.getText().toString()) {
            case "朋友圈":
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.circle));
                break;
            case "微信":
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.wchat));
                break;
            case "QQ空间":
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.qzone));
                break;
            case "QQ":
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.qq));
                break;
            case "新浪微博":
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.sina));
                break;
            case "复制链接":
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.clone_link));
                break;
            case "二维码":
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.zcode));
                break;
            default:
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.zcode));
                break;
        }
        return convertView;
    }

    class ViewHolder {
        ImageView imageView;
        TextView title;
    }
}
