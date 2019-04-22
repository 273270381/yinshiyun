package com.myezopen.myezopen;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.videogo.constant.Constant;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZAccessToken;
import com.videogo.util.LogUtil;

public class MyApplication extends Application {
    public static String AppKey = "bec27f333fd04a95a352bec49d466754";
    public static EZOpenSDK getOpenSDK(){
     return EZOpenSDK.getInstance();
    }
    private IntentFilter intentFilter;
    private MyBroadCastReciver broadCastReciver;
    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        initSDK();
    }

    private void initData() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.videogo.action.ADD_DEVICE_SUCCESS_ACTION");
        intentFilter.addAction("com.videogo.action.OAUTH_SUCCESS_ACTION");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        broadCastReciver = new MyBroadCastReciver();
        registerReceiver(broadCastReciver,intentFilter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(broadCastReciver);
    }

    private void initSDK(){
        /**
         * sdk日志开关，正式发布需要去掉
         */
        EZOpenSDK.showSDKLog(true);
        /**
         * 设置是否支持P2P取流,详见api
         */
        EZOpenSDK.enableP2P(true);
        /**
         * APP_KEY请替换成自己申请的
         */
        EZOpenSDK.initLib(this, AppKey);
    }
    private class MyBroadCastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.i("TAG","action = "+action);
            if (action.equals(Constant.OAUTH_SUCCESS_ACTION)){
                Log.i("TAG", "onReceive: OAUTH_SUCCESS_ACTION");
                Intent i = new Intent(context,CameraListActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                /*******   获取登录成功之后的EZAccessToken对象   *****/
                EZAccessToken token =MyApplication.getOpenSDK().getEZAccessToken();
                context.startActivity(i);
            }
        }
    }
}
