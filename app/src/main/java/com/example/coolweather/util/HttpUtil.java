package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
//请求服务器
//遍历全国数据
public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request =new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
