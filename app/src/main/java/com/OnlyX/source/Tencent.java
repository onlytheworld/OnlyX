package com.OnlyX.source;

import static com.OnlyX.utils.DecryptionUtils.evalDecrypt;

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
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Request;

/**
 * Created by FEILONG on 2017/12/21.
 * need fix
 */

public class Tencent extends MangaParser {

    public static final int TYPE = 51;
    public static final String DEFAULT_TITLE = "腾讯动漫";

    public Tencent(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1)
            url = "https://m.ac.qq.com/search/result?word=%s".concat(keyword);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list(".comic-item")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.attr("a", "href");
                cid = cid.substring("/comic/index/id/".length());
                String title = node.text(".comic-title");
                String cover = node.attr(".cover-image", "src");
                String update = node.text(".comic-update");
                String author = "UNKNOWN";
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "http://ac.qq.com/Comic/ComicInfo/id/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("ac.qq.com"));
        filter.add(new UrlFilter("m.ac.qq.com"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://m.ac.qq.com/comic/index/id/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.head-title-tags > h1");
        String cover = body.src("div.head-banner > img");
        String update = "";
        String author = body.text("li.author-wr");
        String intro = body.text("div.head-info-desc");
        boolean status = isFinish("连载中");//todo: fix here
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
        String url = "https://m.ac.qq.com/comic/chapterList/id/".concat(cid);
        return new Request.Builder()
                .url(url)
                .build();
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("ul.normal > li.chapter-item")) {
            String title = node.text("a");
            String path = node.href("a").substring("/chapter/index/id/518333/cid/".length());
            list.add(new Chapter(title, path));
        }
        return Lists.reverse(list);
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://m.ac.qq.com/chapter/index/id/%s/cid/%s", cid, path);
        return new Request.Builder()
                .url(url)
                .build();
    }

    private String splice(String str, int from, int length) {
        return str.substring(0, from) + str.substring(from + length);
    }

    private String decodeData(String str, String nonce) {
        nonce = evalDecrypt(nonce);
        Matcher m = Pattern.compile("\\d+[a-zA-Z]+").matcher(nonce);
        final List<String> matches = new ArrayList<>();
        while (m.find()) {
            matches.add(m.group(0));
        }
        int len = matches.size();
        while ((len--) != 0) {
            str = splice(str,
                    Integer.parseInt(Objects.requireNonNull(StringUtils.match("^\\d+", matches.get(len), 0))) & 255,
                    StringUtils.replaceAll(matches.get(len), "\\d+", "").length()
            );
        }
        return str;
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("data:\\s*'(.*)?',", html, 1);
        if (str != null) {
            try {
                str = DecryptionUtils.base64Decrypt(
                        decodeData(str, StringUtils.match("<script>window.*?=(.*?)<\\/script>", html, 1))
                );
                JSONObject object = new JSONObject(str);
                JSONArray array = object.getJSONArray("picture");
                for (int i = 0; i != array.length(); ++i) {
                    list.add(new ImageUrl(i + 1, array.getJSONObject(i).getString("url"), false));
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
        headers.put("Referer", "https://m.ac.qq.com");
        return headers;
    }

}
