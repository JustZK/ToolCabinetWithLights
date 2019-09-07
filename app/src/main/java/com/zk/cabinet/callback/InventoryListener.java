package com.zk.cabinet.callback;

import com.zk.cabinet.bean.InventoryInfo;

import java.util.List;

/**
 * Created by ZK on 2018/1/2.
 */

public interface InventoryListener {

    /**
     *
     * @param readerID 读写器ID
     * @param result 0：正常 1：读写器离线
     * @param inventoryInfoList epcList
     */
    void inventoryList(int readerID, int result, List<InventoryInfo> inventoryInfoList);

}
