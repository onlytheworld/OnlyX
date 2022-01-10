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
import com.OnlyX.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by FEILONG on 2017/12/21.
 */

public class Hhxxee extends MangaParser {

    public static final int TYPE = 59;
    public static final String DEFAULT_TITLE = "997700";

    public Hhxxee(Source source) {
        init(source, null);
    }

    private static final String serverstr = "http://20.125084.com/dm01/|http://20.125084.com/dm02/|http://20.125084.com/dm03/|http://20.125084.com/dm04/|http://20.125084.com/dm05/|http://20.125084.com/dm06/|http://20.125084.com/dm07/|http://20.125084.com/dm08/|http://20.125084.com/dm09/|http://20.125084.com/dm10/|http://20.125084.com/dm11/|http://20.125084.com/dm12/|http://20.125084.com/dm13/|http://20.125084.com/dm14/|http://20.125084.com/dm15/|http://20.125084.com/dm16/";
    private static final String[] servers = serverstr.split("\\|");

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1)
            url = "http://99770.hhxxee.com/search/s.aspx";
        RequestBody requestBodyPost = new FormBody.Builder()
                .add("tbSTxt", keyword)
                .build();
        return new Request.Builder().url(url)
                .post(requestBodyPost).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list(".cInfoItem")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.href(".cListTitle > a").substring("http://99770.hhxxee.com/comic/".length());
                String title = node.text(".cListTitle > span");
                title = title.substring(1, title.length() - 1);
                String cover = node.src(".cListSlt > img");
                String update = node.text(".cListh2 > span").substring(8);
                String author = node.text(".cl1_2").substring(3);
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "http://99770.hhxxee.com/comic/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("99770.hhxxee.com","(\\d+)$"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://99770.hhxxee.com/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text(".cTitle");
        String cover = body.src(".cDefaultImg > img");
        String update = "";
        String author = "";
        String intro = body.text(".cCon");
        comic.setInfo(title, cover, update, intro, author, false);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("#subBookListAct > div")) {
            String title = node.text("a");
            String path = node.hrefWithSplit("a", 2);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://99770.hhxxee.com/comic/%s/%s/", cid, path);
        return new Request.Builder().url(url).build();
    }

    private int getPictureServers(String url) {
        return Integer.parseInt(Objects.requireNonNull(StringUtils.match("ok\\-comic(\\d+)", url, 1))) - 1;
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("var sFiles=\"(.*?)\"", html, 1);
        if (str != null) {
            try {
                String[] array = str.split("\\|");
                for (int i = 0; i != array.length; ++i) {
                    list.add(new ImageUrl(i + 1, servers[getPictureServers(array[i])] + array[i], false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("div.book-detail > div.cont-list > dl:eq(2) > dd");
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("li > a")) {
            String cid = node.hrefWithSplit(1);
            String title = node.text("h3");
            String cover = node.attr("div > img", "data-src");
            String update = node.text("dl:eq(5) > dd");
            String author = node.text("dl:eq(2) > dd");
            list.add(new Comic(TYPE, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "http://99770.hhxxee.com");
        return headers;
    }


}
