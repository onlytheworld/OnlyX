package com.OnlyX.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.OnlyX.R;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.model.Comic;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class ResultAdapter extends ImageAdapter<Comic> {
    private SourceManager.SMGetter mSMGetter;

    public ResultAdapter(Context context, List<Comic> list) {
        super(context, list);
        setCache(false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflate(R.layout.item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResultViewHolder viewHolder = (ResultViewHolder) holder;
        Comic comic = get(position);
        viewHolder.comicTitle.setText(comic.getTitle());
        viewHolder.comicAuthor.setText(comic.getAuthor());
        viewHolder.comicSource.setText(mSMGetter.getTitle(comic.getSource()));
        viewHolder.comicUpdate.setText(comic.getUpdate());
        viewHolder.comicType.setText(comic.getIntro());
        setImage(viewHolder.comicImage, comic.getCover(), mSMGetter.parser(comic.getSource()));
        super.onBindViewHolder(holder, position);
    }

    public void setSMGetter(SourceManager.SMGetter getter) {
        mSMGetter = getter;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int offset = parent.getWidth() / 90;
                outRect.set(0, 0, 0, offset);
            }
        };
    }

    static class ResultViewHolder extends BaseViewHolder {
        @BindView(R.id.result_comic_image)
        ImageView comicImage;
        @BindView(R.id.result_comic_title)
        TextView comicTitle;
        @BindView(R.id.result_comic_author)
        TextView comicAuthor;
        @BindView(R.id.result_comic_update)
        TextView comicUpdate;
        @BindView(R.id.result_comic_type)
        TextView comicType;
        @BindView(R.id.result_comic_source)
        TextView comicSource;

        ResultViewHolder(View view) {
            super(view);
        }
    }

}
