package com.zk.cabinet.db;

import com.zk.cabinet.bean.InventoryAll;

public class InventoryAllService extends BaseService<InventoryAll, Long> {
    private static volatile InventoryAllService instance;//单例

    public static InventoryAllService getInstance () {
        if (instance == null) {
            synchronized (InventoryAllService.class) {//保证异步处理安全操作
                if (instance == null) {
                    instance = new InventoryAllService();
                }
            }
        }
        return instance;
    }
}
