package com.zk.cabinet.db;

import com.zk.cabinet.bean.Tools;
import com.zk.cabinet.dao.ToolsDao;

import java.util.List;

public class ToolsService extends BaseService<Tools, Long> {
    private static volatile ToolsService instance;//单例

    public static ToolsService getInstance () {
        if (instance == null) {
            synchronized (ToolsService.class) {//保证异步处理安全操作
                if (instance == null) {
                    instance = new ToolsService();
                }
            }
        }
        return instance;
    }

    public void deleteByState(int state){
        delete(query(ToolsDao.Properties.State.eq(state)));
    }

    public List<Tools> getOutTools(int cellNumber){
        return query(ToolsDao.Properties.CellNumber.eq(cellNumber),
                ToolsDao.Properties.State.eq(0),
                ToolsDao.Properties.Operating.eq(1));
    }

    public List<Tools> getDepositTools(int cellNumber){
        return query(ToolsDao.Properties.CellNumber.eq(cellNumber),
                ToolsDao.Properties.State.eq(1),
                ToolsDao.Properties.Operating.eq(2));
    }
}
