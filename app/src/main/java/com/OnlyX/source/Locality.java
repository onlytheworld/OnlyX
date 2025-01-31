package com.OnlyX.source;

import com.OnlyX.model.Chapter;
import com.OnlyX.model.Comic;
import com.OnlyX.model.ImageUrl;
import com.OnlyX.parser.MangaParser;
import com.OnlyX.parser.SearchIterator;

import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2017/5/21.
 */

public class Locality extends MangaParser {

    public static final int TYPE = -2;
    public static final String DEFAULT_TITLE = "本地漫画";

    public Locality() {
        mTitle = DEFAULT_TITLE;
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        return null;
    }

    @Override
    public String getUrl(String cid) {
        return null;
    }

    @Override
    public Request getInfoRequest(String cid) {
        return null;
    }

    @Override
    public void parseInfo(String html, Comic comic) {
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        return null;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        return null;
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
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

}
