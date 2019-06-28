package com.zk.cabinet.db;

import com.zk.cabinet.bean.Tools;
import com.zk.cabinet.dao.ToolsDao;
import com.zk.cabinet.util.LogUtil;

import java.util.List;

public class ToolsService extends BaseService<Tools, Long> {
    private static volatile ToolsService instance;//单例

    public static ToolsService getInstance() {
        if (instance == null) {
            synchronized (ToolsService.class) {//保证异步处理安全操作
                if (instance == null) {
                    instance = new ToolsService();
                }
            }
        }
        return instance;
    }

    /**
     *
     * @param cellNumber
     * @param toolState
     * @return
     */
    public List<Tools> queryEq (int cellNumber, int toolState) {
        return query(
                ToolsDao.Properties.CellNumber.eq(cellNumber),
                ToolsDao.Properties.ToolState.eq(toolState));
    }

    /**
     * 模糊查询
     * @param str 输入
     * @return 查询结果
     */
    public List<Tools> queryOr (String str) {
        return queryBuilder().where(
                ToolsDao.Properties.ToolName.like("%" + str + "%")).list();
    }

    public List<Tools> queryOr (String userID, int toolState) {
//        return queryBuilder().where(
//                ToolsDao.Properties.ToolState.eq(toolState),
//                ToolsDao.Properties.Borrower.eq(userID)).list();

        return query(
                ToolsDao.Properties.Borrower.eq(userID),
                ToolsDao.Properties.ToolState.eq(toolState));
    }

    public List<Tools> queryOr (String userID, int cellNumber, int toolState) {
//        return queryBuilder().where(
//                ToolsDao.Properties.ToolState.eq(toolState),
//                ToolsDao.Properties.Borrower.eq(userID)).list();

        return query(
                ToolsDao.Properties.Borrower.eq(userID),
                ToolsDao.Properties.CellNumber.eq(cellNumber),
                ToolsDao.Properties.ToolState.eq(toolState));
    }

    /**
     * 查询指定EPC卷宗信息
     *
     * @param epc EPC
     * @return 查询结果
     */
    public Tools queryEq(String epc) {
        List<Tools> list = query(ToolsDao.Properties.Epc.eq(epc));
        Tools tools = null;
        if (list != null && list.size() > 0) {
            tools = list.get(0);
        }
        return tools;
    }

    public void insertOrUpdate(final List<Tools> toolsLists) {
        if (getDao() == null || getDaoSession() == null) {
            LogUtil.getInstance().d(TAG, CHECK_INIT, false);
        } else {
            getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {

                    Tools toolsTemp;
                    for (Tools tools : toolsLists) {
                        try {
                            getDao().insert(tools);
                        } catch (Exception insertException) {
                            insertException.printStackTrace();
                            toolsTemp = queryEq(tools.getEpc());
                            if (toolsTemp != null) {
                                toolsTemp.setToolName(tools.getToolName());
                                toolsTemp.setToolState(tools.getToolState());
                                toolsTemp.setCellNumber(tools.getCellNumber());
                                toolsTemp.setBorrower(tools.getBorrower());
                                try {
                                    update(toolsTemp);
                                } catch (Exception updateException) {
                                    updateException.printStackTrace();
                                }

                            }
                        }
                    }
                }
            });
        }
    }

    public void insertOrUpdate(final Tools tools) {
        if (getDao() == null || getDaoSession() == null) {
            LogUtil.getInstance().d(TAG, CHECK_INIT, false);
        } else {
            getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    try {
                        getDao().insert(tools);
                    } catch (Exception insertException) {
                        insertException.printStackTrace();
                        Tools toolsTemp;
                        toolsTemp = queryEq(tools.getEpc());
                        if (toolsTemp != null) {

                            toolsTemp.setToolName(tools.getToolName());

                            try {
                                update(toolsTemp);
                            } catch (Exception updateException) {
                                updateException.printStackTrace();
                            }

                        }
                    }
                }

            });
        }
    }

}
