package com.zk.cabinet.db;

import android.content.Context;

import com.zk.cabinet.dao.DaoMaster;
import com.zk.cabinet.dao.DaoSession;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;
import org.greenrobot.greendao.identityscope.IdentityScopeType;

/**
 * Created by ZK on 2018/1/11.
 */

public class DBHelper {

    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private static volatile DBHelper instance;//单例

    private DBHelper() {
    }

    public static DBHelper getInstance() {
        if (instance == null) {
            synchronized (DBHelper.class) {//保证异步处理安全操作
                if (instance == null) {
                    instance = new DBHelper();
                }
            }
        }
        return instance;
    }

    public void init (Context context) {
        if (instance != null) {
            //此处devOpenHelper为自动生成开发所使用，发布版本需自定义
//            DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context.getApplicationContext(), "FileCabinet", null);

            DatabaseOpenHelper databaseOpenHelper = new DaoMaster.OpenHelper(context.getApplicationContext(), "FileCabinet") {
                @Override
                public void onCreate(Database db) {
                    super.onCreate(db);
                }

                @Override
                public void onUpgrade(Database db, int oldVersion, int newVersion) {

                }
            };

            mDaoMaster = new DaoMaster(databaseOpenHelper.getWritableDatabase());
            mDaoSession = mDaoMaster.newSession(IdentityScopeType.None);

            CabinetService.getInstance().init(mDaoSession, mDaoSession.getCabinetDao());
            ToolsService.getInstance().init(mDaoSession,mDaoSession.getToolsDao());
            UserService.getInstance().init(mDaoSession,mDaoSession.getUserDao());
        }
    }

    public DaoMaster getMaster() {
        return mDaoMaster;
    }

    public DaoSession getSession() {
        return mDaoSession;
    }


}
