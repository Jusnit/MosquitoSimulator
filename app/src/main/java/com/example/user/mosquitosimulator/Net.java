package com.example.user.mosquitosimulator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;

/**
 * Created by user on 2015/5/12.
 */
public class Net extends View{
    private Bitmap netBitmap;
    public Net(Context context){
        super(context);
        netBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.net);
    }
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
                                   double newHeight) {
        // get Height and Width of the picture
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // instaniate Matrix object used to deal with picture
        Matrix matrix = new Matrix();
        // 計算寬高縮放比率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 縮放圖片的動作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }


}
