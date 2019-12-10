package com.example.coolweather.gson;

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;//解析aqi
        public String pm25;//解析pm25
    }
}
