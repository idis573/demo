package com.example.demo.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.RelativeLayout
import com.example.demo.R
import kotlinx.android.synthetic.main.bbj_view_record_pannel.view.*

/**
 * Author: 573
 * Date: 2020-03-20 13:41
 * Description: 视频拍摄底部控件栏
 */
class RecordPannelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val goneAlphaAnim = AlphaAnimation(1f, 0f).apply {
        duration = 150L
    }

    private val visibleAlphaAnim = AlphaAnimation(0f, 1f).apply {
        duration = 150L
    }

    init {
        inflate(context, R.layout.bbj_view_record_pannel, this)
        initView()
    }

    private fun initView() {
        btn_record.setOnClickListener {
            if (btn_record.isPause) {
                btn_record.startRecord()
            } else {
                btn_record.pauseRecord()
            }
        }

        btn_record.recordListener = object : BbjRecordButton.RecordListener {
            override fun onPause() {
                visibleView(btn_close, btn_delete, btn_next)
            }

            override fun onComplete() {
            }

            override fun onStart() {
                goneView(btn_close, btn_delete, btn_next)
            }

        }
    }

    private fun visibleView(vararg views: View) {
        views.forEach {
            it.startAnimation(visibleAlphaAnim)
        }
        visibleAlphaAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                views.forEach {
                    it.visibility = View.VISIBLE
                }
            }

            override fun onAnimationStart(animation: Animation?) {
            }

        })

    }

    private fun goneView(vararg views: View) {
        views.forEach {
            it.startAnimation(goneAlphaAnim)
        }
        goneAlphaAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                views.forEach {
                    it.visibility = View.GONE
                }
            }

            override fun onAnimationStart(animation: Animation?) {
            }

        })

    }
}
