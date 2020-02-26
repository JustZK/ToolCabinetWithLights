package com.zk.cabinet.network;

import android.content.Context;

import com.zk.cabinet.util.SharedPreferencesUtil;

public class NetworkRequest extends VolleyRequest{
    private static volatile NetworkRequest networkRequest = null;

    public static final String URL_HEAD = "http://";
    public static final String URL_COLON = ":";
    public static final String DEFAULT_URL = "127.0.0.1";
    public static final int DEFAULT_PORT = 7777;
    public String urlLoginByPwd;
    public String urlLoginByCard;
    public String urlOutBoundList;
    public String urlInBoundList;
    public String urlToolsInBoxList;
    public String urlUpOutBoundList;
    public String urlUpInBoundList;
    public String urlUserList;
    public String urlUserFpiAdd;
    public String urlToolsInBox;
    public String urlHeartbeat;
    public String urlInventory;
    private static final String LOGIN_BY_PWD = "/Api/User/Login";
    private static final String LOGIN_BY_CARD = "/Api/user/card";
    private static final String OUT_BOUND_LIST = "/Api/Traffic/TrafficGdnQuery";
    private static final String IN_BOUND_LIST = "/Api/Traffic/TrafficGrnQuery";
    private static final String TOOLS_IN_BOX_LIST = "/Api/Traffic/TrafficCabinet";
    private static final String UP_OUT_BOUND_LIST = "/Api/Traffic/TrafficGrnAdd";
    private static final String UP_IN_BOUND_LIST = "/Api/Traffic/TrafficGrnAdd";
    private static final String USER_LIST = "/Api/Traffic/UserList";
    private static final String USER_FPI_ADD_LIST = "/Api/Traffic/UserFPIAdd";
    private static final String TOOLS_IN_BOX = "/Api/Traffic/TrafficAllCabinet";
    private static final String HEARTBEAT = "/Api/Traffic/TrafficHeartbeat";
    private static final String INVENTORY = "/Api/Traffic/TrafficInventory";


    public static NetworkRequest getInstance(){
        if (networkRequest == null) {
            synchronized(NetworkRequest.class){
                if (networkRequest == null) {
                    networkRequest = new NetworkRequest();
                }
            }
        }
        return networkRequest;
    }

    @Override
    public void init(Context context) {
        super.init(context);

        String url = SharedPreferencesUtil.getInstance().getString(SharedPreferencesUtil.Key.PlatformServiceIp, DEFAULT_URL);
        int port = SharedPreferencesUtil.getInstance().getInt(SharedPreferencesUtil.Key.PlatformServicePort, DEFAULT_PORT);
        configModify(url, port);
    }

    public void configModify (String url, int port) {
        urlLoginByPwd = URL_HEAD + url + URL_COLON + port + LOGIN_BY_PWD;
        urlLoginByCard = URL_HEAD + url + URL_COLON + port + LOGIN_BY_CARD;
        urlOutBoundList = URL_HEAD + url + URL_COLON + port + OUT_BOUND_LIST;
        urlInBoundList = URL_HEAD + url + URL_COLON + port + IN_BOUND_LIST;
        urlToolsInBoxList = URL_HEAD + url + URL_COLON + port + TOOLS_IN_BOX_LIST;
        urlUpOutBoundList = URL_HEAD + url + URL_COLON + port + UP_OUT_BOUND_LIST;
        urlUpInBoundList = URL_HEAD + url + URL_COLON + port + UP_IN_BOUND_LIST;
        urlUserList = URL_HEAD + url + URL_COLON + port + USER_LIST;
        urlUserFpiAdd = URL_HEAD + url + URL_COLON + port + USER_FPI_ADD_LIST;
        urlToolsInBox = URL_HEAD + url + URL_COLON + port + TOOLS_IN_BOX;
        urlHeartbeat = URL_HEAD + url + URL_COLON + port + HEARTBEAT;
        urlInventory = URL_HEAD + url + URL_COLON + port + INVENTORY;
    }
}
