package com.zk.cabinet.db;

import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.dao.CabinetDao;
import com.zk.cabinet.util.LogUtil;

import java.util.LinkedHashMap;
import java.util.List;

public class CabinetService extends BaseService<Cabinet, Long> {

    private static volatile CabinetService instance;//单例

    public static CabinetService getInstance () {
        if (instance == null) {
            synchronized (CabinetService.class) {//保证异步处理安全操作
                if (instance == null) {
                    instance = new CabinetService();
                }
            }
        }
        return instance;
    }

    public LinkedHashMap<Integer, Cabinet> loadAllWithMap() {
        LinkedHashMap<Integer, Cabinet> map = new LinkedHashMap<>();
        if (getDao() == null) {
            LogUtil.getInstance().d(TAG, CHECK_INIT, false);
            return null;
        }
        List<Cabinet> cabinetList = CabinetService.getInstance().loadAll();
        for (int i = 0; i < cabinetList.size(); i++) {
            map.put(cabinetList.get(i).getCellNumber(), cabinetList.get(i));
        }
        return map;
    }

    public Cabinet queryEq (int cellNumber) {
        List<Cabinet> list = query(CabinetDao.Properties.CellNumber.eq(cellNumber));
        Cabinet cabinet = null;
        if (list != null && list.size() > 0) {
            cabinet = list.get(0);
        }
        return cabinet;
    }

    /**
     * 生成A柜数据
     */
    public void buildMain () {
        Cabinet[] cabinets = new Cabinet[3];
        cabinets[0] = new Cabinet();
        cabinets[0].setProportion(2);
        cabinets[0].setBoxName("钥匙格");
        cabinets[0].setCellNumber(-1);

        cabinets[1] = new Cabinet();
        cabinets[1].setProportion(4);
        cabinets[1].setBoxName("主屏幕");
        cabinets[1].setCellNumber(-2);

        cabinets[2] = new Cabinet();
        cabinets[2].setProportion(3);
        cabinets[2].setBoxName("钥匙格");
        cabinets[2].setCellNumber(-3);

        insert(cabinets);
    }

    /**
     * 生成副柜数据
     */
    public void buildDeputy (int deputyCabinet) {
        Cabinet[] cabinets = new Cabinet[10];

        for (int i = 0; i< cabinets.length; i++){
            cabinets[i] = new Cabinet();
            cabinets[i].setCellNumber(deputyCabinet * 10 + i + 1);
            cabinets[i].setBoxName(String.valueOf((char) (deputyCabinet + 65)) + (i + 1));
            if (i < 6 || i == 7 || i == 8){
                cabinets[i].setProportion(1);
            } else if (i == 6) {
                cabinets[i].setProportion(3);
            } else {
                cabinets[i].setProportion(7);
            }
            cabinets[i].setSourceAddress(0xff);
            cabinets[i].setSignBroken(0);
        }
        insert(cabinets);
    }

}
