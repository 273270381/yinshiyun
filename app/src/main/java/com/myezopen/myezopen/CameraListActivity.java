package com.myezopen.myezopen;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.myezopen.myezopen.Utils.ActivityUtils;
import com.myezopen.myezopen.Utils.EZUtils;
import com.myezopen.myezopen.Utils.RemoteListContant;
import com.myezopen.myezopen.Utils.WaitDialog;
import com.myezopen.myezopen.datamanager.RecycleViewActivity;
import com.myezopen.myezopen.devicemgt.EZDeviceSettingActivity;
import com.myezopen.myezopen.message.EZMessageActivity2;
import com.myezopen.myezopen.realplay.RealPlayActivity;
import com.myezopen.myezopen.remoteplayback.list.PlayBackListActivity;
import com.myezopen.myezopen.widget.PullToRefreshFooter;
import com.myezopen.myezopen.widget.PullToRefreshHeader;
import com.myezopen.myezopen.widget.SelectCameraDialog;
import com.myezopen.myezopen.widget.pulltorefreshview.IPullToRefresh;
import com.myezopen.myezopen.widget.pulltorefreshview.LoadingLayout;
import com.myezopen.myezopen.widget.pulltorefreshview.PullToRefreshBase;
import com.myezopen.myezopen.widget.pulltorefreshview.PullToRefreshListView;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZLeaveMessage;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.DateTimeUtil;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.myezopen.myezopen.MyApplication.getOpenSDK;

public class CameraListActivity extends Activity implements View.OnClickListener,SelectCameraDialog.CameraItemClick{
    protected static final String TAG = "CameraListActivity";
    public final static int REQUEST_CODE = 100;
    public final static int RESULT_CODE = 101;
    /**
     * 删除设备
     */
    private final static int SHOW_DIALOG_DEL_DEVICE = 1;
    private BroadcastReceiver mReceiver = null;
    private PullToRefreshListView mListView = null;
    private View mNoMoreView;
    private CameraListAdapter mAdapter = null;
    private LinearLayout mGetCameraFailTipLy = null;
    private TextView mCameraFailTipTv = null;
    private Button mAddBtn;
    private Button mUserBtn;
    private AMap aMap;
    private UiSettings mUiSettings;
    private TextView mMyDevice;
    private MapView mapView;
    private boolean bIsFromSetting = false;
    private boolean Flage = true;
    public final static int TAG_CLICK_PLAY = 1;
    public final static int TAG_CLICK_REMOTE_PLAY_BACK = 2;
    public final static int TAG_CLICK_SET_DEVICE = 3;
    public final static int TAG_CLICK_ALARM_LIST = 4;
    public final static int TAG_CLICK_PROJECT_MANAGER = 5;
    private int mClickType;
    private final static int LOAD_MY_DEVICE = 0;
    private final static int LOAD_SHARE_DEVICE = 1;
    private int mLoadType = LOAD_MY_DEVICE;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtil.d(TAG, "handleMessage " + msg);
            /***获取下载的留言ID***/
            String mId;
            switch (msg.what) {
                case 301:
                    // TODO: 2016/10/13   下载留言失败
                    mId = (String) msg.obj;
                    break;
                case 302:
                    // TODO: 2016/10/13  下载留言成功
                    /*** 获取下载成功的留言ID ***/
                    mId = (String) msg.obj;
                    break;
                default:
                    break;
            }
        }
    };
    private static String[] allpermissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private boolean isNeedCheck = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        Utils.clearAllNotification(this);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
    }
    private void setMap() {
        aMap = mapView.getMap();
        mUiSettings = aMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setLogoBottomMargin(-50);
        mUiSettings.setCompassEnabled(false);
        LogUtil.i("TAG","listdevice2="+mAdapter.getDeviceInfoList().size());
        LatLng latLng1 = new LatLng(33.96793,118.23966);
        LatLng latLng2 = new LatLng(33.95140,118.29297);
        LatLng latLng3 = new LatLng(33.94286,118.30834);
        if (mAdapter.getDeviceInfoList().size()!=0){
            final Marker marker = aMap.addMarker(new MarkerOptions().position(latLng1).title(mAdapter.getDeviceInfoList().get(0).getDeviceName()).snippet("DefaultMarker"));
            final Marker marker2 = aMap.addMarker(new MarkerOptions().position(latLng2).title(mAdapter.getDeviceInfoList().get(1).getDeviceName()).snippet("DefaultMarker"));
            final Marker marker3 = aMap.addMarker(new MarkerOptions().position(latLng3).title(mAdapter.getDeviceInfoList().get(2).getDeviceName()).snippet("DefaultMarker"));
            aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
            aMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        }
        aMap.addCircle(new CircleOptions().center(latLng1).radius(3000).fillColor(Color.argb(50,155,155,230)).strokeColor(Color.argb(50,155,155,230)).strokeWidth(1));
        aMap.addCircle(new CircleOptions().center(latLng2).radius(3000).fillColor(Color.argb(50,155,155,230)).strokeColor(Color.argb(50,155,155,230)).strokeWidth(1));
        aMap.addCircle(new CircleOptions().center(latLng3).radius(3000).fillColor(Color.argb(50,155,155,230)).strokeColor(Color.argb(50,155,155,230)).strokeWidth(1));
    }

    private void initData() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                LogUtil.debugLog(TAG, "onReceive:" + action);
                if (action.equals(Constant.ADD_DEVICE_SUCCESS_ACTION)) {
                    refreshButtonClicked();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ADD_DEVICE_SUCCESS_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void initView() {
        mMyDevice = (TextView) findViewById(R.id.text_my);
        mAddBtn = (Button) findViewById(R.id.btn_add);
        mUserBtn = (Button) findViewById(R.id.btn_user);
        mUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popLogoutDialog();
                Log.i("TAG","Logout");
            }
        });
