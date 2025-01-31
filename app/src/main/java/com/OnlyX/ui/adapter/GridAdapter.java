package com.OnlyX.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.App;
import com.OnlyX.R;
import com.OnlyX.fresco.ControllerBuilderProvider;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.model.MiniComic;
import com.OnlyX.utils.FrescoUtils;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class GridAdapter extends ImageAdapter<MiniComic> {

    public static int TYPE_GRID = 2016101213;

    private ControllerBuilderProvider mProvider;
    private SourceManager.SMGetter mSMGetter;
    private boolean symbol = false;

    public GridAdapter(Context context, List<MiniComic> list) {
        super(context, list);
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_GRID;
    }

    @NonNull
    @Override
    public GridHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflate(R.layout.item_grid, parent, false);
        return new GridHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MiniComic comic = get(position);
        GridHolder gridHolder = (GridHolder) holder;
        gridHolder.comicTitle.setText(comic.getTitle());
        gridHolder.comicSource.setText(mSMGetter.getTitle(comic.getSource()));
        if (mProvider != null) {
//            ImageRequest request = ImageRequestBuilder
//                    .newBuilderWithSource(Uri.parse(comic.getCover()))
//                    .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
//                    .build();
            ImageRequest request = null;
            try {
                if (!App.getManager_wifi().isWifiEnabled() && App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_CONNECT_ONLY_WIFI, false)) {
//                    request = null;
                    if (FrescoUtils.isCached(comic.getCover())) {
                        request = ImageRequestBuilder
                                .newBuilderWithSource(Uri.fromFile(FrescoUtils.getFileFromDiskCache(comic.getCover())))
                                .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
                                .build();
                    }
                } else if (!App.getManager_wifi().isWifiEnabled() && App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_LOADCOVER_ONLY_WIFI, false)) {
//                    request = null;
                    if (FrescoUtils.isCached(comic.getCover())) {
                        request = ImageRequestBuilder
                                .newBuilderWithSource(Uri.fromFile(FrescoUtils.getFileFromDiskCache(comic.getCover())))
                                .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
                                .build();
                    }
                } else {
                    if (FrescoUtils.isCached(comic.getCover())) {
                        request = ImageRequestBuilder
                                .newBuilderWithSource(Uri.fromFile(FrescoUtils.getFileFromDiskCache(comic.getCover())))
                                .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
                                .build();
                    } else {
                        request = ImageRequestBuilder
                                .newBuilderWithSource(Uri.parse(comic.getCover()))
                                .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
                                .build();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            DraweeController controller = mProvider.get(comic.getSource())
//                    .setOldController(gridHolder.comicImage.getController())
//                    .setImageRequest(request)
//                    .build();

//            String referer = mSMGetter.getHeader(comic.getSource()).get("Referer");
//            String ua = mSMGetter.getHeader(comic.getSource()).get("User-Agent");
//            GlideUrl url = new GlideUrl(comic.getCover(), new LazyHeaders.Builder()
//                    .addHeader("Referer", referer == null ? "" : referer)
//                    .addHeader("User-Agent", ua == null ? "" : ua)
//                    .build());
//
//            mGlide.load(url).into(gridHolder.comicImage);
//            gridHolder.comicImage.setController(controller);
        }
        gridHolder.comicHighlight.setVisibility(symbol && comic.isHighlight() ? View.VISIBLE : View.INVISIBLE);
        setImage(gridHolder.comicImage, comic.getCover(),mSMGetter.parser(comic.getSource()));
        super.onBindViewHolder(holder, position);
    }

    public void setProvider(ControllerBuilderProvider provider) {
        mProvider = provider;
    }

    public void setSMGetter(SourceManager.SMGetter getter) {
        mSMGetter = getter;
    }

    public void setSymbol(boolean symbol) {
        this.symbol = symbol;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int offset = parent.getWidth() / 90;
                outRect.set(offset, 0, offset, (int) (2.8 * offset));
            }
        };
    }

    public void removeItemById(long id) {
        for (MiniComic comic : getDateSet()) {
            if (id == comic.getId()) {
                remove(comic);
                break;
            }
        }
    }

    public int findFirstNotHighlight() {
        int count = 0;
        if (symbol) {
            for (MiniComic comic : getDateSet()) {
                if (!comic.isHighlight()) {
                    break;
                }
                ++count;
            }
        }
        return count;
    }

    public void cancelAllHighlight() {
        int count = 0;
        for (MiniComic comic : getDateSet()) {
            if (!comic.isHighlight()) {
                break;
            }
            ++count;
            comic.setHighlight(false);
        }
        notifyItemRangeChanged(0, count);
    }

    public void moveItemTop(MiniComic comic) {
        if (remove(comic)) {
            add(findFirstNotHighlight(), comic);
        }
    }

    static class GridHolder extends BaseViewHolder {
        @BindView(R.id.item_grid_image)
        ImageView comicImage;
        @BindView(R.id.item_grid_title)
        TextView comicTitle;
        @BindView(R.id.item_grid_subtitle)
        TextView comicSource;
        @BindView(R.id.item_grid_symbol)
        View comicHighlight;

        GridHolder(View view) {
            super(view);
        }
    }

}
