package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
//遍历全国数据
public class AreaFrament extends Fragment {
    private static final String TAG = "ChooseAreaFragment";
    public static final int LEVEL_PROVINCE=0;
    public static final  int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
//   private ProgressBar progressBar;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;//数组适配器
    private List<String> dataList=new ArrayList<>();//容器list
    /*省列表*/
    private List<Province>provinceList;
    /*市列表*/
    private List<City>cityList;
    /*县列表*/
    private List<County>countyList;
    /*选中的省份*/
    private  Province selectedProvince;
    /*选中的城市*/
    private City selectedCity;
    /*当前选中的级别*/
    private  int currentLevel;
//获取控件view和button
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
           View view=inflater.inflate(R.layout.choose_area,container,false);
           titleText=(TextView)view.findViewById(R.id.title_text);
           backButton=(Button)view.findViewById(R.id.back_button);
           listView=(ListView)view.findViewById(R.id.list_view);
           //数组适配器
           adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
           listView.setAdapter(adapter);
           return view;
    }
    //初始化view和button
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position); /*选中的省份*/
                    queryCities();//解析市
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position); /*选中的市*/
                    queryCounties();//解析县

                }else if (currentLevel==LEVEL_COUNTY){
                    //WeatherActivity启动
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity){
                        //Log.i("ccc","weatherId"+weatherId);
                        Intent intent=new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();

                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity=(WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);

                    }

                }

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();//开始加载市数据
                }else  if(currentLevel==LEVEL_CITY){
                    queryProvinces();//开始加载省数据
                }
            }
        });
        queryProvinces();//加载省的数据
    }
    /*查询所有的省，优先数据库查询，若没有就去服务器查询*/
   private  void queryProvinces(){
       titleText.setText("中国");
       backButton.setVisibility(View.GONE);//动画过度按钮隐藏
       provinceList=DataSupport.findAll(Province.class);//先从数据库Province中查找数据
       if (provinceList.size()>0){
           dataList.clear();
           for (Province province : provinceList){//在Utility里面的省对象拿到并且放入省列表
               dataList.add(province.getProvinceName());
           }
           adapter.notifyDataSetChanged();//数组适配器
           listView.setSelection(0);
           currentLevel=LEVEL_PROVINCE;
       }else {
           String address="http://guolin.tech/api/china";
           queryFromServer(address,"province");//服务器查询
       }
   }
    /*查询所有的市，优先数据库查询，若没有就去服务器查询*/
    private  void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);//省份下的市在数据库City里面找
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){//在Utility里面的市对象拿到并且放入省列表
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();//数组适配器
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();//省份下的市在服务器里面找
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    /*查询所有的县，优先数据库查询，若没有就去服务器查询*/
    private  void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);//城市下的县在数据库County里面找

        Log.i("y", "countyList==null"+(countyList==null?"1111":"22222"));

        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList){//在Utility里面的县对象拿到并且放入省列表
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//列表第0项
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();//省份下的市在服务器里面找
            int cityCode=selectedCity.getCityCode();//城市下的县在服务器里面找
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }
    /*根据传入的数据的地址和类型服务器上查询省市数据*/
    private  void queryFromServer(String address,final String type){
        showProgressDialog();//显示对话框
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("bbb","onResponse"+response);
              String responseText=response.body().string();//服务器返回的字符串
              boolean result=false;
              if ("province".equals(type)){
                  result=Utility.handleProvinceResponse(responseText);//处理解析服务器返回来的数据，并存储到数据库
              }else if ("city".equals(type)){
                  result=Utility.handleCityResponse(responseText,selectedProvince.getId());
              }else  if ("county".equals(type)){
                  result=Utility.handleCountyResponse(responseText,selectedCity.getId());
              }
              if (result){
                  getActivity().runOnUiThread(new Runnable() {//子线程切换主线程
                      @Override
                      public void run() {
                          closeProgressDialog();
                          if ("province".equals(type)){
                           queryProvinces();
                          }else if ("city".equals(type)){
                              queryCities();
                          }else if ("county".equals(type)){
                              queryCounties();

                          }
                  }
                  });
              }
            }
            @Override
            public void onFailure(Call call,IOException e){
                Log.i("aaa","onFailure"+e.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    /*显示对话框*/
    private  void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCanceledOnTouchOutside(false);
//            progressBar.setVisibility(View.VISIBLE);
        }
        progressDialog.show();
    }
    /*关闭对话框*/
    private void closeProgressDialog(){
        if (progressDialog !=null){
            progressDialog.dismiss();
//            progressBar.setVisibility(View.GONE);
        }
    }
}
