package com.weiba.web.sharelibrary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weiba.web.sharelibrary.R;


/**
 * Created by lidong on 16/8/9.
 */
public class BottomAdapter extends RecyclerView.Adapter<BottomAdapter.BottomViewHolder> {
    private Context context;
    private String[] data;

    public BottomAdapter(Context context, String[] data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public BottomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BottomViewHolder holder = new BottomViewHolder(LayoutInflater.from(context).inflate(R.layout.item_bottom, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(BottomViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return data.length;
    }

    class BottomViewHolder extends RecyclerView.ViewHolder {

        public BottomViewHolder(View itemView) {
            super(itemView);
        }
    }
}
