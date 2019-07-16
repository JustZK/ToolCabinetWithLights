package com.zk.cabinet.network;

import android.content.Context;

import com.zk.cabinet.util.SharedPreferencesUtil;

public class NetworkRequest extends VolleyRequest{
    private static volatile NetworkRequest networkRequest = null;

    private String deviceCode;
    public static final String URL_HEAD = "http://";
    public static final String URL_COLON = ":";
    public static final String DEFAULT_URL = "127.0.0.1";
    public static final int DEFAULT_PORT = 7777;
    public String urlLoginByPwd;
    public String urlLoginByCard;
    public String urlCabinetInfo;
    public String urlTool;
    public String urlOftenTool;
    public String urlWaitTool;
    public String urlBorrowTool;
    public String urlReturnTool;
    public static final String LOGIN_BY_PWD = "/api/User/Login";
    public static final String LOGIN_BY_CARD = "/openapi/user/card";
    public static final String CABINET_INFO = "/openapi/base/Cabinet";
    public static final String TOOL = "/openapi/base/tool";
    public static final String OFTEN_TOOL = "/openapi/base/oftentool";
    public static final String WAIT_TOOL = "/openapi/base/waittool";
    public static final String BORROW_TOOL = "/openapi/submit/borrowtool";
    public static final String RETURN_TOOL = "/openapi/submit/returntool";

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
        deviceCode = SharedPreferencesUtil.getInstance().getString(SharedPreferencesUtil.Key.DeviceId, "00000000");
        configModify(url, port);
    }


    public void configModify (String url, int port) {
        urlLoginByPwd = URL_HEAD + url + URL_COLON + port + LOGIN_BY_PWD;
        urlLoginByCard = URL_HEAD + url + URL_COLON + port + LOGIN_BY_CARD;
        urlCabinetInfo = URL_HEAD + url + URL_COLON + port + CABINET_INFO;
        urlTool = URL_HEAD + url + URL_COLON + port + TOOL;
        urlOftenTool = URL_HEAD + url + URL_COLON + port + OFTEN_TOOL;
        urlWaitTool = URL_HEAD + url + URL_COLON + port + WAIT_TOOL;
        urlBorrowTool = URL_HEAD + url + URL_COLON + port + BORROW_TOOL;
        urlReturnTool = URL_HEAD + url + URL_COLON + port + RETURN_TOOL;
    }

//    @Override
//    public void add(JsonObjectRequest jsonObjectRequest){
//        super.add(jsonObjectRequest);
//    }
}
