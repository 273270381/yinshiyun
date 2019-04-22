package com.myezopen.myezopen.datamanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.myezopen.myezopen.R;
import com.myezopen.myezopen.Utils.ToastUtils;
import com.myezopen.myezopen.adapter.PhotoPagerAdapter;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class PictureActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private TextView tvNum;
    private ArrayList<String> urlList;
    private int position;
    private ImageButton shareBtn;
    private ImageButton otherBtn;
    private ImageButton delateBtn;
    private Uri uri;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        initView();
    }

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        shareBtn = (ImageButton) findViewById(R.id.share);
        otherBtn = (ImageButton) findViewById(R.id.other);
        delateBtn = (ImageButton) findViewById(R.id.delate);
        //tvNum = (TextView) findViewById(R.id.tv_num);
        urlList = new ArrayList<>();
        Intent intent = getIntent();
        urlList = intent.getStringArrayListExtra("list");
        position = intent.getIntExtra("position",0);
        PhotoPagerAdapter viewPagerAdapter = new PhotoPagerAdapter(getSupportFragmentManager(), urlList);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(position);
        uri = Uri.parse(urlList.get(position));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //tvNum.setText(String.valueOf(position + 1) + "/" + urlList.size());
                uri = Uri.parse(urlList.get(position));
                path = urlList.get(position);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImg("分享","分享","分享",uri);
            }
        });
        otherBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                if (delateBtn.getVisibility()==0){
                    delateBtn.setVisibility(View.GONE);
                }else if (delateBtn.getVisibility()==8){
                    delateBtn.setVisibility(View.VISIBLE);
                }
            }
        });
        delateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    File file = new File(path);
                    if (file.exists()&&file.isFile()){
                        file.delete();
                        Toast.makeText(PictureActivity.this,"文件已删除!",Toast.LENGTH_SHORT).show();
                        finish();
                    }
            }
        });
    }
    /**
     * 分享图片和文字内容
     *
     * @param dlgTitle
     *            分享对话框标题
     * @param subject
     *            主题
     * @param content
     *            分享内容（文字）
     * @param uri
     *            图片资源URI
     */
    private void shareImg(String dlgTitle, String subject, String content, Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (content != null && !"".equals(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }
        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            startActivity(intent);
        }
    }
}
