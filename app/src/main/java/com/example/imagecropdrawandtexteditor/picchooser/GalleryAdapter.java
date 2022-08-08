package com.example.imagecropdrawandtexteditor.picchooser;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.imagecropdrawandtexteditor.R;



class GalleryAdapter extends BaseAdapter {

    private final Context context;
    private final List<GridItem> items;
    private final LayoutInflater mInflater;
//


    public GalleryAdapter(final Context context, final List<GridItem> buckets) {
        this.items = buckets;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        ImageView imageView;
        if (convertView == null) {
            imageView = (ImageView) mInflater.inflate(R.layout.imageitem, null);
        } else {
            imageView = (ImageView) convertView;
        }
        Glide.with(context)
            .load("file://" + items.get(position).path).placeholder(R.drawable.ic_placeholder_image)
            .into(imageView);
        return imageView;
    }
}
class   GridItem {
    final String name;
    final String path;
     String imageTaken = "";
    final long imageSize;
    public GridItem(final String n, final String p, String imageTaken,final long imageSize) {
        name = n;
        path = p;
        this.imageTaken = imageTaken;
        this.imageSize = imageSize;
    }
}
