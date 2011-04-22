package com.honeykang.browser.misc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.honeykang.browser.Constants;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class BitmapHelper {

    private static final String TAG = "FFNBitmap";
    private Bitmap bitmap = null;

    public BitmapHelper(Object image){
        if (image instanceof URL){
            loadFromURL((URL) image);
        } else if (image instanceof byte[]){
            loadFromByteArray((byte[]) image);
        } else if (image instanceof Bitmap){
            bitmap = (Bitmap) image;
        }
    }

    public Bitmap getAsBitmap(){
        return bitmap;
    }

    private void loadFromURL(URL imageUrl){
            try {
                if (imageUrl != null){
                    InputStream is = (InputStream) imageUrl.getContent();
                    if (is != null){
                        bitmap = BitmapFactory.decodeStream(is);
                    }
                }
            } catch (MalformedURLException e){
                Log.e(TAG, e.toString());
                if (Constants.DEBUG){
                    e.printStackTrace();
                }
            } catch (IOException e){
                Log.e(TAG, e.toString());
                if (Constants.DEBUG){
                    e.printStackTrace();
                }
            }
    }

    public byte[] getAsByteArray() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            if (bitmap == null){
                return null;
            }
            bitmap.compress(CompressFormat.PNG, 0, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e){
            Log.e(TAG, e.toString());
            if (Constants.DEBUG){
                e.printStackTrace();
            }
            return null;
        }
    }

    public void loadFromByteArray(byte[] image) {
        bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public Bitmap getRoundedBitmap(){
        int roundPx = 8;
        if (bitmap == null){
            return null;
        }
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                                  bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output); 
        int color = 0xff424242; 
        Paint paint = new Paint(); 
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()); 
        RectF rectF = new RectF(rect); 
        paint.setAntiAlias(true); 
        canvas.drawARGB(0, 0, 0, 0); 
        paint.setColor(color); 
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint); 
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint); 
        return output; 
    }
}
