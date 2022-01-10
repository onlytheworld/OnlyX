package com.OnlyX.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.R;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/12/6.
 */

public class DirAdapter extends BaseAdapter<String> {

    public DirAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflate(R.layout.item_dir, parent, false);
        return new DirHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        DirHolder viewHolder = (DirHolder) holder;
        viewHolder.mDirTitle.setText(get(position));
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return null;
    }

    static class DirHolder extends BaseAdapter.BaseViewHolder {
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.item_dir_title)
        TextView mDirTitle;

        DirHolder(View view) {
            super(view);
        }
    }

}
