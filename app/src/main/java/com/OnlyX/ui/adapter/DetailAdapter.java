package com.OnlyX.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.R;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.model.Chapter;
import com.OnlyX.ui.widget.ChapterButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailAdapter extends BaseAdapter<Chapter> {

//    private PipelineDraweeControllerBuilderSupplier mControllerSupplier;

    private SourceManager.SMGetter mSMGetter;
    RequestManager mGlide;

    public String title;
    private String cover;
    private String update;
    private String author;
    public String intro;
    private String tags;
    private Boolean finish;

    private String last;
    private int source;

    public DetailAdapter(Context context, List<Chapter> list) {
        super(context, list);
        mGlide = Glide.with(context);
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildLayoutPosition(view);
                if (position == 0) {
                    outRect.set(0, 0, 0, 10);
                } else {
                    int offset = parent.getWidth() / 40;
                    outRect.set(offset, 0, offset, (int) (offset * 1.5));
                }
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = inflate(R.layout.item_chapter_header, parent, false);
            return new HeaderHolder(view);
        }
        View view = inflate(R.layout.item_chapter, parent, false);
        return new ChapterHolder(view);
    }

    public void setInfo(String cover, String title, String author, String intro,
                        Boolean finish, String update, String last, int source) {
        this.cover = cover;
        this.title = title;
        String[] intros = intro.split("@@");
        this.intro = intros[0];
        this.finish = finish;
        this.update = update;
        this.author = author;
        this.last = last;
        this.tags = intros.length == 1 ? "" : intros[1];
        this.source = source;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (position == 0) {
            HeaderHolder headerHolder = (HeaderHolder) holder;
            if (title != null) {
                if (cover != null) {
                    String referer = mSMGetter.getHeader(source).get("Referer");
                    String ua = mSMGetter.getHeader(source).get("User-Agent");
                    GlideUrl url = new GlideUrl(cover, new LazyHeaders.Builder()
                            .addHeader("Referer", referer == null ? "" : referer)
                            .addHeader("User-Agent", ua == null ? "" : ua)
                            .build());

                    mGlide.load(url).into(headerHolder.mComicImage);
//                    headerHolder.mComicImage.setController(mControllerSupplier.get().setUri(cover).build());
                }
                headerHolder.mComicTitle.setText(title);
                headerHolder.mComicIntro.setText(intro);
                headerHolder.mComicTags.setText(tags);
                if (finish != null) {
                    headerHolder.mComicStatus.setText(finish ? "完结" : "连载中");
                }
                if (update != null) {
                    headerHolder.mComicUpdate.setText("最后更新：".concat(update));
                }
                headerHolder.mComicAuthor.setText(author);
            }
        } else {
            Chapter chapter = get(position - 1);
            ChapterHolder viewHolder = (ChapterHolder) holder;
            viewHolder.chapterButton.setText(chapter.getTitle());
            viewHolder.chapterButton.setDownload(chapter.isComplete());
            if (chapter.getPath().equals(last)) {
                viewHolder.chapterButton.setSelected(true);
            } else if (viewHolder.chapterButton.isSelected()) {
                viewHolder.chapterButton.setSelected(false);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        final GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
        Objects.requireNonNull(manager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? manager.getSpanCount() : 1;
            }
        });
    }

    //    public void setControllerSupplier(PipelineDraweeControllerBuilderSupplier supplier) {
//        this.mControllerSupplier = supplier;
//    }
    public void setSMGetter(SourceManager.SMGetter getter) {
        mSMGetter = getter;
    }

    public void setLast(String value) {
        if (value == null || value.equals(last)) {
            return;
        }
        String temp = last;
        last = value;
        for (int i = 0; i != size(); ++i) {
            String path = get(i).getPath();
            if (path.equals(last)) {
                notifyItemChanged(i + 1);
            } else if (path.equals(temp)) {
                notifyItemChanged(i + 1);
            }
        }
    }

    static class ChapterHolder extends BaseViewHolder {
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.item_chapter_button)
        ChapterButton chapterButton;

        ChapterHolder(View view) {
            super(view);
        }
    }

    static class HeaderHolder extends BaseViewHolder {
        @BindView(R.id.item_header_comic_image)
        SimpleDraweeView mComicImage;
        @BindView(R.id.item_header_comic_title)
        TextView mComicTitle;
        @BindView(R.id.item_header_comic_intro)
        TextView mComicIntro;
        @BindView(R.id.item_header_comic_status)
        TextView mComicStatus;
        @BindView(R.id.item_header_comic_update)
        TextView mComicUpdate;
        @BindView(R.id.item_header_comic_author)
        TextView mComicAuthor;
        @BindView(R.id.item_header_comic_tag)
        TextView mComicTags;

        HeaderHolder(View view) {
            super(view);
        }
    }

}
