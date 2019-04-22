package com.myezopen.myezopen.datamanager;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.myezopen.myezopen.R;
import com.myezopen.myezopen.Utils.DataUtils;
import com.myezopen.myezopen.adapter.ImageRecyclerAdapter;
import com.myezopen.myezopen.adapter.OnRecyclerItemClickListener;
import com.videogo.openapi.bean.EZDeviceInfo;
import java.util.ArrayList;
import java.util.List;

public class RecycleViewActivity extends Activity {
    private RecyclerView rv;
    private TextView tv;
    private List<String> datalist;
    private ImageRecyclerAdapter adapter;
    private EZDeviceInfo deviceInfo;
    private int width;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_view);
        initRecycler();
    }

    private void initRecycler() {
        initView();
        if(adapter == null){
            rv.setLayoutManager(new GridLayoutManager(this,width/350));
            rv.setItemAnimator(new DefaultItemAnimator());
            adapter = new ImageRecyclerAdapter(this,datalist);
            adapter.setItemClickListener(new OnRecyclerItemClickListener() {
                @Override
                protected void click(View item, int position) {
                    startActivity(RecycleViewActivity.this, (ArrayList<String>) datalist,position);
                }
            });
            rv.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        datalist.clear();
        datalist = DataUtils.getImagePathFromSD(datalist,deviceInfo.getDeviceName());
        adapter.notifyDataSetChanged();
    }

    public static void startActivity(Context context, ArrayList<String> list, int position){
        Intent intent = new Intent(context,PictureActivity.class);
        intent.putExtra("position",position);
        intent.putStringArrayListExtra("list", (ArrayList<String>) list);
        context.startActivity(intent);
    }
    private void initView() {
        rv = (RecyclerView) findViewById(R.id.recyclerView);
        tv = (TextView) findViewById(R.id.text);
        Intent intent = getIntent();
        deviceInfo = intent.getParcelableExtra("projectmanager");
        width = getScreenProperty();
        datalist = new ArrayList<>();
        datalist = DataUtils.getImagePathFromSD(datalist,deviceInfo.getDeviceName());
        if (datalist.size()==0){
            rv.setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
        }
    }
    private int getScreenProperty(){
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;  //屏幕宽度(像素)
        float density = dm.density;  //屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        return width;
    }
}
