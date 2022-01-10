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
import com.OnlyX.parser.UrlFilter;
import com.OnlyX.soup.Node;
import com.OnlyX.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class Pica extends MangaParser {

    public static final int TYPE = 99;
    public static final String DEFAULT_TITLE = "哔咔漫画";
    public static final String baseUrl = "https://picaapi.picacomic.com/";
    PicaHeader picHelper;


    public Pica(Source source) {
        init(source, new Category());
        picHelper = new PicaHeader();
    }

    @Override
    public Request login(String username, String password, boolean remember) {
        String url = baseUrl + "auth/sign-in";
        FormBody formBody = new FormBody.Builder()
                .add("email", username)
                .add("password", password)
                .build();
        picHelper.setUrl(url);
        picHelper.setMethod(PicaHeader.Method.POST);
        picHelper.setContentType("application/json; charset=UTF-8");
        picHelper.setChannel(3);
        return new Request.Builder().post(formBody)
                .headers(Headers.of(picHelper.getHttpHeader())).url(url).build();
    }

    @Override
    public void additionalParser(String body) {
        try {
            JSONObject response = new JSONObject(body);
            picHelper.setAuthorization(response.getJSONObject("data").getString("token"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter(baseUrl));
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("https://v3api.dmzj.com/search/show/0/%s/%d.json", keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        try {
            return new JsonIterator(new JSONArray(html)) {
                @Override
                protected Comic parse(JSONObject object) {
                    try {
                        String cid = object.getString("id");
                        String title = object.getString("title");
                        String cover = object.getString("cover");
                        String author = object.optString("authors");
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
        String url = StringUtils.format("http://m.dmzj.com/info/%s.html", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String intro = body.textWithSubstring("p.txtDesc", 3);
        String title = body.attr("#Cover > img", "title");
        String cover = body.src("#Cover > img");
        String author = body.text("div.Introduct_Sub > div.sub_r > p:eq(0) > a");
        String update = body.textWithSubstring("div.Introduct_Sub > div.sub_r > p:eq(3) > span.date", 0, 10);
        boolean status = isFinish(body.text("div.Introduct_Sub > div.sub_r > p:eq(2) > a:eq(3)"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        String jsonString = StringUtils.match("initIntroData\\((.*?)\\);", html, 1);
        List<Chapter> list = new LinkedList<>();
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                for (int i = 0; i != array.length(); ++i) {
                    JSONArray data = array.getJSONObject(i).getJSONArray("data");
                    for (int j = 0; j != data.length(); ++j) {
                        JSONObject object = data.getJSONObject(j);
                        String title = object.getString("chapter_name");
                        String path = object.getString("id");
                        list.add(new Chapter(title, path));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        String jsonString = StringUtils.match("\"page_url\":(\\[.*?\\]),", html, 1);
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                for (int i = 0; i != array.length(); ++i) {
                    list.add(new ImageUrl(i + 1, array.getString(i).replace("dmzj", "dmzj1").replace("//g/", "/g/"), false));
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
        return new Node(html).textWithSubstring("div.Introduct_Sub > div.sub_r > p:eq(3) > span.date", 0, 10);
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        try {
            JSONArray array = new JSONArray(html);
            for (int i = 0; i != array.length(); ++i) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    String cid = object.getString("id");
                    String title = object.getString("title");
                    String cover = object.getString("cover");
                    Long time = object.has("last_updatetime") ? object.getLong("last_updatetime") * 1000 : null;
                    String update = time == null ? null : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(time));
                    String author = object.optString("authors");
                    list.add(new Comic(TYPE, cid, title, cover, update, author));
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
    public Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("api-key", "C69BAF41DA5ABD1FFEDC6D2FEA56B");
        headers.put("accept", "application/vnd.picacomic.com.v1+json");
        headers.put("app-channel", "2");
        String ts = String.valueOf(System.currentTimeMillis()).substring(0, 10);
        headers.put("time", ts);
        headers.put("nonce", UUID.randomUUID().toString().replace("-", ""));
        headers.put("app-version", "2.2.1.2.3.3");
        headers.put("app-uuid", "983f6ed7-e528-3129-a125-19a5427d603f");
        headers.put("app-platform", "android");
        headers.put("app-build-version", "44");
        headers.put("Content-Type", "application/json; charset=UTF-8");
        headers.put("image-quality", "original");
        headers.put("User-Agent", "okhttp/3.8.1");
        return headers;
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            String path = args[CATEGORY_SUBJECT].concat(" ").concat(args[CATEGORY_READER]).concat(" ").concat(args[CATEGORY_PROGRESS])
                    .concat(" ").concat(args[CATEGORY_AREA]).trim();
            if (path.isEmpty()) {
                path = String.valueOf(0);
            } else {
                path = path.replaceAll("\\s+", "-");
            }
            return StringUtils.format("http://v2.api.dmzj.com/classify/%s/%s/%%d.json", path, args[CATEGORY_ORDER]);
        }

        @Override
        public List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("冒险", "4"));
            list.add(Pair.create("百合", "3243"));
            list.add(Pair.create("生活", "3242"));
            list.add(Pair.create("四格", "17"));
            list.add(Pair.create("伪娘", "3244"));
            list.add(Pair.create("悬疑", "3245"));
            list.add(Pair.create("后宫", "3249"));
            list.add(Pair.create("热血", "3248"));
            list.add(Pair.create("耽美", "3246"));
            list.add(Pair.create("其他", "16"));
            list.add(Pair.create("恐怖", "14"));
            list.add(Pair.create("科幻", "7"));
            list.add(Pair.create("格斗", "6"));
            list.add(Pair.create("欢乐向", "5"));
            list.add(Pair.create("爱情", "8"));
            list.add(Pair.create("侦探", "9"));
            list.add(Pair.create("校园", "13"));
            list.add(Pair.create("神鬼", "12"));
            list.add(Pair.create("魔法", "11"));
            list.add(Pair.create("竞技", "10"));
            list.add(Pair.create("历史", "3250"));
            list.add(Pair.create("战争", "3251"));
            list.add(Pair.create("魔幻", "5806"));
            list.add(Pair.create("扶她", "5345"));
            list.add(Pair.create("东方", "5077"));
            list.add(Pair.create("奇幻", "5848"));
            list.add(Pair.create("轻小说", "6316"));
            list.add(Pair.create("仙侠", "7900"));
            list.add(Pair.create("搞笑", "7568"));
            list.add(Pair.create("颜艺", "6437"));
            list.add(Pair.create("性转换", "4518"));
            list.add(Pair.create("高清单行", "4459"));
            list.add(Pair.create("治愈", "3254"));
            list.add(Pair.create("宅系", "3253"));
            list.add(Pair.create("萌系", "3252"));
            list.add(Pair.create("励志", "3255"));
            list.add(Pair.create("节操", "6219"));
            list.add(Pair.create("职场", "3328"));
            list.add(Pair.create("西方魔幻", "3365"));
            list.add(Pair.create("音乐舞蹈", "3326"));
            list.add(Pair.create("机战", "3325"));
            return list;
        }

        @Override
        public boolean hasArea() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getArea() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("日本", "2304"));
            list.add(Pair.create("韩国", "2305"));
            list.add(Pair.create("欧美", "2306"));
            list.add(Pair.create("港台", "2307"));
            list.add(Pair.create("内地", "2308"));
            list.add(Pair.create("其他", "8453"));
            return list;
        }

        @Override
        public boolean hasReader() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getReader() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("少年", "3262"));
            list.add(Pair.create("少女", "3263"));
            list.add(Pair.create("青年", "3264"));
            return list;
        }

        @Override
        public boolean hasProgress() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getProgress() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("连载", "2309"));
            list.add(Pair.create("完结", "2310"));
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


    /**
     * 代表pica的http标准请求头
     */
    public static class PicaHeader {

        /**
         * 客户端某个硬编码的值
         */
        private String secret_key = "~d}$Q7$eIni=V)9\\RK/P.RM4;9[7|@/CA}b~OW!3?EV`:<>M7pddUBL5n|0/*Cn";

        /**
         * 噪声，防重放攻击
         * 测试无效果，随机即可
         */
        private String nonce = UUID.randomUUID().toString().replace("-", "");

        /**
         * 客户端请求头（似乎也是固定？）
         */
        private String api_key = "C69BAF41DA5ABD1FFEDC6D2FEA56B";

        /**
         * 客户端版本号
         */
        private String version = "2.2.1.3.3.4";

        /**
         * 构建的版本？安卓sdk版本？
         */
        private String build_version = "45";
        /**
         * 分流服务器？
         */
        private int channel = 2;
        /**
         * 时间戳
         * 注意，和服务器的时间差要控制在300秒以内
         */
        private long timestamp = System.currentTimeMillis() / 1000;
        /**
         * 登陆后服务器返回的token
         * 大部分请求需要此字段，否则会返回401错误
         */
        private String authorization = null;

        /**
         * 要访问的url
         */
        private String url = null;

        /**
         * 设置请求类型
         * GET或POST
         */
        private Method method = Method.GET;

        /**
         * 提交类型
         * 部分请求需要此字段
         * 例：
         * 登录：application/json; charset=UTF-8 无此字段将会报too many requests
         */
        private String contentType = null;

        /**
         * 用户代理
         */
        private String user_agent = "okhttp/3.8.1";

        private String host = null;
        private String connection = null;

        private String accept_encoding = null;

        public PicaHeader() {

        }

        private static String HMACSHA256(byte[] data, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            return byte2hex(mac.doFinal(data));
        }

        private static String byte2hex(byte[] b) {
            StringBuilder hs = new StringBuilder();
            String stmp;
            for (int n = 0; b != null && n < b.length; n++) {
                stmp = Integer.toHexString(b[n] & 0XFF);
                if (stmp.length() == 1) {
                    hs.append('0');
                }
                hs.append(stmp);
            }
            return hs.toString();
        }

        /**
         * 获取http请求头
         * 注意，调用后请尽快使用，避免请求过期
         *
         * @return
         */
        public Map<String, String> getHttpHeader() {
            Map<String, String> header = new TreeMap<>();
            try {

                if (authorization != null) {
                    header.put("authorization", authorization);
                }

                if (contentType != null) {
                    header.put("Content-Type", contentType);
                }

                header.put("api-key", getApi_key());
                header.put("accept", "application/vnd.picacomic.com.v1+json");
                header.put("app-channel", String.valueOf(getChannel()));
                header.put("time", String.valueOf(getTimestamp()));
                header.put("nonce", getNonce());
                header.put("signature", getSignature());
                header.put("app-version", getVersion());
                header.put("app-uuid", "cb69a7aa-b9a8-3320-8cf1-74347e9ee970");
                header.put("image-quality", "original");
                header.put("app-platform", "android");
                header.put("app-build-version", getBuild_version());
                if (user_agent != null) {
                    header.put("User-Agent", getUser_agent());
                }
                if (host != null) {
                    header.put("Host", getHost());
                }
                if (connection != null) {
                    header.put("Connection", getConnection());
                }
                if (accept_encoding != null) {
                    header.put("Accept-Encoding", accept_encoding);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return header;
        }

        /**
         * 获取签名
         * 通过 URL 、时间戳、噪声、提交方式和APIKEY计算出请求头的签名
         *
         * @return
         * @throws NoSuchAlgorithmException
         * @throws InvalidKeyException
         */
        public String getSignature() throws NoSuchAlgorithmException, InvalidKeyException {
            url = url.replace("https://picaapi.picacomic.com/", "");
            url = url + getTimestamp() + getNonce() + getMethod() + getApi_key();
            url = url.toLowerCase();
            return HMACSHA256(url.getBytes(), getSecret_key().getBytes());
        }

        public String getSecret_key() {
            return secret_key;
        }

        public void setSecret_key(String secret_key) {
            this.secret_key = secret_key;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getApi_key() {
            return api_key;
        }

        public void setApi_key(String api_key) {
            this.api_key = api_key;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getBuild_version() {
            return build_version;
        }

        public void setBuild_version(String build_version) {
            this.build_version = build_version;
        }

        public int getChannel() {
            return channel;
        }

        public void setChannel(int channel) {
            this.channel = channel;
        }

        public long getTimestamp() {
            return timestamp;
        }

        /**
         * 设置请求的时间戳，单位秒
         * 注意：如果时间和服务器相差300秒以上将会报错，请在设置后尽快发起请求！
         *
         * @param timestamp
         */
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getAuthorization() {
            return authorization;
        }

        /**
         * 设置身份验证字段
         * 大部分请求需要带有此字段鉴权
         * 使用登录api获取
         *
         * @param authorization
         */
        public void setAuthorization(String authorization) {
            this.authorization = authorization;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getUser_agent() {
            return user_agent;
        }

        public void setUser_agent(String user_agent) {
            this.user_agent = user_agent;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getConnection() {
            return connection;
        }

        public void setConnection(String connection) {
            this.connection = connection;
        }

        public String getAccept_encoding() {
            return accept_encoding;
        }

        public void setAccept_encoding(String accept_encoding) {
            this.accept_encoding = accept_encoding;
        }

        /**
         * 代表请求的模式
         */
        public enum Method {
            /**
             * GET请求
             */
            GET,
            /**
             * POST请求
             */
            POST
        }
    }

}
