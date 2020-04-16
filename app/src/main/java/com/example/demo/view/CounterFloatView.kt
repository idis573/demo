package com.example.demo.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.ImageViewTarget
import com.example.demo.R
import sun.security.krb5.internal.KDCOptions.with


class CounterFloatView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var infos = arrayListOf<CounterInfo>(
        CounterInfo(
            "https://upload.jianshu.io/users/upload_avatars/4134622/1a81262accce.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/96/h/96/format/webp",
            ""
        )
    )
    var mode: Mode = Mode.RIGHT

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when (infos.size) {
            1 -> {
                Glide.with(context)
                    .load(infos[0].url)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(
                        R.drawable.btn_round_normal)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(object :CustomTarget<Bitmap>())
            }
        }

    }


}

enum class Mode {
    LEFT, RIGHT
}

data class CounterInfo(var url: String, val time: String)