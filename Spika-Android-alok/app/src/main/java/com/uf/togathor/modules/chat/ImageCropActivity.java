package com.uf.togathor.modules.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.edmodo.cropper.CropImageView;
import com.uf.togathor.R;
import com.uf.togathor.uitems.HookUpProgressDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ImageCropActivity extends ActionBarActivity {

    File imageFile;
    Bitmap croppedImage;
    CropImageView imageCropper;
    Toolbar toolbar;
    private MaterialMenuDrawable materialMenu;
    LinearLayout cropperLayout;
    private HookUpProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        progressBar = new HookUpProgressDialog(this);
        Intent callerIntent = getIntent();

        imageFile = new File(callerIntent.getStringExtra("image_file"));
        imageCropper = (CropImageView) findViewById(R.id.CropImageView);
        croppedImage = BitmapFactory.decodeFile(imageFile.toString());
        imageCropper.setGuidelines(CropImageView.DEFAULT_GUIDELINES);
        imageCropper.setImageBitmap(croppedImage);
        cropperLayout = (LinearLayout) findViewById(R.id.CropperLayout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        toolbar.setNavigationIcon(materialMenu);
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.CHECK, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_image_cropper, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                new AsyncCropImage().execute();
                return true;

            case R.id.menu_cancel_crop:
                finish();
                break;

        }
        return (super.onOptionsItemSelected(item));
    }

    private class AsyncCropImage extends AsyncTask  {

        @Override
        protected void onPreExecute() {
            progressBar.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {

            croppedImage = imageCropper.getCroppedImage();
            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                croppedImage.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                fos.write(bos.toByteArray());
                fos.flush();
                fos.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressBar.dismiss();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("image_file", imageFile.getAbsolutePath());
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}
