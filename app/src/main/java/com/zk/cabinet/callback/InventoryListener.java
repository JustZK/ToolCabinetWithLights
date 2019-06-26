package com.zk.cabinet.callback;

import com.zk.cabinet.bean.InventoryInfo;

import java.util.List;

/**
 * Created by ZK on 2018/1/2.
 */

public interface InventoryListener {

    /**
     * @param inventoryInfoList
     */
    void inventoryList(int result, List<InventoryInfo> inventoryInfoList);

}