//        mAddBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CameraListActivity.this, CaptureActivity.class);
//            }
//        });
        mMyDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.clearAll();
                mLoadType = LOAD_MY_DEVICE;
                getCameraInfoList(true);
            }
        });
        mNoMoreView = getLayoutInflater().inflate(R.layout.no_device_more_footer,null);
        mAdapter = new CameraListAdapter(this);

        LogUtil.i("TAG","list = "+mAdapter.getDeviceInfoList().toString());
        mAdapter.setOnClickListener(new CameraListAdapter.OnClickListener() {
            @Override
            public void onPlayClick(BaseAdapter adapter, View view, int position) {
                mClickType = TAG_CLICK_PLAY;
                final EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                if(deviceInfo.getCameraNum() <=0 || deviceInfo.getCameraInfoList() == null || deviceInfo.getCameraInfoList().size() <= 0){
                    LogUtil.d(TAG, "cameralist is null or cameralist size is 0");
                    return;
                }
                if(deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1){
                    LogUtil.d(TAG, "cameralist have one camera");
                    final EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
                    if (cameraInfo == null) {
                        return;
                    }
                    Intent intent = new Intent(CameraListActivity.this, RealPlayActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                    intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                    startActivityForResult(intent, REQUEST_CODE);
                    return;
                }
                SelectCameraDialog selectCameraDialog = new SelectCameraDialog();
                selectCameraDialog.setEZDeviceInfo(deviceInfo);
                selectCameraDialog.setCameraItemClick(CameraListActivity.this);
                selectCameraDialog.show(getFragmentManager(), "onPlayClick");
            }

            @Override
            public void onDeleteClick(BaseAdapter adapter, View view, int position) {
                showDialog(SHOW_DIALOG_DEL_DEVICE);
            }

            @Override
            public void onAlarmListClick(BaseAdapter adapter, View view, int position) {
                mClickType = TAG_CLICK_ALARM_LIST;
                final EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                LogUtil.d(TAG, "cameralist is null or cameralist size is 0");
                Intent intent = new Intent(CameraListActivity.this, EZMessageActivity2.class);
                intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, deviceInfo.getDeviceSerial());
                startActivity(intent);
            }

            @Override
            public void onRemotePlayBackClick(BaseAdapter adapter, View view, int position) {
                mClickType = TAG_CLICK_REMOTE_PLAY_BACK;
                EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                if (deviceInfo.getCameraNum() <= 0 || deviceInfo.getCameraInfoList() == null || deviceInfo.getCameraInfoList().size() <= 0) {
                    LogUtil.d(TAG, "cameralist is null or cameralist size is 0");
                    return;
                }
                if (deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1) {
                    LogUtil.d(TAG, "cameralist have one camera");
                    EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
                    if (cameraInfo == null) {
                        return;
                    }
                    Intent intent = new Intent(CameraListActivity.this, PlayBackListActivity.class);
                    intent.putExtra(RemoteListContant.QUERY_DATE_INTENT_KEY, DateTimeUtil.getNow());
                    intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                    startActivity(intent);
                    return;
                }
                SelectCameraDialog selectCameraDialog = new SelectCameraDialog();
                selectCameraDialog.setEZDeviceInfo(deviceInfo);
                selectCameraDialog.setCameraItemClick(CameraListActivity.this);
                selectCameraDialog.show(getFragmentManager(), "RemotePlayBackClick");

            }

            @Override
            public void onSetDeviceClick(BaseAdapter adapter, View view, int position) {
                mClickType = TAG_CLICK_SET_DEVICE;
                EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                Intent intent = new Intent(CameraListActivity.this, EZDeviceSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(IntentConsts.EXTRA_DEVICE_INFO,deviceInfo);
                intent.putExtra("Bundle",bundle);
                startActivity(intent);
                bIsFromSetting = true;
            }

            @Override
            public void onDevicePictureClick(BaseAdapter adapter, View view, int position) {

            }

            @Override
            public void onDeviceVideoClick(BaseAdapter adapter, View view, int position) {

            }

            @Override
            public void onDeviceDefenceClick(BaseAdapter adapter, View view, int position) {

            }

            @Override
            public void onProjectClick(BaseAdapter adapter, View view, int position) {
                    mClickType = TAG_CLICK_PROJECT_MANAGER;
                    EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                    Intent intent = new Intent(CameraListActivity.this,RecycleViewActivity.class);
                    intent.putExtra("projectmanager", deviceInfo);
                    startActivity(intent);
            }
        });
        mListView = (PullToRefreshListView) findViewById(R.id.camera_listview);
        mListView.setLoadingLayoutCreator(new PullToRefreshBase.LoadingLayoutCreator() {
            @Override
            public LoadingLayout create(Context context, boolean headerOrFooter, PullToRefreshBase.Orientation orientation) {
                if (headerOrFooter)
                    return new PullToRefreshHeader(context);
                else
                    return new PullToRefreshFooter(context, PullToRefreshFooter.Style.EMPTY_NO_MORE);
            }
        });
        mListView.setMode(IPullToRefresh.Mode.BOTH);
        mListView.setOnRefreshListener(new IPullToRefresh.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView, boolean headerOrFooter) {
                getCameraInfoList(headerOrFooter);
            }
        });
        mListView.getRefreshableView().addFooterView(mNoMoreView);
        mListView.setAdapter(mAdapter);
        mListView.getRefreshableView().removeFooterView(mNoMoreView);
        mGetCameraFailTipLy = (LinearLayout) findViewById(R.id.get_camera_fail_tip_ly);
        mCameraFailTipTv = (TextView) findViewById(R.id.get_camera_list_fail_tv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (bIsFromSetting || (mAdapter != null && mAdapter.getCount() == 0)) {
            refreshButtonClicked();
            bIsFromSetting = false;
        }
        if (isNeedCheck){
            checkpermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (mAdapter != null) {
            mAdapter.shutDownExecutorService();
        }
    }
    /**
     * 从服务器获取最新事件消息
     */
    private void getCameraInfoList(boolean headerOrFooter) {
        if (this.isFinishing()) {
            return;
        }
        new GetCamersInfoListTask(headerOrFooter).execute();
    }

    @Override
    public void onCameraItemClick(EZDeviceInfo deviceInfo, int camera_index) {
        EZCameraInfo cameraInfo = null;
        Intent intent = null;
        switch (mClickType) {
            case TAG_CLICK_PLAY:
//              String pwd = DESHelper.decryptWithBase64("EyCs73n8m5s=", Utils.getAndroidID(this));
                cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, camera_index);
                if (cameraInfo == null) {
                    return;
                }
                intent = new Intent(CameraListActivity.this, RealPlayActivity.class);
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case TAG_CLICK_REMOTE_PLAY_BACK:
                cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, camera_index);
                if (cameraInfo == null) {
                    return;
                }
                intent = new Intent(CameraListActivity.this, PlayBackListActivity.class);
                intent.putExtra(RemoteListContant.QUERY_DATE_INTENT_KEY, DateTimeUtil.getNow());
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 获取事件消息任务
     */
    private class GetCamersInfoListTask extends AsyncTask<Void, Void, List<EZDeviceInfo>> {
        private boolean mHeaderOrFooter;
        private int mErrorCode = 0;
        public GetCamersInfoListTask(boolean headerOrFooter) {
            mHeaderOrFooter = headerOrFooter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mListView.setFooterRefreshEnabled(true);
            if(mHeaderOrFooter){
                mListView.setVisibility(View.VISIBLE);
                mGetCameraFailTipLy.setVisibility(View.GONE);
            }
            mListView.getRefreshableView().removeFooterView(mNoMoreView);
        }

        @Override
        protected List<EZDeviceInfo> doInBackground(Void... voids) {
            if(CameraListActivity.this.isFinishing()){
                return null;
            }
            if(!ConnectionDetector.isNetworkAvailable(CameraListActivity.this)){
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }
            try {
                List<EZDeviceInfo> listDevice = null;
                if (mLoadType == LOAD_MY_DEVICE){
                    if(mHeaderOrFooter){
                        listDevice = getOpenSDK().getDeviceList(0, 20);
                    }else{
                        listDevice = getOpenSDK().getDeviceList((mAdapter.getCount() / 20)+(mAdapter.getCount() % 20>0?1:0), 20);
                    }
                }
                return listDevice;
            }catch(BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.debugLog(TAG, errorInfo.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<EZDeviceInfo> result) {
            super.onPostExecute(result);
            mListView.onRefreshComplete();
            if (CameraListActivity.this.isFinishing()) {
                return;
            }
            if(result!=null){
                if(mHeaderOrFooter){
                    CharSequence dateText = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date());
                    for (LoadingLayout layout : mListView.getLoadingLayoutProxy(true, false).getLayouts()) {
                        ((PullToRefreshHeader) layout).setLastRefreshTime(":" + dateText);
                    }
                    mAdapter.clearItem();
                }
                if(mAdapter.getCount() == 0 && result.size() == 0){
                    mListView.setVisibility(View.GONE);
                    mGetCameraFailTipLy.setVisibility(View.GONE);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                }else if(result.size() < 10){
                    mListView.setFooterRefreshEnabled(false);
                    mListView.getRefreshableView().addFooterView(mNoMoreView);
                }else if (mHeaderOrFooter) {
                    mListView.setFooterRefreshEnabled(true);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                }
                addCameraList(result);
                mAdapter.notifyDataSetChanged();
                if (Flage){
                    setMap();
                }
                Flage = false;
            }
            if (mErrorCode != 0) {
                onError(mErrorCode);
            }
        }
        protected void onError(int errorCode) {
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
                    ActivityUtils.handleSessionException(CameraListActivity.this);
                    break;
                default:
                    if (mAdapter.getCount() == 0) {
                        //mListView.setVisibility(View.GONE);
                        //mCameraFailTipTv.setText(Utils.getErrorTip(CameraListActivity.this, R.string.get_camera_list_fail, errorCode));
                        //mGetCameraFailTipLy.setVisibility(View.VISIBLE);
                        new LogoutTask().execute();
                        Flage = true;
                    } else {
                        Utils.showToast(CameraListActivity.this, R.string.get_camera_list_fail, errorCode);
                    }
                    break;
            }
        }
    }
    private void addCameraList(List<EZDeviceInfo> result) {
        int count = result.size();
        EZDeviceInfo item = null;
        for (int i = 0; i < count; i++) {
            item = result.get(i);
            mAdapter.addItem(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    /**
     * 权限管理
     */
    private void checkpermission() {
        if (Build.VERSION.SDK_INT>=23){
            boolean needapply = false;
            for(int i = 0;i <allpermissions.length;i++ ){
                int checkpermission = ContextCompat.checkSelfPermission(getApplicationContext(),allpermissions[i]);
                if (checkpermission!= PackageManager.PERMISSION_GRANTED){
                    needapply = true;
                }
            }
            if(needapply){
                ActivityCompat.requestPermissions(CameraListActivity.this,allpermissions,1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int  i = 0 ;i<grantResults.length;i++){
            if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(CameraListActivity.this, permissions[i]+"已授权",Toast.LENGTH_SHORT).show();
                isNeedCheck = false;
            }else{
                Toast.makeText(CameraListActivity.this,permissions[i]+"拒绝授权",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {

    }
    /**
     * 刷新点击
     */
    private void refreshButtonClicked() {
        mListView.setVisibility(View.VISIBLE);
        mGetCameraFailTipLy.setVisibility(View.GONE);
        mListView.setMode(IPullToRefresh.Mode.BOTH);
        mListView.setRefreshing();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case SHOW_DIALOG_DEL_DEVICE:
                break;
        }
        return dialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.update_exit).setIcon(R.drawable.exit_selector);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (dialog != null) {
            removeDialog(id);
            TextView tv = (TextView) dialog.findViewById(android.R.id.message);
            tv.setGravity(Gravity.CENTER);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {// 得到被点击的item的itemId
            case 1:// 对应的ID就是在add方法中所设定的Id
                popLogoutDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * 弹出登出对话框
     *
     * @see
     * @since V1.0
     */
    private void popLogoutDialog() {
        AlertDialog.Builder exitDialog = new AlertDialog.Builder(CameraListActivity.this);
        exitDialog.setTitle(R.string.exit);
        exitDialog.setMessage(R.string.exit_tip);
        exitDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new LogoutTask().execute();
            }
        });
        exitDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        exitDialog.show();
    }
    private class LogoutTask extends AsyncTask<Void, Void, Void> {
        private Dialog mWaitDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(CameraListActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getOpenSDK().logout();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mWaitDialog.dismiss();
            ActivityUtils.goToLoginAgain(CameraListActivity.this);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_CODE){
            if (requestCode == REQUEST_CODE){
                String deviceSerial = intent.getStringExtra(IntentConsts.EXTRA_DEVICE_ID);
                int cameraNo = intent.getIntExtra(IntentConsts.EXTRA_CAMERA_NO,-1);
                int videoLevel = intent.getIntExtra("video_level",-1);
                if (TextUtils.isEmpty(deviceSerial)){
                    return;
                }
                if (videoLevel == -1 || cameraNo == -1){
                    return;
                }
                if (mAdapter.getDeviceInfoList() != null){
                    for (EZDeviceInfo deviceInfo:mAdapter.getDeviceInfoList()){
                        if (deviceInfo.getDeviceSerial().equals(deviceSerial)){
                            if (deviceInfo.getCameraInfoList() != null){
                                for (EZCameraInfo cameraInfo:deviceInfo.getCameraInfoList()){
                                    if (cameraInfo.getCameraNo() == cameraNo){
                                        cameraInfo.setVideoLevel(videoLevel);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * 查询语音留言并下载获取到的留言文件, 可以直接播放或者存放文件
     * 文件名称需要注意防重，重复下载名称需要区分
     */
    private void downloadLeaveMassage(final String deviceSerial) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Calendar mStartTime = Calendar.getInstance();
                mStartTime.set(Calendar.AM_PM, 0);
                mStartTime.set(mStartTime.get(Calendar.YEAR), mStartTime.get(Calendar.MONTH) - 1,
                        mStartTime.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                Calendar mEndTime = Calendar.getInstance();
                mEndTime.set(Calendar.AM_PM, 0);
                mEndTime.set(mEndTime.get(Calendar.YEAR), mEndTime.get(Calendar.MONTH),
                        mEndTime.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
                try {
                    List<EZLeaveMessage> leaveMessages = getOpenSDK().getInstance().getLeaveMessageList(deviceSerial, 0, 20, mStartTime, mEndTime);
                    if (leaveMessages == null || leaveMessages.size() <= 0) {
                        LogUtil.d(TAG, "no leaveMessage");
                    }
                    for (EZLeaveMessage ezLeaveMessage : leaveMessages) {
                        /***文件名称需要注意防重，重复下载名称需要区别**/
                        final File file = new File(Environment.getExternalStorageDirectory(), "EZOpenSDK/LeaveMessage/" + deviceSerial + "-" + ezLeaveMessage.getMsgId());
                        File parent = file.getParentFile();
                        if (parent == null || !parent.exists() || parent.isFile()) {
                            parent.mkdirs();
                        }
                        getOpenSDK().getLeaveMessageData(mHandler, ezLeaveMessage, new EZOpenSDKListener.EZLeaveMessageFlowCallback() {
                            @Override
                            public void onLeaveMessageFlowCallback(int i, byte[] bytes, int i1, String s) {
                                LogUtil.d(TAG, "");
                                LogUtil.d(TAG, "bytes" + bytes);
                                if (!file.exists() || !file.isFile()) {
                                    try {
                                        file.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                FileOutputStream fileOutputStream = null;
                                try {
                                    fileOutputStream = new FileOutputStream(file, false);
                                    if (fileOutputStream != null) {
                                        fileOutputStream.write(bytes);
                                    }
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (fileOutputStream != null) {
                                        try {
                                            fileOutputStream.flush();
                                            fileOutputStream.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });
                    }
                } catch (BaseException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
