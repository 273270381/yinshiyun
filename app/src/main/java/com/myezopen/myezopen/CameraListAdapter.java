package com.myezopen.myezopen;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.myezopen.myezopen.Utils.EZUtils;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class CameraListAdapter extends BaseAdapter {
    private static final String TAG = "CameraListAdapter";
    private Context mContext = null;
    private List<EZDeviceInfo> mCameraInfoList = null;
    /** 监听对象 */
    private OnClickListener mListener;
    private ExecutorService mExecutorService = null;//线程池
    public Map<String,EZDeviceInfo> mExecuteItemMap = null;

    public CameraListAdapter(Context mContext) {
        this.mContext = mContext;
        mCameraInfoList = new ArrayList<EZDeviceInfo>();
        mExecuteItemMap = new HashMap<String,EZDeviceInfo>();
    }
    public void setOnClickListener(OnClickListener l) {
        mListener = l;
    }

    public void addItem(EZDeviceInfo item) {
        mCameraInfoList.add(item);
    }

    public void removeItem(EZDeviceInfo item) {
        for(int i = 0; i < mCameraInfoList.size(); i++) {
            if(item == mCameraInfoList.get(i)) {
                mCameraInfoList.remove(i);
            }
        }
    }

    public void clearItem() {
        //mExecuteItemMap.clear();
        mCameraInfoList.clear();
    }

    public void clearAll(){
        mCameraInfoList.clear();
        notifyDataSetChanged();
    }

    public List<EZDeviceInfo> getDeviceInfoList(){
        return mCameraInfoList;
    }

    @Override
    public int getCount() {
        return mCameraInfoList.size();
    }

    @Override
    public EZDeviceInfo getItem(int position) {
        EZDeviceInfo item = null;
        if(position>=0 && getCount()>=position){
            item = mCameraInfoList.get(position);
        }
        return item;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 自定义视图
        ViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            // 获取list_item布局文件的视图
            convertView = LayoutInflater.from(mContext).inflate(R.layout.cameralist_small_item,null);

            // 获取控件对象
            viewHolder.iconIv = (ImageView) convertView.findViewById(R.id.item_icon);
            viewHolder.playBtn = (ImageView) convertView.findViewById(R.id.item_play_btn);
            viewHolder.offlineBtn = (ImageView) convertView.findViewById(R.id.item_offline);
            viewHolder.cameraNameTv = (TextView) convertView.findViewById(R.id.camera_name_tv);
            viewHolder.cameraDelBtn = (ImageButton) convertView.findViewById(R.id.camera_del_btn);
            viewHolder.alarmListBtn = (ImageButton) convertView.findViewById(R.id.tab_alarmlist_btn);
            viewHolder.remoteplaybackBtn = (ImageButton) convertView.findViewById(R.id.tab_remoteplayback_btn);
            viewHolder.setDeviceBtn = (ImageButton) convertView.findViewById(R.id.tab_setdevice_btn);
            viewHolder.projectBtn = (ImageButton) convertView.findViewById(R.id.tab_project_btn);
            viewHolder.offlineBgBtn = (ImageView) convertView.findViewById(R.id.offline_bg);

            // 设置点击图标的监听响应函数
            viewHolder.playBtn.setOnClickListener(mOnClickListener);
            // 设置删除的监听响应函数
            viewHolder.cameraDelBtn.setOnClickListener(mOnClickListener);
            // 设置报警列表的监听响应函数
            viewHolder.alarmListBtn.setOnClickListener(mOnClickListener);
            // 设置历史回放的监听响应函数
            viewHolder.remoteplaybackBtn.setOnClickListener(mOnClickListener);
            // 设置设备设置的监听响应函数
            viewHolder.setDeviceBtn.setOnClickListener(mOnClickListener);
            //设置项目管理的监听响应函数
            viewHolder.projectBtn.setOnClickListener(mOnClickListener);
            // 设置控件集到convertView
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // 设置position
        viewHolder.playBtn.setTag(position);
        viewHolder.remoteplaybackBtn.setTag(position);
        viewHolder.alarmListBtn.setTag(position);
        viewHolder.setDeviceBtn.setTag(position);
        viewHolder.projectBtn.setTag(position);

        final EZDeviceInfo deviceInfo = getItem(position);
        final EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo,0);
        if(deviceInfo != null){
            if (deviceInfo.getStatus()==2){
                viewHolder.offlineBtn.setVisibility(View.VISIBLE);
                viewHolder.offlineBgBtn.setVisibility(View.VISIBLE);
                viewHolder.playBtn.setVisibility(View.GONE);
            }else{
                viewHolder.offlineBtn.setVisibility(View.GONE);
                viewHolder.offlineBgBtn.setVisibility(View.GONE);
                viewHolder.playBtn.setVisibility(View.VISIBLE);
            }
            viewHolder.cameraNameTv.setText(deviceInfo.getDeviceName());
            viewHolder.iconIv.setVisibility(View.VISIBLE);
            String imageUrl = deviceInfo.getDeviceCover();
            if(!TextUtils.isEmpty(imageUrl)){
                Glide.with(mContext).load(imageUrl).placeholder(R.drawable.device_other).into(viewHolder.iconIv);
            }
        }
        return convertView;
    }
    public void shutDownExecutorService(){
        if (mExecutorService != null) {
            if(!mExecutorService.isShutdown()){
                mExecutorService.shutdown();
            }
            mExecutorService = null;
        }
    }
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mListener!=null){
                int position = (Integer) v.getTag();
                switch (v.getId()){
                    case R.id.item_play_btn:
                        mListener.onPlayClick(CameraListAdapter.this, v, position);
                        break;
                    case R.id.tab_remoteplayback_btn:
                        mListener.onRemotePlayBackClick(CameraListAdapter.this, v, position);
                        break;
                    case R.id.tab_alarmlist_btn:
                        mListener.onAlarmListClick(CameraListAdapter.this, v, position);
                        break;
                    case R.id.tab_setdevice_btn:
                        mListener.onSetDeviceClick(CameraListAdapter.this, v, position);
                        break;
                    case R.id.camera_del_btn:
                        mListener.onDeleteClick(CameraListAdapter.this, v, position);
                        break;
                    case R.id.tab_project_btn:
                        mListener.onProjectClick(CameraListAdapter.this,v,position);
                        break;
                }
            }
        }
    };
    /**
     * 自定义控件集合
     *
     * @author dengsh
     * @data 2012-6-25
     */
    public static class ViewHolder{
        public ImageView iconIv;

        public ImageView playBtn;

        public ImageView offlineBtn;

        public TextView cameraNameTv;

        public ImageButton cameraDelBtn;

        public ImageButton alarmListBtn;

        public ImageButton remoteplaybackBtn;

        public ImageButton setDeviceBtn;

        public ImageView offlineBgBtn;

        public ImageButton projectBtn;
    }
    public interface OnClickListener{
        public void onPlayClick(BaseAdapter adapter,View view,int position);
        public void onDeleteClick(BaseAdapter adapter,View view,int position);
        public void onAlarmListClick(BaseAdapter adapter, View view, int position);
        public void onRemotePlayBackClick(BaseAdapter adapter, View view, int position);
        public void onSetDeviceClick(BaseAdapter adapter, View view, int position);
        public void onDevicePictureClick(BaseAdapter adapter, View view, int position);
        public void onDeviceVideoClick(BaseAdapter adapter, View view, int position);
        public void onDeviceDefenceClick(BaseAdapter adapter, View view, int position);
        public void onProjectClick(BaseAdapter adapter,View view, int position);
    }
}
