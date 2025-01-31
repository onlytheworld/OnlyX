package com.OnlyX.source;

import android.util.Pair;

import com.OnlyX.App;
import com.OnlyX.core.Manga;
import com.OnlyX.model.Chapter;
import com.OnlyX.model.Comic;
import com.OnlyX.model.ImageUrl;
import com.OnlyX.model.Source;
import com.OnlyX.parser.MangaCategory;
import com.OnlyX.parser.MangaParser;
import com.OnlyX.parser.NodeIterator;
import com.OnlyX.parser.SearchIterator;
import com.OnlyX.parser.UrlFilter;
import com.OnlyX.soup.Node;
import com.OnlyX.utils.DecryptionUtils;
import com.OnlyX.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by FEILONG on 2017/12/21.
 */

public class PuFei extends MangaParser {

    public static final int TYPE = 50;
    public static final String DEFAULT_TITLE = "扑飞漫画";
    private String baseurl = "http://m.pufei5.com/";
    String requestUrl;


    public PuFei(Source source) {
        init(source, new Category());
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        requestUrl = "";
        if (page == 1) {
            requestUrl = StringUtils.format(baseurl + "e/search/?searchget=1&tbname=mh&show=title,player,playadmin,bieming,pinyin,playadmin&tempid=4&keyboard=%s",
                    URLEncoder.encode(keyword, "GB2312"));
        }
        return new Request.Builder()
                .headers(Headers.of(getHeader())).url(requestUrl).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Document midHT = Jsoup.parse(html);
        try {
            String js = baseurl + midHT.head().select("script").first().attr("src").trim();
            Request request = new Request.Builder().headers(Headers.of(getHeader())).url(js).build();
            String jscript = Manga.getResponseBody(App.getHttpClient(), request);
            Element sc = midHT.body().selectFirst("script");
            jscript += midHT.body().selectFirst("script").val();
            String result = DecryptionUtils.evalDecrypt(jscript);
            request = new Request.Builder()
                    .headers(Headers.of(getHeader())).url(requestUrl).build();
            html = Manga.getResponseBody(App.getHttpClient(), request);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Node body = new Node(html);
        return new NodeIterator(body.list("#detail > li")) {
            @Override
            protected Comic parse(Node node) {
                Node node_a = node.list("a").get(0);
                String cid = node_a.hrefWithSplit(1);
                String title = node_a.text("h3");
                String cover = node_a.attr("div > img", "data-src");
                String author = node_a.text("dl > dd");
                String update = node.text("dl:eq(4) > dd");
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return baseurl + "manhua/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.pufei8.com"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = baseurl + "manhua/".concat(cid);
        return new Request.Builder()
                .headers(Headers.of(getHeader())).url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.main-bar > h1");
        String cover = body.src("div.book-detail > div.cont-list > div.thumb > img");
        String update = body.text("div.book-detail > div.cont-list > dl:eq(2) > dd");
        String author = body.text("div.book-detail > div.cont-list > dl:eq(3) > dd");
        String intro = body.text("#bookIntro");
        boolean status = isFinish(body.text("div.book-detail > div.cont-list > div.thumb > i"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("#chapterList2 > ul > li > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format(baseurl + "manhua/%s/%s.html", cid, path);
        return new Request.Builder()
                .headers(Headers.of(getHeader())).url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("cp=\"(.*?)\"", html, 1);
        if (str != null) {
            try {
                str = DecryptionUtils.evalDecrypt(DecryptionUtils.base64Decrypt(str));
                String[] array = str.split(",");
                for (int i = 0; i != array.length; ++i) {
                    list.add(new ImageUrl(i + 1, "http://res.img.youzipi.net/" + array[i], false));
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
        for (Node node : body.list("div.cont-list > ul > li")) {//li > a
            String cid = node.getChild("a").hrefWithSplit(1);//node.hrefWithSplit(1);
            String title = node.text("a > h3");
            String cover = node.attr("a > div > img", "data-src");
            String update = node.text("dl:eq(4) > dd");
            String author = node.text("a > dl:eq(2) > dd");
            list.add(new Comic(TYPE, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", baseurl);
        headers.put("User-Agent", windowsUA);
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Host", "m.pufei5.com");
        return headers;
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            return StringUtils.format("http://m.pufei.com/act/?act=list&page=%%d&catid=%s&ajax=1&order=%s",
                    args[CATEGORY_SUBJECT], args[CATEGORY_ORDER]);
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
            list.add(Pair.create("最近更新", "manhua/update"));
            list.add(Pair.create("漫画排行", "manhua/paihang"));
            list.add(Pair.create("少年热血", "shaonianrexue"));
            list.add(Pair.create("武侠格斗", "wuxiagedou"));
            list.add(Pair.create("科幻魔幻", "kehuan"));
            list.add(Pair.create("竞技体育", "jingjitiyu"));
            list.add(Pair.create("搞笑喜剧", "gaoxiaoxiju"));
            list.add(Pair.create("侦探推理", "zhentantuili"));
            list.add(Pair.create("恐怖灵异", "kongbulingyi"));
            list.add(Pair.create("少女爱情", "shaonvaiqing"));
            list.add(Pair.create("耽美BL", "danmeirensheng"));
//            list.add(Pair.create("恋爱生活", "9"));
            return list;
        }

        @Override
        protected boolean hasOrder() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getOrder() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("发布", "index"));
            list.add(Pair.create("更新", "update"));
            list.add(Pair.create("人气", "view"));
            return list;
        }

    }

}
