package com.zk.cabinet.db;

import com.zk.cabinet.bean.User;
import com.zk.cabinet.dao.UserDao;

import java.util.List;

public class UserService extends BaseService<User, Long> {
    private static volatile UserService instance;//单例

    public static UserService getInstance () {
        if (instance == null) {
            synchronized (UserService.class) {//保证异步处理安全操作
                if (instance == null) {
                    instance = new UserService();
                }
            }
        }
        return instance;
    }

    public User queryByUserID (String userID) {

        if (userID == null) return null;

        List<User> list = query(UserDao.Properties.UserID.eq(userID));
        User user = null;
        if (list != null && list.size() > 0) {
            user = list.get(0);
        }
        return user;
    }
}
