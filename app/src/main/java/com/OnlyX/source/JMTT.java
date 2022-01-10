package com.OnlyX.source;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Pair;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.OnlyX.App;
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
import com.OnlyX.utils.StringUtils;
import com.google.common.collect.Lists;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;

public class JMTT extends MangaParser {

    public static final int TYPE = 72;
    private static final String DEFAULT_TITLE = "禁漫天堂";
    private final String baseUrl = "https://jmcomic1.cc/";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    public JMTT(Source source) {
        init(source, new Category());
    }

    @Override
    public Request login(String username, String password, boolean remember) {
        String url = baseUrl + "login";
        String login_remember = remember ? "on" : "";
        String submit_login = "";
        FormBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("login_remember", login_remember)
                .add("submit_login", submit_login)
                .build();
        return new Request.Builder().post(formBody).headers(Headers.of(getHeader())).url(url).build();
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url;
        if (page != 1)
            url = StringUtils.format(baseUrl + "/search/photos?search_query=%s&main_tag=0&page=%d", keyword, page);
        else
            url = StringUtils.format(baseUrl + "/search/photos?search_query=%s&main_tag=0", keyword);
        return new Request.Builder().headers(Headers.of(getHeader())).url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        Node body = new Node(html);
        body = body.getChild("div#wrapper > div.container");
        return new NodeIterator(body.list(".col-xs-6")) {
            @Override
            protected Comic parse(Node node) {
                final String cid = node.href("div.thumb-overlay > a");
                final String title = node.text("span.video-title");
                final String cover = node.attr("div.thumb-overlay > a > img", "data-original");
                final String update = node.text("div.video-views");
                final String author = node.text("div.title-truncate > a");
                if (cid != null)
                    return new Comic(TYPE, cid, title, cover, update, author);
                return null;
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return baseUrl + cid;
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter(baseUrl));
        filter.add(new UrlFilter("https://18comic1.one/"));
        filter.add(new UrlFilter("https://18comic2.one/"));
        filter.add(new UrlFilter("https://18comic.vip"));
        filter.add(new UrlFilter("18comic.org"));
        filter.add(new UrlFilter("https://cm365.xyz/7MJX9t"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = baseUrl + cid;
        return new Request.Builder().headers(Headers.of(getHeader())).url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        try {
            Node body = new Node(html);
            String intro = body.text("#intro-block > div:eq(0)");
            String title = body.text("div.panel-heading > div");
            String cover = body.attr("img.lazy_img.img-responsive", "src").trim();
            String author = body.text("#intro-block > div:eq(4) > span");
            String update = body.attr("#album_photo_cover > div:eq(1) > div:eq(3)", "content");
            boolean status = isFinish(body.text("#intro-block > div:eq(2) > span"));
            comic.setInfo(title, cover, update, intro, author, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        String startTitle = body.text(".col.btn.btn-primary.dropdown-toggle.reading").trim();
        String startPath = body.href(".col.btn.btn-primary.dropdown-toggle.reading");
        list.add(new Chapter(startTitle, startPath));
        for (Node node : body.list("#episode-block > div > div.episode > ul > a")) {
            String title = node.text("li").trim();
            String path = node.href();
            list.add(new Chapter(title, path));
        }
        return Lists.reverse(list);
    }

    private String imgpath = "";

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = baseUrl + path;
        imgpath = path;
        return new Request.Builder().headers(Headers.of(getHeader())).url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new ArrayList<>();
        try {
            int i = 0;
            for (Node node : new Node(html).list("img.lazy_img")) {
                String img1 = node.attr("img", "src");
                String img2 = node.attr("img", "data-original");
                String[] reg = imgpath.split("\\/");
                if (img1.contains(reg[2])) {
                    list.add(new ImageUrl(++i, img1, false));
                } else if (img2.contains(reg[2])) {
                    list.add(new ImageUrl(++i, img2, false));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private int getRows(int aid, String imgIndex) {
        AtomicReference<String> result = new AtomicReference<>();
        AtomicBoolean flag = new AtomicBoolean(false);
        try {
            App.getActivity().runOnUiThread(() -> {
                WebView myWebView = new WebView(App.getActivity());
                WebSettings settings = myWebView.getSettings();
                settings.setJavaScriptEnabled(true);
                myWebView.setWebChromeClient(new WebChromeClient() {
                });
                myWebView.setWebViewClient(new WebViewClient() {

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        view.evaluateJavascript("javascript:get_num('" + aid + "', '" + imgIndex + "')", s -> {
                            result.set(s);
                            flag.set(true);
                        });
                    }
                });
                myWebView.loadUrl("file:///android_asset/md5.html");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (!flag.get()) {
        }
        return Integer.parseInt(result.get());
    }

    @Override
    public void decodeImages(Bitmap bitmap, String url) {
        int scrambleId = 220980;
        if (!url.contains("media/photos")) return;
        int aid = Integer.parseInt(url.substring(url.indexOf("photos/") + 7, url.lastIndexOf("/")));
        if (aid >= scrambleId) {
            String imgIndex = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
            int rows = getRows(aid, imgIndex);
//            int rows = DecryptionUtils.evalDecrypt(packed + "get_num('" + aid + "', '" + imgIndex + "')");
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            // 未除尽像素
            int remainder = (height % rows);
            // 创建新的图片对象
//            Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Bitmap resultBitmap = bitmap.copy(bitmap.getConfig(), bitmap.isMutable());
            Canvas canvas = new Canvas(bitmap);
            // 分割图片
            for (int x = 0; x < rows; x++) {
                // 分割算法(详情见html源码页的方法"function scramble_image(img)")
                int copyH = height / rows;
                int py = copyH * x;
                int y = height - (copyH * (x + 1)) - remainder;
                if (x == 0) {
                    copyH += remainder;
                } else {
                    py += remainder;
                }
                // 要裁剪的区域
                Rect crop = new Rect(0, y, width, y + copyH);
                // 裁剪后应放置到新图片对象的区域
                Rect splitC = new Rect(0, py, width, py + copyH);
                canvas.drawBitmap(resultBitmap, crop, splitC, null);
            }
        }
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).attr("#album_photo_cover > div:eq(1) > div:eq(3)", "content");
    }

    @Override
    public Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", baseUrl);
        return headers;
    }

    private static class Category extends MangaCategory {

        @Override
        public String getFormat(String... args) {
            return "";
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            return new ArrayList<>();
        }
    }
}