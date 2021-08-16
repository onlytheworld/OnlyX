package com.OnlyX.source;

import android.util.Pair;

import com.OnlyX.model.Chapter;
import com.OnlyX.model.Comic;
import com.OnlyX.model.ImageUrl;
import com.OnlyX.model.Source;
import com.OnlyX.parser.JsonIterator;
import com.OnlyX.parser.MangaCategory;
import com.OnlyX.parser.MangaParser;
import com.OnlyX.parser.SearchIterator;
import com.OnlyX.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class SourcePaser extends MangaParser {

    public static final int TYPE = 1;
    public static final String DEFAULT_TITLE = "动漫之家";

    public SourcePaser(Source source) {
        init(source, new Category());
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = StringUtils.format("http://s.acg.dmzj.com/comicsum/search.php?s=%s", keyword, page - 1);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        try {
            Pattern p = Pattern.compile("\\[(\\{.*?\\})+,?\\]");
            Matcher m = p.matcher(html);
            if (!m.find())
                return null;

            return new JsonIterator(new JSONArray(m.group(0))) {
                @Override
                protected Comic parse(JSONObject object) {
                    try {
                        String cid = object.getString("id");
                        String title = object.getString("comic_name");
                        String cover = object.getString("cover");
                        String author = object.optString("comic_author");
                        return new Comic(TYPE, cid, title, cover, null, author);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getUrl(String cid) {
        return StringUtils.format("http://m.dmzj.com/info/%s.html", cid);
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("http://api.dmzj.com/dynamic/comicinfo/%s.json", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        try {
            JSONObject object = new JSONObject(html);
            JSONObject data = object.getJSONObject("data");
            JSONObject info = data.getJSONObject("info");
            String title = info.getString("title");
            String cover = info.getString("cover");
            Long time = info.has("last_updatetime") ? info.getLong("last_updatetime") * 1000 : null;
            String update = time == null ? null : StringUtils.getFormatTime("yyyy-MM-dd", time);
            String intro = info.optString("description");
            String authors = info.getString("authors");
            boolean status = info.getString("status")=="连载中"?true:false;
            comic.setInfo(title, cover, update, intro, authors, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        try {
            JSONObject object = new JSONObject(html);
            JSONArray array = object.getJSONObject("data").getJSONArray("list");
            for (int i = 0; i != array.length(); ++i) {
                JSONObject chapter = array.getJSONObject(i);
                String title = chapter.getString("chapter_name");
                String path = chapter.getString("id");
                list.add(new Chapter(title, path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.dmzj.com/chapinfo/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        try {
            JSONObject object = new JSONObject(html);
            JSONArray array = object.getJSONArray("page_url");
            for (int i = 0; i < array.length(); ++i) {
                list.add(new ImageUrl(i + 1, array.getString(i).replace("//g/","/g/"), false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        try {
            JSONObject object = new JSONObject(html);
            long time = object.getLong("last_updatetime") * 1000;
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        try {
            JSONArray array = new JSONArray(html);
            for (int i = 0; i != array.length(); ++i) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    if (object.optInt("hidden", 1) != 1) {
                        String cid = object.getString("id");
                        String title = object.getString("name");
                        String cover = "http://images.dmzj.com/".concat(object.getString("cover"));
                        Long time = object.has("last_updatetime") ? object.getLong("last_updatetime") * 1000 : null;
                        String update = time == null ? null : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(time));
                        String author = object.optString("authors");
                        list.add(new Comic(TYPE, cid, title, cover, update, author));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.dmzj.com/");
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            return StringUtils.format("http://m.dmzj.com/classify/%s-%s-%s-all-%s-%s-%%d.json",
                    args[CATEGORY_SUBJECT], args[CATEGORY_READER], args[CATEGORY_PROGRESS], args[CATEGORY_AREA], args[CATEGORY_ORDER]);
        }

        @Override
        public List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "0"));
            list.add(Pair.create("冒险", "1"));
            list.add(Pair.create("欢乐向", "2"));
            list.add(Pair.create("格斗", "3"));
            list.add(Pair.create("科幻", "4"));
            list.add(Pair.create("爱情", "5"));
            list.add(Pair.create("竞技", "6"));
            list.add(Pair.create("魔法", "7"));
            list.add(Pair.create("校园", "8"));
            list.add(Pair.create("悬疑", "9"));
            list.add(Pair.create("恐怖", "10"));
            list.add(Pair.create("生活亲情", "11"));
            list.add(Pair.create("百合", "3243"));
            list.add(Pair.create("伪娘", "13"));
            list.add(Pair.create("耽美", "14"));
            list.add(Pair.create("后宫", "15"));
            list.add(Pair.create("萌系", "16"));
            list.add(Pair.create("治愈", "17"));
            list.add(Pair.create("武侠", "18"));
            list.add(Pair.create("职场", "19"));
            list.add(Pair.create("奇幻", "20"));
            list.add(Pair.create("节操", "21"));
            list.add(Pair.create("轻小说", "22"));
            list.add(Pair.create("搞笑", "23"));
            return list;
        }

        @Override
        public boolean hasArea() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getArea() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "0"));
            list.add(Pair.create("日本", "1"));
            list.add(Pair.create("内地", "2"));
            list.add(Pair.create("欧美", "3"));
            list.add(Pair.create("港台", "4"));
            list.add(Pair.create("韩国", "5"));
            list.add(Pair.create("其他", "6"));
            return list;
        }

        @Override
        public boolean hasReader() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getReader() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "0"));
            list.add(Pair.create("少年", "1"));
            list.add(Pair.create("少女", "2"));
            list.add(Pair.create("青年", "3"));
            return list;
        }

        @Override
        public boolean hasProgress() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getProgress() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "0"));
            list.add(Pair.create("连载", "1"));
            list.add(Pair.create("完结", "2"));
            return list;
        }

        @Override
        public boolean hasOrder() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getOrder() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("更新", "1"));
            list.add(Pair.create("人气", "0"));
            return list;
        }

    }

}
