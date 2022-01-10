package com.OnlyX.helper;

import android.app.Application;

import com.OnlyX.BuildConfig;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.model.DaoSession;
import com.OnlyX.model.Source;
import com.OnlyX.source.*;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2017/1/18.
 */

public class UpdateHelper {

    // 1.04.08.008
    private static final int VERSION = BuildConfig.VERSION_CODE;

    public static void update(Application app, PreferenceManager manager, final DaoSession session) {
        int version = manager.getInt(PreferenceManager.PREF_APP_VERSION, 0);
        if (version != VERSION) {
            try {
                initSource(app, session);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            manager.putInt(PreferenceManager.PREF_APP_VERSION, VERSION);
        }
    }

    /**
     * 初始化图源
     */
    private static void initSource(Application app, DaoSession session) throws IOException, JSONException {
        List<Source> list = new ArrayList<>();
//        List<Source> list = Luo.getDefaultSource(session.getSourceDao(), app.getFilesDir().toString());
        list.add(IKanman.getDefaultSource());
        list.add(Dmzj.getDefaultSource());
        list.add(JMTT.getDefaultSource());
        list.add(HHAAZZ.getDefaultSource());
        list.add(CCTuku.getDefaultSource());
        list.add(U17.getDefaultSource());
        list.add(DM5.getDefaultSource());
        list.add(Webtoon.getDefaultSource());
        list.add(HHSSEE.getDefaultSource());
        list.add(MH57.getDefaultSource());
        list.add(MH50.getDefaultSource());
        list.add(Dmzjv2.getDefaultSource());
        list.add(MangaNel.getDefaultSource());
        list.add(PuFei.getDefaultSource());
        list.add(Cartoonmad.getDefaultSource());
        list.add(Animx2.getDefaultSource());
        list.add(MH517.getDefaultSource());
        list.add(BaiNian.getDefaultSource());
        list.add(MiGu.getDefaultSource());
        list.add(Tencent.getDefaultSource());
        list.add(BuKa.getDefaultSource());
        list.add(EHentai.getDefaultSource());
        list.add(NetEase.getDefaultSource());
        list.add(Hhxxee.getDefaultSource());
        list.add(ChuiXue.getDefaultSource());
        list.add(BaiNian.getDefaultSource());
        list.add(TuHao.getDefaultSource());
        list.add(MangaBZ.getDefaultSource());
        list.add(ManHuaDB.getDefaultSource());
        list.add(Manhuatai.getDefaultSource());
        list.add(GuFeng.getDefaultSource());
        list.add(CCMH.getDefaultSource());
        list.add(Manhuatai.getDefaultSource());
        list.add(MHLove.getDefaultSource());
        list.add(GuFeng.getDefaultSource());
        list.add(YYLS.getDefaultSource());
        list.add(Pica.getDefaultSource());
        session.getSourceDao().insertOrReplaceInTx(list);
    }
}
