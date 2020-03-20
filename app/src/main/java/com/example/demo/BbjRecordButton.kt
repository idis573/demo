package com.example.demo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator

/**
 * 圆形进度条控件
 * Author: JueYes jueyes_1024@163.com
 * Time: 2019-08-07 15:38
 */

class BbjRecordButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        const val ANIM_BREATHING_LIGHT_DURATION = 1800L
    }

    enum class Status(val status: Int) {
        IDLE(0),
        START(1),
        PAUSE(2)
    }

    private var mStatus = Status.IDLE

    var isPause: Boolean
        set(value) {
            isPause = value
        }
        get() {
            return mStatus != Status.START
        }

    private val mProgressPaint: Paint   // 绘制画笔
    private val mSrcPaint: Paint        // 呼吸圈画笔
    private lateinit var mRectF: RectF  // 绘制区域
    private var mRecordSecond = 0f     // 录制时长
    private var mTotalSecond = 300f // 总时长
    private var mRecordTimeList = mutableListOf<Int>()
    private var mPauseBitmap: Bitmap
    private var mStartBitmap: Bitmap

    //中心点
    private var mCenterX = 0f
    private var mCenterY = 0f

    //icon呼吸圈半径
    private var mBlnMaxRadius = 0f
    private var mBlnRadius = 0f
    private var mBlnAnimator: ValueAnimator? = null


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BbjRecordButton)
        // 初始化进度圆环画笔
        mProgressPaint = Paint()
        mProgressPaint.style = Paint.Style.STROKE    // 只描边，不填充
        mProgressPaint.strokeCap = Paint.Cap.ROUND   // 设置圆角
        mProgressPaint.isAntiAlias = true              // 设置抗锯齿
        mProgressPaint.isDither = true                 // 设置抖动
        mProgressPaint.strokeWidth =
            typedArray.getDimension(R.styleable.BbjRecordButton_progressWidth, 10f)
        mProgressPaint.color =
            typedArray.getColor(R.styleable.BbjRecordButton_progressColor, Color.BLUE)
        mPauseBitmap = BitmapFactory.decodeResource(
            resources,
            typedArray.getResourceId(R.styleable.BbjRecordButton_srcPause, R.drawable.pause)
        )

        //初始化呼吸圈画笔
        mSrcPaint = Paint()
        mSrcPaint.style = Paint.Style.FILL    // 只描边，不填充
        mSrcPaint.strokeCap = Paint.Cap.ROUND   // 设置圆角
        mSrcPaint.isAntiAlias = true            // 设置抗锯齿
        mSrcPaint.isDither = true               // 设置抖动
        mSrcPaint.color = typedArray.getColor(R.styleable.BbjRecordButton_progressColor, Color.BLUE)

        mStartBitmap = BitmapFactory.decodeResource(
            resources,
            typedArray.getResourceId(R.styleable.BbjRecordButton_srcStart, R.drawable.shoot)
        )
        // 初始化进度
        mTotalSecond = typedArray.getFloat(R.styleable.BbjRecordButton_totalSecond, 0f)

        mBlnMaxRadius = typedArray.getDimension(R.styleable.BbjRecordButton_blnRadius, 228f)

        typedArray.recycle()

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //1、外圆圈
        drawCircle(canvas)

        //2、icon呼吸
        canvas.drawCircle(mCenterX, mCenterY, mBlnRadius, mSrcPaint)

        //3、icon
        drawIcon(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
        when (mStatus) {
            Status.IDLE -> canvas.drawCircle(
                mCenterX,
                mCenterY,
                mStartBitmap.width / 2f + mProgressPaint.strokeWidth + 9,
                mProgressPaint
            )
            else -> canvas.drawArc(
                mRectF,
                275f,
                360f * mRecordSecond / mTotalSecond,
                false,
                mProgressPaint
            )
        }

    }

    private fun drawIcon(canvas: Canvas) {
        fun drawSrc(canvas: Canvas, bitmap: Bitmap) {
            val left = mRectF.left + (mRectF.right - mRectF.left - bitmap.width) / 2.0f
            val top = mRectF.top + (mRectF.bottom - mRectF.top - bitmap.height) / 2.0f
            canvas.drawBitmap(bitmap, left, top, mProgressPaint)
        }
        when (mStatus) {
            Status.IDLE -> drawSrc(canvas, mStartBitmap)
            else -> drawSrc(canvas, mPauseBitmap)
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val viewWide = measuredWidth - paddingLeft - paddingRight
        val viewHigh = measuredHeight - paddingTop - paddingBottom
        val mRectLength = mProgressPaint.strokeWidth.toInt() / 2

        mRectF = RectF(
            (paddingLeft + mRectLength).toFloat(),
            (paddingTop + mRectLength).toFloat(),
            (paddingLeft + viewWide - mRectLength).toFloat(),
            (paddingTop + viewHigh - mRectLength).toFloat()
        )

        mCenterX = measuredWidth / 2F
        mCenterY = measuredHeight / 2F

    }

    fun start() {
        mStatus = Status.START
        if (mBlnAnimator != null && mBlnAnimator!!.isRunning) {
            return
        }
        if (mBlnAnimator == null) {
            mBlnAnimator = ValueAnimator.ofFloat(mStartBitmap.width / 2f, mBlnMaxRadius)
        }
        mBlnAnimator?.run {
            addUpdateListener { animation ->
                mBlnRadius = animation.animatedValue as Float
                mRecordSecond = mRecordTimeList.sum() + (animation.currentPlayTime / 1000f)
                Log.e("wqs","sum:${mRecordTimeList.sum()}")
                invalidate()
            }
            duration = ANIM_BREATHING_LIGHT_DURATION
            repeatMode = ValueAnimator.REVERSE
            repeatCount = -1
            start()
        }
    }

    fun pause() {
        mStatus = Status.PAUSE
        mBlnAnimator?.run {
            pause()
            mRecordTimeList.add((currentPlayTime / 1000f).toInt())
            Log.e("wqs","item:${(currentPlayTime / 1000f).toInt()}")
            mBlnAnimator = null
            mBlnRadius = mStartBitmap.width / 2f
            invalidate()
        }
    }

}