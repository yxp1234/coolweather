package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")//tmp解析
    public String temperature;

    @SerializedName("cond")//解析cond
    public More more;

    public class More{
        @SerializedName("txt")//解析txt
        public String info;
    }
}
