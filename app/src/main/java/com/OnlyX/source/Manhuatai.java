package com.OnlyX.source;



import com.OnlyX.App;
import com.OnlyX.core.Manga;
import com.OnlyX.model.Chapter;
import com.OnlyX.model.Comic;
import com.OnlyX.model.ImageUrl;
import com.OnlyX.model.Source;
import com.OnlyX.parser.JsonIterator;
import com.OnlyX.parser.MangaParser;
import com.OnlyX.parser.SearchIterator;
import com.OnlyX.soup.Node;
import com.OnlyX.utils.StringUtils;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Request;


/**
 * Created by reborn on 18-1-18.
 */

public class Manhuatai extends MangaParser {

    public static final int TYPE = 49;
    public static final String DEFAULT_TITLE = "漫画台";
    public static final String baseUrl = "https://m.manhuatai.com";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    public Manhuatai(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = StringUtils.format(baseUrl + "/api/getsortlist/?product_id=2&productname=mht&platformname=wap&orderby=click&search_key=%s&page=%d&size=48",
                URLEncoder.encode(keyword, "UTF-8"), page);

        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        JSONObject object = new JSONObject(html);

        return new JsonIterator(object.getJSONObject("data").getJSONArray("data")) {
            @Override
            protected Comic parse(JSONObject object) throws JSONException {
                String title = object.getString("comic_name");
                String cid = object.getString("comic_newid");
                String cover = "https://image.yqmh.com/mh/" + object.getString("comic_id") + ".jpg-300x400.webp";
                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    private Node getComicNode(String cid) throws Manga.NetworkErrorException {
        Request request = getInfoRequest(cid);
        String html = Manga.getResponseBody(App.getHttpClient(), request);
        return new Node(html);
    }

//    private String getResponseBody(OkHttpClient client, Request request) throws Manga.NetworkErrorException {
//        Response response = null;
//        try {
//            response = client.newCall(request).execute();
//            if (response.isSuccessful()) {
////                return response.body().string();
//
//                // 1.修正gb2312编码网页读取错误
//                byte[] bodybytes = response.body().bytes();
//                String body = new String(bodybytes);
//                if (body.indexOf("charset=gb2312") != -1) {
//                    body = new String(bodybytes, "GB2312");
//                }
//                return body;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (response != null) {
//                response.close();
//            }
//        }
//        throw new Manga.NetworkErrorException();
//    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://www.manhuatai.com/".concat(cid) + "/";
        return new Request.Builder().url(url).build();
    }

    //获取封面等信息（非搜索页）
    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.attr("h1#detail-title", "title");
//        String cover = body.src("#offlinebtn-container > img");//封面链接已改到style属性里了
        String cover = body.attr("div.detail-cover > img", "data-src");
        cover = "https:" + cover;
//        Log.i("Cover", cover);
        String update = body.text("span.update").substring(0,10);
        String intro = body.text("div#js_comciDesc > p.desc-content");
//        boolean status = isFinish(body.text("div.jshtml > ul > li:nth-child(2)").substring(3));
        comic.setInfo(title, cover, update, intro, null, false);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("ol#j_chapter_list > li > a")) {
            String title = node.attr( "title");
//            String path = node.hrefWithSplit(0);//于2018.3失效
            String path = node.hrefWithSplit(1);
//            Log.i("Path", path);
            list.add(new Chapter(title, path));
        }
        return Lists.reverse(list);
    }

    private String _path = null;

