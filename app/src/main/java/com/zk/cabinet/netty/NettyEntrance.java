package com.zk.cabinet.netty;

import com.zk.cabinet.bean.NettySendInfo;
import com.zk.cabinet.callback.InventoryListener;
import com.zk.cabinet.netty.base.NettyParsingLibrary;
import com.zk.cabinet.util.SharedPreferencesUtil;
import com.zk.cabinet.util.SharedPreferencesUtil.Key;

import java.util.HashMap;
import java.util.Map.Entry;

public class NettyEntrance {
    private volatile static NettyEntrance instance;
    private HashMap<String, NettyParsingLibrary> parsingLibraryHashMap;

    private NettyEntrance() {
        parsingLibraryHashMap = new HashMap<>();
    }

    public static NettyEntrance getInstance() {
        if (instance == null) {
            synchronized (NettyEntrance.class) {
                if (instance == null)
                    instance = new NettyEntrance();
            }
        }
        return instance;
    }

    public void init(){
        String[] readerServiceIpPorts = SharedPreferencesUtil.getInstance().getString(Key.ReaderServiceIpPort, "").split(",");
        for (String s : readerServiceIpPorts){
            parsingLibraryHashMap.put(s, new NettyParsingLibrary(
                    s.substring(0, s.indexOf(":")),
                    Integer.parseInt(s.substring(s.indexOf(":") + 1))));
        }
    }

    public void disConnect(){
        if (parsingLibraryHashMap != null){
            for (Entry<String, NettyParsingLibrary> entry : parsingLibraryHashMap.entrySet()) {
                entry.getValue().disConnect();
            }
        }
    }

    public void onInventoryListener(InventoryListener inventoryListener){
        for (Entry<String, NettyParsingLibrary> entry : parsingLibraryHashMap.entrySet()) {
            entry.getValue().onInventoryListener(inventoryListener);
        }
    }

    public void sendData(NettySendInfo nettySendInfo){
        parsingLibraryHashMap.get(nettySendInfo.getReaderIp()).send(nettySendInfo);
    }
}
