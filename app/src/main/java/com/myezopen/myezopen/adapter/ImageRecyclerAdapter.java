package com.myezopen.myezopen.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.myezopen.myezopen.R;
import com.myezopen.myezopen.Utils.DataUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ImageHolder> {
    private List<String> dataList;
    private Context context;
    private LayoutInflater inflater;
    private OnRecyclerItemClickListener itemClickListener;

    public ImageRecyclerAdapter(Context context,List<String> dataList   ) {
        this.dataList = dataList;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageHolder(inflater.inflate(R.layout.item_image,parent,false),itemClickListener);
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        String FileEnd = dataList.get(position).substring(dataList.get(position).lastIndexOf(".") + 1, dataList.get(position).length()).toLowerCase();
        if (FileEnd.equals("mp4")){
            Bitmap bitmap = DataUtils.getVideoThumbnail(dataList.get(position),350,200, MediaStore.Video.Thumbnails.MINI_KIND);
            holder.image.setImageBitmap(bitmap);
            holder.imgPlay.setVisibility(View.VISIBLE);
        }else {
            Picasso.with(context).load(new File(dataList.get(position))).resize(350,200).centerCrop().into(holder.image);
        }
    }

    public void setItemClickListener(OnRecyclerItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public static class ImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView image;
        private ImageView imgPlay;
        private OnRecyclerItemClickListener itemClickListener;

        public ImageHolder(View itemView, OnRecyclerItemClickListener itemClickListener) {
            super(itemView);
            this.itemClickListener = itemClickListener;
            itemView.setOnClickListener(this);
            image = (ImageView) itemView.findViewById(R.id.image);
            imgPlay = (ImageView) itemView.findViewById(R.id.VideoFlag);
        }
        @Override
        public void onClick(View view) {
            if(itemClickListener == null) return;
            itemClickListener.click(itemView,getAdapterPosition());
        }
    }
}
