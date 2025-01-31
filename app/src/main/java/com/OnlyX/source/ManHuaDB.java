package com.OnlyX.source;

import com.OnlyX.model.Chapter;
import com.OnlyX.model.Comic;
import com.OnlyX.model.ImageUrl;
import com.OnlyX.model.Source;
import com.OnlyX.parser.MangaParser;
import com.OnlyX.parser.NodeIterator;
import com.OnlyX.parser.SearchIterator;
import com.OnlyX.parser.UrlFilter;
import com.OnlyX.soup.Node;
import com.OnlyX.utils.DecryptionUtils;
import com.OnlyX.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Request;

/**
 * Created by ZhiWen on 2019/02/25.
 */

public class ManHuaDB extends MangaParser {

    public static final int TYPE = 46;
    public static final String DEFAULT_TITLE = "漫画DB";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    public ManHuaDB(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("https://www.manhuadb.com/search?q=%s", keyword);
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public String getUrl(String cid) {
        return "https://www.manhuadb.com/manhua/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.manhuadb.com"));
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("a.d-block")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit(1);
                String title = node.attr("title");
                String cover = node.attr("img", "data-original");
                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://www.manhuadb.com/manhua/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("h1.comic-title");
//        String cover = body.src("div.cover > img"); // 这一个封面可能没有
        String cover = body.src("td.comic-cover > img");
        String author = body.text("a.comic-creator");
        String intro = body.text("p.comic_story");
        boolean status = isFinish(body.text("a.comic-pub-state"));

        String update = body.text("a.comic-pub-end-date");
        if (update == null || update.equals("")) {
            update = body.text("a.comic-pub-date");
        }
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("#comic-book-list > div > ol > li > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(2);
            list.add(0, new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://www.manhuadb.com/manhua/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new ArrayList<>();

        try {
            final String imageHost = StringUtils.match("data-host=\"(.*?)\"", html, 1);
            final String imagePre = StringUtils.match("data-img_pre=\"(.*?)\"", html, 1);
            final String base64Data = StringUtils.match("var img_data = '(.*?)';", html, 1);
            final String jsonStr = DecryptionUtils.base64Decrypt(base64Data);
            final JSONArray imageList = JSON.parseArray(jsonStr);

            for(int i = 0; i < imageList.size(); i++ ) {
                final JSONObject image = imageList.getJSONObject(i);

                final String imageUrl = imageHost + imagePre + image.getString("img");

                list.add(new ImageUrl(image.getIntValue("p"), imageUrl, false));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
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
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        // 这里表示的是更新时间
        Node body = new Node(html);
        String update = body.text("a.comic-pub-end-date");
        if (update == null || update.equals("")) {
            update = body.text("a.comic-pub-date");
        }
        return update;
    }

    @Override
    public Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://www.manhuadb.com");
        return headers;
    }

}
