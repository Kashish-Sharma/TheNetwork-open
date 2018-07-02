package com.thenetwork.app.android.thenetwork.HelperUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.thenetwork.app.android.thenetwork.R;

/**
 * Created by Kashish on 17-06-2018.
 */

public class GlideLoadImage {

    public static void loadImage(Context context, ImageView view, String thumb, String image){
        RequestBuilder<Drawable> thumbnailRequestBlogImage = Glide
                .with(context)
                .load(thumb)
                .apply(new RequestOptions().transform(new BlurTransformation(context)));

        Glide.with(context).load(image)
                .apply(new RequestOptions().dontAnimate().dontTransform().placeholder(R.drawable.rectangle))
                .thumbnail(thumbnailRequestBlogImage)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource
                            dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(view);
    }

    public static void loadSmallImage(Context context, ImageView view, String thumb, String image){
        RequestBuilder<Drawable> thumbnailRequestBlogImage = Glide
                .with(context)
                .load(thumb);

        Glide.with(context).load(image)
                .apply(new RequestOptions().dontAnimate().dontTransform().placeholder(R.drawable.rectangle))
                .thumbnail(thumbnailRequestBlogImage)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource
                            dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(view);
    }

}
