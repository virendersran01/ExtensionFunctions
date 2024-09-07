package com.virtualstudios.extensionfunctions.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.WorkerThread
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutionException

fun setImage(view: ImageView, url: String, @DrawableRes placeHolder: Int) {
    setObject(view, url, placeHolder)
}

fun setImage(view: ImageView, url: String, options: RequestOptions) {
    setObject(view, url, options)
}

fun setImage(view: ImageView, file: File, @DrawableRes placeHolder: Int) {
    setObject(view, file, placeHolder)
}

fun setImage(view: ImageView, file: File, options: RequestOptions) {
    setObject(view, file, options)
}

private fun setObject(view: ImageView, `object`: Any, @DrawableRes placeHolder: Int) {
    Glide.with(view.context.applicationContext)
        .load(`object`)
        .apply(RequestOptions().placeholder(placeHolder))
        .thumbnail(0.1f)
        .into(view)
}

private fun setObject(view: ImageView, `object`: Any, options: RequestOptions) {
    Glide.with(view.context.applicationContext)
        .load(`object`)
        .apply(options)
        .thumbnail(0.1f)
        .into(view)
}

@WorkerThread
@Throws(
    ExecutionException::class,
    InterruptedException::class
)
fun getImage(context: Context, uri: Uri, width: Int, height: Int): WeakReference<Bitmap> {
    val bmp = Glide.with(context.applicationContext)
        .asBitmap()
        .load(uri)
        .apply(
            RequestOptions()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
        )
        .submit()
    return WeakReference(bmp.get())
}