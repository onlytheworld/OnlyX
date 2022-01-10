package com.OnlyX.ui.adapter;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.parser.Parser;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Map;

public abstract class ImageAdapter<T> extends BaseAdapter<T> {
    private final RequestManager _glide;
    private String _url;
    private Parser _source;
    private ImageView _comicImage;
    private boolean _cached = true;

    public ImageAdapter(Context context, List<T> list) {
        super(context, list);
        _glide = Glide.with(context);
    }


    public GlideUrl getImageGlideUrl(String url) {
        LazyHeaders.Builder lzHeader = new LazyHeaders.Builder();
        for (Map.Entry<String, String> entry : _source.getHeader().entrySet()) {
            lzHeader.addHeader(entry.getKey(), entry.getValue());
        }
        return new GlideUrl(url, lzHeader.build());
    }

    protected void setImage(ImageView view, String url, Parser source) {
        _comicImage = view;
        _url = url;
        _source = source;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (_cached)
            _glide.load(getImageGlideUrl(_url)).into(_comicImage);
        else {
            _glide.load(getImageGlideUrl(_url))
                    .apply(new RequestOptions().skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(_comicImage);
        }
        super.onBindViewHolder(holder, position);
    }

    public void setCache(boolean cached) {
        _cached = cached;
    }
}
