package com.OnlyX.manager;

import com.OnlyX.component.AppGetter;
import com.OnlyX.model.Task;
import com.OnlyX.model.TaskDao;
import com.OnlyX.model.TaskDao.Properties;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import rx.Observable;

/**
 * Created by Hiroshi on 2016/9/4.
 */
public class TaskManager {

    private static TaskManager mInstance;

    private final TaskDao mTaskDao;

    private TaskManager(AppGetter getter) {
        mTaskDao = getter.getAppInstance().getDaoSession().getTaskDao();
    }

    public static TaskManager getInstance(AppGetter getter) {
        if (mInstance == null) {
            synchronized (TaskManager.class) {
                if (mInstance == null) {
                    mInstance = new TaskManager(getter);
                }
            }
        }
        return mInstance;
    }

    public List<Task> list() {
        return mTaskDao.queryBuilder().list();
    }

    public List<Task> list(long key) {
        return mTaskDao.queryBuilder()
                .where(Properties.Key.eq(key))
                .list();
    }

    public Observable<List<Task>> listInRx(long key) {
        return mTaskDao.queryBuilder()
                .where(Properties.Key.eq(key))
                .rx()
                .list();
    }

    public Observable<List<Task>> listInRx() {
        return mTaskDao.queryBuilder()
                .rx()
                .list();
    }

    public void insert(Task task) {
        long id = mTaskDao.insert(task);
        task.setId(id);
    }

    public void insertInTx(Iterable<Task> entities) {
        mTaskDao.insertInTx(entities);
    }

    public void update(Task task) {
        mTaskDao.update(task);
    }

    public void delete(Task task) {
        mTaskDao.delete(task);
    }

    public void delete(long id) {
        mTaskDao.deleteByKey(id);
    }

    public void deleteByComicId(long id) {
        mTaskDao.queryBuilder()
                .where(Properties.Key.eq(id))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    public void insertIfNotExist(final Iterable<Task> entities) {
        mTaskDao.getSession().runInTx(() -> {
            for (Task task : entities) {
                QueryBuilder<Task> builder = mTaskDao.queryBuilder()
                        .where(Properties.Key.eq(task.getKey()), Properties.Path.eq(task.getPath()));
                if (builder.unique() == null) {
                    mTaskDao.insert(task);
                }
            }
        });
    }

}
