package com.dcalabrese22.dan.chatter.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by dcalabrese on 11/29/2017.
 */

public class LogoAsyncTask extends AsyncTask<ImageView, Void, Bitmap> {

    ImageView imageView;

    @Override
    protected Bitmap doInBackground(ImageView... imageViews) {
        imageView = imageViews[0];
        return getLogo(imageView.getTag().toString());
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    public Bitmap getLogo(String url) {
        try {
            InputStream inputStream = (InputStream) new URL(url).getContent();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
