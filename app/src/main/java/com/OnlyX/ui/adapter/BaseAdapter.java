package com.OnlyX.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.global.FastClick;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<T> mDataSet;
    private final LayoutInflater mInflater;

    private OnItemClickListener mClickListener;
    private OnItemLongClickListener mLongClickListener;

    public BaseAdapter(Context context, List<T> list) {
        mDataSet = list;
        mInflater = LayoutInflater.from(context);
    }

    public View inflate(int layout, ViewGroup parent, boolean attach) {
        return mInflater.inflate(layout, parent, attach);
    }

    public T get(int position) {
        return mDataSet.get(position);
    }

    public int size() {
        return mDataSet.size();
    }

    public void add(T data) {
        if (mDataSet.add(data)) {
            notifyItemInserted(mDataSet.size());
        }
    }

    public void add(int location, T data) {
        mDataSet.add(location, data);
        notifyItemInserted(location);
    }

    public void addAll(Collection<T> collection) {
        addAll(mDataSet.size(), collection);
    }

    public void addAll(int location, Collection<T> collection) {
        if (mDataSet.addAll(location, collection)) {
            notifyItemRangeInserted(location, location + collection.size());
        }
    }

    public boolean exist(T data) {
        return mDataSet.contains(data);
    }

    public boolean remove(T data) {
        int position = mDataSet.indexOf(data);
        if (position != -1) {
            remove(position);
            return true;
        }
        return false;
    }

    public void remove(int position) {
        mDataSet.remove(position);
        notifyItemRemoved(position);
    }

    public boolean contains(T data) {
        return mDataSet.contains(data);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void reverse() {
        Collections.reverse(mDataSet);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    public List<T> getDateSet() {
        return mDataSet;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(Collection<T> collection) {
        mDataSet.clear();
        mDataSet.addAll(collection);
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        return mDataSet.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mLongClickListener = onItemLongClickListener;
    }

    public abstract RecyclerView.ItemDecoration getItemDecoration();

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            if (mClickListener != null && isClickValid()) {
                mClickListener.onItemClick(v, holder.getBindingAdapterPosition());
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (mLongClickListener == null) {
                return false;
            }
            return mLongClickListener.onItemLongClick(v, holder.getBindingAdapterPosition());
        });
    }

    protected boolean isClickValid() {
        return FastClick.isClickValid();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    static class BaseViewHolder extends RecyclerView.ViewHolder {
        BaseViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