    //获取漫画图片Request
    @Override
    public Request getImagesRequest(String cid, String path) {
        _path = path;
        String url = StringUtils.format("https://m.manhuatai.com/api/getcomicinfo_body?product_id=2&productname=mht&platformname=wap&comic_newid=%s", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html)  {
        List<ImageUrl> list = new LinkedList<>();
        try {
            JSONObject object = new JSONObject(html);
            if (object.getInt("status") != 0) {
                return list;
            }

            JSONArray chapters = object.getJSONObject("data").getJSONArray("comic_chapter");
            JSONObject chapter = null;
            for (int i = 0; i < chapters.length(); i++) {
                chapter = chapters.getJSONObject(i);
                String a = chapter.getString("chapter_id");
                if(a.equals(_path)) {
                    break;
                }
            }

            String ImagePattern = "http://mhpic." + chapter.getString("chapter_domain") + chapter.getString("rule") + "-mht.low.webp";

            for (int index = chapter.getInt("start_num"); index <= chapter.getInt("end_num"); index++) {
                String image = ImagePattern.replaceFirst("\\$\\$", Integer.toString(index));
                list.add(new ImageUrl(index, image, false));
            }
        } catch (JSONException ex) {
            // ignore
        }

        return list;
    }


    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("div.jshtml > ul > li:nth-child(5)").substring(3);
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("a.sdiv")) {
            String cid = node.hrefWithSplit(0);
            String title = node.attr("title");
            String cover = node.getChild("img").attr("data-url");
//            String cover1 = node.attr("div > img", "data-url");
            Node node1 = null;
            try {
                node1 = getComicNode(cid);
            } catch (Manga.NetworkErrorException e) {
                e.printStackTrace();
            }
            if (StringUtils.isEmpty(cover) && node1 != null) {
//                cover = node.src("div > img");
                cover = node1.src("#offlinebtn-container > img");
            }
//            String update = node.text("div > span:nth-child(1)");
//            String author = "佚名";
//            String cover = null;
            String author = null;
            String update = null;
            if (node1 != null) {
//                cover = getComicNode(cid).src("#offlinebtn-container > img");
                author = node1.text("div.jshtml > ul > li:nth-child(3)").substring(3);
                update = node1.text("div.jshtml > ul > li:nth-child(5)").substring(3);
            }
            list.add(new Comic(TYPE, cid, title, cover, update, author));
        }
        return list;
    }
//
//    private static class Category extends MangaCategory {
//
//        @Override
//        public boolean isComposite() {
//            return true;
//        }
//
//        @Override
//        public String getFormat(String... args) {
//            return StringUtils.format("https://www.manhuatai.com/%s_p%%d.html",
//                    args[CATEGORY_SUBJECT]);
//        }
//
//        @Override
//        public List<Pair<String, String>> getSubject() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部漫画", "all"));
//            list.add(Pair.create("知音漫客", "zhiyinmanke"));
//            list.add(Pair.create("神漫", "shenman"));
//            list.add(Pair.create("风炫漫画", "fengxuanmanhua"));
//            list.add(Pair.create("漫画周刊", "manhuazhoukan"));
//            list.add(Pair.create("飒漫乐画", "samanlehua"));
//            list.add(Pair.create("飒漫画", "samanhua"));
//            list.add(Pair.create("漫画世界", "manhuashijie"));
////            list.add(Pair.create("排行榜", "top"));
//
////            list.add(Pair.create("热血", "rexue"));
////            list.add(Pair.create("神魔", "shenmo"));
////            list.add(Pair.create("竞技", "jingji"));
////            list.add(Pair.create("恋爱", "lianai"));
////            list.add(Pair.create("霸总", "bazong"));
////            list.add(Pair.create("玄幻", "xuanhuan"));
////            list.add(Pair.create("穿越", "chuanyue"));
////            list.add(Pair.create("搞笑", "gaoxiao"));
////            list.add(Pair.create("冒险", "maoxian"));
////            list.add(Pair.create("萝莉", "luoli"));
////            list.add(Pair.create("武侠", "wuxia"));
////            list.add(Pair.create("社会", "shehui"));
////            list.add(Pair.create("都市", "dushi"));
////            list.add(Pair.create("漫改", "mangai"));
////            list.add(Pair.create("杂志", "zazhi"));
////            list.add(Pair.create("悬疑", "xuanyi"));
////            list.add(Pair.create("恐怖", "kongbu"));
////            list.add(Pair.create("生活", "shenghuo"));
//            return list;
//        }
//
//        @Override
//        protected boolean hasOrder() {
//            return false;
//        }
//
//        @Override
//        protected List<Pair<String, String>> getOrder() {
////            List<Pair<String, String>> list = new ArrayList<>();
////            list.add(Pair.create("更新", "update"));
////            list.add(Pair.create("发布", "index"));
////            list.add(Pair.create("人气", "view"));
//            return null;
//        }
//
//    }

    @Override
    public Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://m.manhuatai.com");
        return headers;
    }

}
