package com.uf.togathor;

/**
 * Created by Alok on 3/25/2015.
 */

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.utils.constants.Const;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


public class ImageRecognitionActivity extends ActionBarActivity implements ImageChooserListener {

    TextView saveImage;
    TextView matchImage;
    private ImageChooserManager imageChooserManager;
    private int chooserType;
    private Webb webb;
    String filePath;

    private boolean match = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_recognition);

        saveImage = (TextView) findViewById(R.id.save_image);
        matchImage = (TextView) findViewById(R.id.match_image);

        webb = Webb.create();

        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                match = false;
                chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
                imageChooserManager = new ImageChooserManager(ImageRecognitionActivity.this,
                        ChooserType.REQUEST_CAPTURE_PICTURE, "ImageLock", true);
                imageChooserManager.setImageChooserListener(ImageRecognitionActivity.this);
                try {
                    filePath = imageChooserManager.choose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        matchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                match = true;
                chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
                imageChooserManager = new ImageChooserManager(ImageRecognitionActivity.this,
                        ChooserType.REQUEST_CAPTURE_PICTURE, "ImageLock", true);
                imageChooserManager.setImageChooserListener(ImageRecognitionActivity.this);
                try {
                    filePath = imageChooserManager.choose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageChosen(final ChosenImage chosenImage) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                if (chosenImage != null) {
                    Log.d("ImageChoose", chosenImage.getFilePathOriginal());
                    if (match) {

                        Response<JSONObject> response = webb.post("https://fashionbase-image-server.p.mashape.com/api/match")
                                .header("X-Mashape-Key", "bIleDR0ag0mshujqa2ePUnUJsxj8p12NyrVjsnc9r4BArRaPMV")
                                .param("img", new File(chosenImage.getFilePathOriginal()))
                                .asJsonObject();

                        Log.d("ImageListener", response.getResponseMessage());
                    } else {
                        new UploadImageAsync(chosenImage.getFilePathOriginal()).execute();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onError(String s) {

        Log.d("ImageListener", s);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK
                && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
        }
    }

    private void reinitializeImageChooser() {
        imageChooserManager = new ImageChooserManager(this, chooserType,
                "ImageLock", true);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }

    private class UploadImageAsync extends AsyncTask<Void, Void, Void> {

        String file;
        String uploadID;

        private UploadImageAsync(String file) {
            this.file = file;
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Log.d("FileUploader", "Uploading file " + file);
                uploadID = CouchDB.uploadFile(file);

                Log.d("FileUploader", "Upload complete with file URL " + Togathor.getInstance().getBaseUrlWithSufix(
                        Const.FILE_DOWNLOADER_URL) + Const.FILE + "=" + uploadID);

                Response<String> response = webb.post("https://fashionbase-image-server.p.mashape.com/api/img/" + uploadID)
                        .header("X-Mashape-Key", "bIleDR0ag0mshujqa2ePUnUJsxj8p12NyrVjsnc9r4BArRaPMV")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Accept", "text/plain")
                        .param("img", Togathor.getInstance().getBaseUrlWithSufix(
                                Const.FILE_DOWNLOADER_URL) + Const.FILE + "=" + uploadID)
                        .asString();

                if(response.getBody() != null)
                    Log.d("ImageListener", response.getBody());
                Log.d("ImageListener", response.getResponseMessage());

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}