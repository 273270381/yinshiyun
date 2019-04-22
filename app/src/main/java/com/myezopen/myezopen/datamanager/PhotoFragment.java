package com.myezopen.myezopen.datamanager;


import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.myezopen.myezopen.R;
import com.myezopen.myezopen.Utils.ToastUtils;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoFragment extends Fragment {
    private String url;
    private PhotoView mPhotoView;
    private VideoView mVideoView;
    private ImageView mvideoplay;
    private String UrlEnd;

    /**
     * 获取这个fragment需要展示图片的url
     * @param url
     * @return
     */
    public static PhotoFragment newInstance(String url) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getArguments().getString("url");
        UrlEnd = url.substring(url.lastIndexOf(".") + 1, url.length()).toLowerCase();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_img, container, false);
        mPhotoView = (PhotoView) view.findViewById(R.id.photoview);
        mVideoView = (VideoView) view.findViewById(R.id.videoview);
        mvideoplay = (ImageView) view.findViewById(R.id.videoplay);
        //设置缩放类型，默认ScaleType.CENTER（可以不设置）
        mPhotoView.setScaleType(ImageView.ScaleType.CENTER);
        //设置控制条
        MediaController mediaController = new MediaController(inflater.getContext());
        mVideoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(mVideoView);
        mediaController.show();

        mVideoView.setVideoURI(Uri.parse(url));
        mPhotoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //ToastUtils.showToast(getContext(),"长按事件");
                return true;
            }
        });
        mPhotoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                //ToastUtils.showToast(getContext(),"点击事件，真实项目中可关闭activity");
            }
        });
        mvideoplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoView.setClickable(true);
                mVideoView.start();
                mVideoView.setVisibility(View.VISIBLE);
                mvideoplay.setVisibility(View.GONE);
                mPhotoView.setVisibility(View.GONE);
            }
        });
        com.bumptech.glide.Glide.with(getContext())
                .load(url)
                .placeholder(R.mipmap.ic_launcher)//加载过程中图片未显示时显示的本地图片
                .error(R.mipmap.ic_launcher)//加载异常时显示的图片
//                .centerCrop()//图片图填充ImageView设置的大小
                .fitCenter()//缩放图像测量出来等于或小于ImageView的边界范围,该图像将会完全显示
                .into(mPhotoView);
        if(UrlEnd.equals("mp4")){
            mvideoplay.setVisibility(View.VISIBLE);
        }
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mvideoplay.setVisibility(View.VISIBLE);
            }
        });
        return view;
    }

}
