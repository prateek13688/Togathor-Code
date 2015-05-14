package com.uf.togathor.modules.chat;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.uf.togathor.R;
import com.uf.togathor.adapters.ImageViewerAdapter;
import com.uf.togathor.management.FileCache;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by alok on 1/3/15.
 */
public class ImageViewerActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private ArrayList<String> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imager_view);
        toolbar = (Toolbar) findViewById(R.id.image_viewer_toolbar);
        viewPager = (ViewPager) findViewById(R.id.image_viewpager);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle("Photos");
        }

        Intent intent = getIntent();
        images = intent.getStringArrayListExtra("images");
        int finalPosition = intent.getIntExtra("currentimage", 0);

        viewPager.setAdapter(new ImageViewerAdapter(this, images));
        viewPager.setCurrentItem(finalPosition, true);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_image_viewer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_image_share:
                return shareImage();

            case android.R.id.home:
                finish();
                return true;

            default:
                return (super.onOptionsItemSelected(item));
        }
    }

    private boolean shareImage()    {
        int currentImage = viewPager.getCurrentItem();
        FileCache fileCache = new FileCache(this);
        File image = fileCache.getFile(images.get(currentImage));
        if (image.exists())  {
            Uri uri = Uri.fromFile(image);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/png");
            startActivity(Intent.createChooser(shareIntent, "Share image using"));
            return true;
        }
        else    {
            return false;
        }
    }
}
