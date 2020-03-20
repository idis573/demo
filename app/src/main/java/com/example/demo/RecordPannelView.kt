package com.example.demo

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
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
    init {
        inflate(context, R.layout.bbj_view_record_pannel, this)
        initView()
    }

    private fun initView() {
        btn_record.setOnClickListener {
            if (btn_record.isPause) {
                btn_record.start()
            } else {
                btn_record.pause()
            }
        }
    }
}
