package com.OnlyX.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.R;
import com.OnlyX.model.Tag;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class TagAdapter extends BaseAdapter<Tag> {

    private @ColorInt
    int color = -1;

    public TagAdapter(Context context, List<Tag> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflate(R.layout.item_tag, parent, false);
        return new TagHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Tag tag = get(position);
        TagHolder viewHolder = (TagHolder) holder;
        viewHolder.tagTitle.setText(tag.getTitle());
        if (color != -1) {
            viewHolder.tagTitle.setBackgroundColor(color);
        }
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int offset = parent.getWidth() / 90;
                outRect.set(offset, 0, offset, (int) (offset * 1.5));
            }
        };
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

    static class TagHolder extends BaseViewHolder {
        @BindView(R.id.item_tag_title)
        TextView tagTitle;

        TagHolder(final View view) {
            super(view);
        }
    }

}
