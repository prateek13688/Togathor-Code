package com.uf.togathor.adapters;

/**
 * Created by alok on 1/3/15
 */

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.utils.constants.Const;
import com.uf.togathor.uitems.views.TouchImageView;

import java.util.ArrayList;

public class ImageViewerAdapter extends PagerAdapter {

    private Activity activity;
    private ArrayList<String> imagePaths;
    private TouchImageView imgDisplay;

    ProgressBarCircularIndeterminate progress;

    public ImageViewerAdapter(Activity activity,
                              ArrayList<String> imagePaths) {
        this.activity = activity;
        this.imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return this.imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_image_viewer, container,
                false);

        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);
        progress = (ProgressBarCircularIndeterminate) viewLayout.findViewById(R.id.progress_bar);
        progress.setVisibility(View.VISIBLE);

        Log.d("Image Viewer", Togathor.getInstance().getBaseUrlWithSufix(
                Const.FILE_DOWNLOADER_URL) + Const.FILE + "=" + imagePaths.get(position));

        Glide.with(activity)
                .load(Togathor.getInstance().getBaseUrlWithSufix(
                        Const.FILE_DOWNLOADER_URL) + Const.FILE + "=" + imagePaths.get(position))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        e.printStackTrace();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progress.setVisibility(View.GONE);
                        return false;
                    }
                })
                .centerCrop()
                .into(imgDisplay);
        container.addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);

    }
}
