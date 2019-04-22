package com.myezopen.myezopen.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import com.myezopen.myezopen.datamanager.PhotoFragment;

import java.util.ArrayList;

/**
 * Created by zheng on 2017/11/27.
 */

public class PhotoPagerAdapter extends FragmentPagerAdapter {

    private final ArrayList<String> urlList;

    public PhotoPagerAdapter(FragmentManager fm, ArrayList<String> urlList) {
        super(fm);
        this.urlList=urlList;
    }

    @Override
    public Fragment getItem(int position) {
        return PhotoFragment.newInstance(urlList.get(position));
    }

    @Override
    public int getCount() {
        return urlList.size();
    }
}
