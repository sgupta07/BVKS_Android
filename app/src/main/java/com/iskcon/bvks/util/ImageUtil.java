package com.iskcon.bvks.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.iskcon.bvks.R;

import java.util.concurrent.ExecutionException;

public class ImageUtil {

    /**
     * Loads rounded thumbnail from given uri.
     *
     * @param imageView ImageView reference.
     * @param uri       String uri of the image.
     */
    public static void loadThumbnail(@NonNull ImageView imageView, @NonNull String uri) {
        Glide.with(imageView)
                .load(uri)
                .dontAnimate()
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .thumbnail(0.25f)
                .error(R.drawable.bvks_thumbnail_v2)
                .into(imageView);
    }

    public static Bitmap loadThumbnail(@NonNull Context context, @DrawableRes int resId) {
        try {
            return Glide.with(context)
                    .asBitmap()
                    .load(resId)
                    .dontAnimate()
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .thumbnail(0.25f)
                    .error(R.drawable.bvks_thumbnail_v2)
                    .into(100, 100).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loadPlayerImage(@NonNull ImageView imageView, @NonNull String uri) {
        Glide.with(imageView)
                .load(uri)
                .dontAnimate()
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .error(R.drawable.bvks_thumbnail_v2)
                .into(imageView);
    }

    public static void loadThumbnail(@NonNull ImageView imageView, @DrawableRes int drawableRes) {
        Glide.with(imageView)
                .load(drawableRes)
                .dontAnimate()
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .into(imageView);
    }
}
