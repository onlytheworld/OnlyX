package com.OnlyX.parser;

import android.graphics.Bitmap;
import android.net.Uri;

import com.OnlyX.model.Comic;
import com.OnlyX.model.ImageUrl;
import com.OnlyX.model.Source;
import com.OnlyX.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/22.
 */
public abstract class MangaParser implements Parser {

    protected String mTitle;
    protected List<UrlFilter> filter = new ArrayList<>();
    private Category mCategory;
    protected static String windowsUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.93 Safari/537.36";
    protected static String mobileUA = "Mozilla/5.0 (Linux; Android 10; ELE-AL00 Build/HUAWEIELE-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/96.0.4664.92 Mobile Safari/537.36";

    protected void init(Source source, Category category) {
        mTitle = source.getTitle();
        mCategory = category;

        initUrlFilterList();
    }

    protected void initUrlFilterList() {
//        filter.add(new UrlFilter("manhua.dmzj.com", "/(\\w+)", 1));
    }

    @Override
    public Request login(String username, String password, boolean remember) {
        return null;
    }

    @Override
    public void additionalParser(String body) {
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
        return null;
    }

    @Override
    public Request getLazyRequest(String url) {
        return null;
    }

    @Override
    public String parseLazy(String html, String url) {
        return null;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return null;
    }

    @Override
    public String parseCheck(String html) {
        return null;
    }

    @Override
    public Category getCategory() {
        return mCategory;
    }

    @Override
    public Request getCategoryRequest(String format, int page) {
        String url = StringUtils.format(format, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        return null;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    protected boolean isFinish(String text) {
        return text != null && (text.contains("完结") || text.contains("Completed"));
    }

    @Override
    public String getUrl(String cid) {
        return cid;
    }

    @Override
    public Map<String, String> getHeader() {
        return null;
    }

    @Override
    public Headers getHeader(String url) {
        return Headers.of(getHeader());
    }

    @Override
    public Headers getHeader(List<ImageUrl> list) {
        return Headers.of(getHeader());
    }

    @Override
    public boolean isHere(Uri uri) {
        boolean val = false;
        for (UrlFilter uf : filter) {
            val |= (uri.getHost().contains(uf.Filter));
        }
        return val;
    }

    @Override
    public String getComicId(Uri uri) {
        for (UrlFilter uf : filter) {
            if (uri.getHost().contains(uf.Filter)) {
                return StringUtils.match(uf.Regex, uri.getPath(), uf.Group);
            }
        }
        return null;
    }

    @Override
    public void decodeImages(Bitmap bitmap, String url) {
    }

}
