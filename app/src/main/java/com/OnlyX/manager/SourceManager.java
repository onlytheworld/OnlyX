package com.OnlyX.manager;


import android.util.SparseArray;

import com.OnlyX.component.AppGetter;
import com.OnlyX.model.Source;
import com.OnlyX.model.SourceDao;
import com.OnlyX.model.SourceDao.Properties;
import com.OnlyX.parser.Parser;
import com.OnlyX.source.Animx2;
import com.OnlyX.source.BaiNian;
import com.OnlyX.source.BuKa;
import com.OnlyX.source.CCMH;
import com.OnlyX.source.CCTuku;
import com.OnlyX.source.Cartoonmad;
import com.OnlyX.source.ChuiXue;
import com.OnlyX.source.DM5;
import com.OnlyX.source.Dmzj;
import com.OnlyX.source.Dmzjv2;
import com.OnlyX.source.EHentai;
import com.OnlyX.source.GuFeng;
import com.OnlyX.source.HHAAZZ;
import com.OnlyX.source.HHSSEE;
import com.OnlyX.source.Hhxxee;
import com.OnlyX.source.IKanman;
import com.OnlyX.source.JMTT;
import com.OnlyX.source.Locality;
import com.OnlyX.source.Luo;
import com.OnlyX.source.MH50;
import com.OnlyX.source.MH517;
import com.OnlyX.source.MH57;
import com.OnlyX.source.MHLove;
import com.OnlyX.source.ManHuaDB;
import com.OnlyX.source.MangaBZ;
import com.OnlyX.source.MangaNel;
import com.OnlyX.source.Manhuatai;
import com.OnlyX.source.MiGu;
import com.OnlyX.source.NetEase;
import com.OnlyX.source.Pica;
import com.OnlyX.source.PuFei;
import com.OnlyX.source.Tencent;
import com.OnlyX.source.TuHao;
import com.OnlyX.source.U17;
import com.OnlyX.source.Webtoon;
import com.OnlyX.source.YYLS;

import java.util.List;

import okhttp3.Headers;
import rx.Observable;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceManager {

    private static SourceManager mInstance;

    private SourceDao mSourceDao;
    private final SparseArray<Parser> mParserArray = new SparseArray<>();

    private SourceManager(AppGetter getter) {
        mSourceDao = getter.getAppInstance().getDaoSession().getSourceDao();
    }

    public void updateManager(AppGetter getter) {
        mSourceDao = getter.getAppInstance().getDaoSession().getSourceDao();
        mParserArray.clear();
    }

    public static SourceManager getInstance(AppGetter getter) {
        if (mInstance == null) {
            synchronized (SourceManager.class) {
                if (mInstance == null) {
                    mInstance = new SourceManager(getter);
                }
            }
        }
        return mInstance;
    }

    public static SourceManager getInstance() {
        return mInstance;
    }

    public Observable<List<Source>> list() {
        return mSourceDao.queryBuilder()
                .orderAsc(Properties.Type)
                .rx()
                .list();
    }

    public Observable<List<Source>> listEnableInRx() {
        return mSourceDao.queryBuilder()
                .where(Properties.Enable.eq(true))
                .orderAsc(Properties.Type)
                .rx()
                .list();
    }

    public List<Source> listEnable() {
        return mSourceDao.queryBuilder()
                .where(Properties.Enable.eq(true))
                .orderAsc(Properties.Type)
                .list();
    }

    public Source load(int type) {
        return mSourceDao.queryBuilder()
                .where(Properties.Type.eq(type))
                .unique();
    }

    public long insert(Source source) {
        return mSourceDao.insert(source);
    }

    public void update(Source source) {
        mSourceDao.update(source);
    }

    public Parser getParser(int type) {
        Parser parser = mParserArray.get(type);
        if (parser == null) {
            Source source = load(type);
            if (source == null) return null;
//            parser = new Luo(source);
            switch (type) {
                case Dmzjv2.TYPE:
                    parser = new Dmzjv2(source);
                    break;
                case Dmzj.TYPE:
                    parser = new Dmzj(source);
                    break;
                case JMTT.TYPE:
                    parser = new JMTT(source);
                    break;
                // 下面待检查
                case IKanman.TYPE:
                    parser = new IKanman(source);
                    break;
                case HHAAZZ.TYPE:
                    parser = new HHAAZZ(source);
                    break;
                case CCTuku.TYPE:
                    parser = new CCTuku(source);
                    break;
                case U17.TYPE:
                    parser = new U17(source);
                    break;
                case DM5.TYPE:
                    parser = new DM5(source);
                    break;
                case Webtoon.TYPE:
                    parser = new Webtoon(source);
                    break;
                case HHSSEE.TYPE:
                    parser = new HHSSEE(source);
                    break;
                case MH57.TYPE:
                    parser = new MH57(source);
                    break;
                case MH50.TYPE:
                    parser = new MH50(source);
                    break;
                case Locality.TYPE:
                    parser = new Locality();
                    break;
                case MangaNel.TYPE:
                    parser = new MangaNel(source);
                    break;

                case PuFei.TYPE:
                    parser = new PuFei(source);
                    break;
                case Tencent.TYPE:
                    parser = new Tencent(source);
                    break;
                case BuKa.TYPE:
                    parser = new BuKa(source);
                    break;
                case EHentai.TYPE:
                    parser = new EHentai(source);
                    break;
                case NetEase.TYPE:
                    parser = new NetEase(source);
                    break;
                case Hhxxee.TYPE:
                    parser = new Hhxxee(source);
                    break;
                case Cartoonmad.TYPE:
                    parser = new Cartoonmad(source);
                    break;
                case Animx2.TYPE:
                    parser = new Animx2(source);
                    break;
                case MH517.TYPE:
                    parser = new MH517(source);
                    break;
                case MiGu.TYPE:
                    parser = new MiGu(source);
                    break;
                case BaiNian.TYPE:
                    parser = new BaiNian(source);
                    break;
                case ChuiXue.TYPE:
                    parser = new ChuiXue(source);
                    break;
                case TuHao.TYPE:
                    parser = new TuHao(source);
                    break;
                case ManHuaDB.TYPE:
                    parser = new ManHuaDB(source);
                    break;
                case Manhuatai.TYPE:
                    parser = new Manhuatai(source);
                    break;
                case GuFeng.TYPE:
                    parser = new GuFeng(source);
                    break;
                case CCMH.TYPE:
                    parser = new CCMH(source);
                    break;
                case MHLove.TYPE:
                    parser = new MHLove(source);
                    break;
                case YYLS.TYPE:
                    parser = new YYLS(source);
                    break;
                case MangaBZ.TYPE:
                    parser = new MangaBZ(source);
                    break;
                case Pica.TYPE:
                    parser = new Pica(source);
                    break;
//                case -1:
//                    parser = new Luo(source);
//                    break;

                default:
                    parser = new Luo(source);
                    break;
            }
            mParserArray.put(type, parser);
        }
        return parser;
    }

    public class SMGetter {

        public Parser parser(int type) {
            return getParser(type);
        }

        public String getTitle(int type) {
            return getParser(type).getTitle();
        }

        public Headers getHeader(int type) {
            return Headers.of(getParser(type).getHeader());
        }

    }
}
