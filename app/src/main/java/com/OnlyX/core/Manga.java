package com.OnlyX.core;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.OnlyX.App;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.model.Chapter;
import com.OnlyX.model.Comic;
import com.OnlyX.model.ImageUrl;
import com.OnlyX.parser.Parser;
import com.OnlyX.parser.SearchIterator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/8/20.
 */
public class Manga {

    private static boolean indexOfIgnoreCase(String str, String search) {
        return str.toLowerCase().contains(search.toLowerCase());
    }

    public static Observable<Comic> getSearchResult(final Parser parser, final String keyword, final int page, final boolean strictSearch) {
        return Observable.create((Observable.OnSubscribe<Comic>) subscriber -> {
            try {
                Request request = parser.getSearchRequest(keyword, page);
                Random random = new Random();
                String html = getResponseBody(App.getHttpClient(), request);
                SearchIterator iterator = parser.getSearchIterator(html, page);
                if (iterator == null || iterator.empty()) {
                    throw new Exception();
                }
                while (iterator.hasNext()) {
                    Comic comic = iterator.next();
//                        if (comic != null && (comic.getTitle().indexOf(keyword) != -1 || comic.getAuthor().indexOf(keyword) != -1)) {
                    if (comic != null) {
                        if (indexOfIgnoreCase(comic.getTitle(), keyword)
                                || indexOfIgnoreCase(comic.getAuthor(), keyword)
                                || (!strictSearch)) {
                            subscriber.onNext(comic);
                            Thread.sleep(random.nextInt(200));
                        }
                    }
                }
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<Chapter>> getComicInfo(final Parser parser, final Comic comic) {
        return Observable.create((Observable.OnSubscribe<List<Chapter>>) subscriber -> {
            try {
//                    Mongo mongo = new Mongo();
                List<Chapter> list;

//                    list.addAll(mongo.QueryComicBase(comic));
                comic.setUrl(parser.getUrl(comic.getCid()));
                Request request = parser.getInfoRequest(comic.getCid());
                String html = getResponseBody(App.getHttpClient(), request);
                parser.parseInfo(html, comic);
                request = parser.getChapterRequest(html, comic.getCid());
                if (request != null) {
                    html = getResponseBody(App.getHttpClient(), request);
                }
                list = parser.parseChapter(html);
//                        mongo.UpdateComicBase(comic, list);
                if (!list.isEmpty()) {
                    subscriber.onNext(list);
                    subscriber.onCompleted();
                } else {
                    throw new ParseErrorException();
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<Comic>> getCategoryComic(final Parser parser, final String format,
                                                           final int page) {
        return Observable.create((Observable.OnSubscribe<List<Comic>>) subscriber -> {
            try {
                Request request = parser.getCategoryRequest(format, page);
                String html = getResponseBody(App.getHttpClient(), request);
                List<Comic> list = parser.parseCategory(html, page);
                if (!list.isEmpty()) {
                    subscriber.onNext(list);
                    subscriber.onCompleted();
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<ImageUrl>> getChapterImage(final Parser parser,
                                                             final String cid,
                                                             final String path) {
        return Observable.create((Observable.OnSubscribe<List<ImageUrl>>) subscriber -> {
            String html;
//                Mongo mongo = new Mongo();
            List<ImageUrl> list;
            try {
//                    List<ImageUrl> listdoc = new ArrayList<>();
//                    list.addAll(mongo.QueryComicChapter(mComic, path));
                Request request = parser.getImagesRequest(cid, path);
                html = getResponseBody(App.getHttpClient(), request);
                list = parser.parseImages(html);
//                        if (!list.isEmpty()) {
//                            mongo.InsertComicChapter(mComic, path, list);
//                        }

                if (list.isEmpty()) {
                    throw new Exception();
                } else {
                    for (ImageUrl imageUrl : list) {
                        imageUrl.setChapter(path);
                    }
                    subscriber.onNext(list);
                    subscriber.onCompleted();
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    public static void getLogin(final Parser parser, String username, String password, boolean remember) {
        Request request = parser.login(username, password, remember);
        Objects.requireNonNull(App.getHttpClient()).newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                byte[] bodyBytes = new byte[0];
                try {
                    assert response.body() != null;
                    bodyBytes = response.body().bytes();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String body = new String(bodyBytes);
                parser.additionalParser(body);
            }
        });
    }

    public static List<Pair<String, List<Pair<String, String>>>> getCategoryList(final Parser parser) {
        try {
            Request request = parser.getCategory().getCategoryListRequest();
            String html = getResponseBody(App.getHttpClient(), request);
            return parser.getCategory().getCategoryList(html);
        } catch (NetworkErrorException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<ImageUrl> getImageUrls(Parser parser, String cid, String path) throws InterruptedIOException {
        List<ImageUrl> list = new ArrayList<>();
//        Mongo mongo = new Mongo();
        Response response = null;
        try {
//            list.addAll(mongo.QueryComicChapter(source, cid, path));
            Request request = parser.getImagesRequest(cid, path);
            response = Objects.requireNonNull(App.getHttpClient()).newCall(request).execute();
            if (response.isSuccessful()) {
                assert response.body() != null;
                list.addAll(parser.parseImages(response.body().string()));
//                mongo.InsertComicChapter(source, cid, path, list);
            } else {
                throw new NetworkErrorException();
            }
        } catch (InterruptedIOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return list;
    }

    public static String getLazyUrl(Parser parser, String url) throws InterruptedIOException {
        Response response = null;
        try {
            Request request = parser.getLazyRequest(url);
            response = Objects.requireNonNull(App.getHttpClient()).newCall(request).execute();
            if (response.isSuccessful()) {
                assert response.body() != null;
                return parser.parseLazy(response.body().string(), url);
            } else {
                throw new NetworkErrorException();
            }
        } catch (InterruptedIOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    public static Observable<String> loadLazyUrl(final Parser parser, final String url) {
        return Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            Request request = parser.getLazyRequest(url);
            String newUrl = null;
            try {
                newUrl = parser.parseLazy(getResponseBody(App.getHttpClient(), request), url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            subscriber.onNext(newUrl);
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<String>> loadAutoComplete(final String keyword) {
        return Observable.create((Observable.OnSubscribe<List<String>>) subscriber -> {
//                RequestBody body = new FormBody.Builder()
//                        .add("key", keyword)
//                        .add("s", "1")
//                        .build();
//                Request request = new Request.Builder()
//                        .url("http://m.ikanman.com/support/word.ashx")
//                        .post(body)
//                        .build();
            Request request = new Request.Builder()
                    .url("http://m.ac.qq.com/search/smart?word=" + keyword)
                    .build();
            try {
                String jsonString = getResponseBody(App.getHttpClient(), request);
//                    JSONArray array = new JSONArray(jsonString);
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray array = jsonObject.getJSONArray("data");
                List<String> list = new ArrayList<>();
                for (int i = 0; i != array.length(); ++i) {
//                        list.add(array.getJSONObject(i).getString("t"));
                    list.add(array.getJSONObject(i).getString("title"));
                }
                subscriber.onNext(list);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }


    public static Observable<Comic> checkUpdate(
            final SourceManager manager, final List<Comic> list) {
        return Observable.create(new Observable.OnSubscribe<Comic>() {
            @Override
            public void call(Subscriber<? super Comic> subscriber) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(1500, TimeUnit.MILLISECONDS)
                        .readTimeout(1500, TimeUnit.MILLISECONDS)
                        .build();
                class UpdateThread extends Thread {
                    final Comic comic;

                    public UpdateThread(Comic comic) {
                        this.comic = comic;
                    }

                    @Override
                    public void run() {
                        Parser parser = manager.getParser(comic.getSource());
                        Request request = parser.getCheckRequest(comic.getCid());

                        try {
                            String update = parser.parseCheck(getResponseBody(client, request));
                            if (comic.getUpdate() != null && update != null && !comic.getUpdate().equals(update)) {
                                comic.setFavorite(System.currentTimeMillis());
                                comic.setUpdate(update);
                                comic.setHighlight(true);
                                subscriber.onNext(comic);
                            } else {
                                subscriber.onNext(null);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                List<UpdateThread> updateList = new ArrayList<>();
                for (Comic comic : list) {
                    updateList.add(new UpdateThread(comic));
                }
                Random random = new Random();
                for (int i = 0; i < updateList.size(); i++) {
                    try {
                        Thread.sleep(random.nextInt(200));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateList.get(i).start();
                }
                for (int i = 0; i < updateList.size(); i++) {
                    try {
                        updateList.get(i).join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public static String getResponseBody(OkHttpClient client, Request request) throws NetworkErrorException {
        return getResponseBody(client, request, true);
    }

    private static String getResponseBody(OkHttpClient client, Request request, boolean retry) throws NetworkErrorException {
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                byte[] bodyBytes = response.body().bytes();
                String body = new String(bodyBytes);
                Matcher m = Pattern.compile("charset=([\\w\\-]+)").matcher(body);
                if (m.find()) {
                    body = new String(bodyBytes, Objects.requireNonNull(m.group(1)));
                }
                return body;
            } else if (retry)
                return getResponseBody(client, request, false);
        } catch (Exception e) {
            e.printStackTrace();
            if (retry)
                return getResponseBody(client, request, false);
        }
//        throw new NetworkErrorException();
        return null;
    }

    public static class ParseErrorException extends Exception {
    }

    public static class NetworkErrorException extends Exception {
    }

}
