package com.example.demo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.PI

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

    enum class Status {
        IDLE,
        RECORDING,
        PAUSE
    }

    enum class PaintType {
        PROGRESS,
        PROGRESS_PAUSE,
        BLN_ANIM,
        BLN_BG
    }

    private var mStatus = Status.IDLE

    var isPause: Boolean
        set(value) {
            isPause = value
        }
        get() {
            return mStatus != Status.RECORDING
        }

    //画笔
    private val mPaint: Paint = Paint()

    //外圈圆环绘制区域
    private lateinit var mRectF: RectF
    //当前录制时长
    private var mRecordSecond = 0f
    //总时长
    private var mTotalSecond = 300f
    //暂停时列表时长
    private var mRecordTimeList = mutableListOf<Float>()

    private var mPauseBitmap: Bitmap
    private var mStartBitmap: Bitmap

    //中心点
    private var mCenterX = 0f
    private var mCenterY = 0f

    //icon呼吸圈半径
    private var mBlnMaxRadius = 0f

    //暂停弧长的角度
    private var mPauseArcDegree = 0f

    // 呼吸圈画笔
    private var mBlnRadius = 0f
    private var mBlnAnimator: ValueAnimator? = null

    private var mProgressColor = Color.RED
    private var mBlnBgColor = Color.WHITE


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BbjRecordButton)

        mPauseBitmap = BitmapFactory.decodeResource(
            resources,
            typedArray.getResourceId(R.styleable.BbjRecordButton_srcPause, R.drawable.pause)
        )

        mPaint.strokeWidth =
            typedArray.getDimension(R.styleable.BbjRecordButton_progressWidth, 10f)
        mPaint.isAntiAlias = true
        mPaint.isDither = true

        mProgressColor = typedArray.getColor(R.styleable.BbjRecordButton_progressColor, Color.RED)

        mBlnBgColor =
            typedArray.getColor(R.styleable.BbjRecordButton_blnBackgroundColor, Color.WHITE)


        mStartBitmap = BitmapFactory.decodeResource(
            resources,
            typedArray.getResourceId(R.styleable.BbjRecordButton_srcStart, R.drawable.shoot)
        )
        // 初始化进度
        mTotalSecond = typedArray.getFloat(R.styleable.BbjRecordButton_totalSecond, 0f)

        mBlnMaxRadius = typedArray.getDimension(R.styleable.BbjRecordButton_blnMaxRadius, 228f)

        typedArray.recycle()

    }

    private fun getPaint(type: PaintType): Paint {
        when (type) {
            PaintType.BLN_ANIM -> {
                mPaint.color = mProgressColor
                mPaint.style = Paint.Style.FILL
            }
            PaintType.PROGRESS -> {
                mPaint.color = mProgressColor
                mPaint.strokeCap = Paint.Cap.ROUND
                mPaint.style = Paint.Style.STROKE
                mPaint.strokeWidth = 12f
            }
            PaintType.BLN_BG -> {
                mPaint.color = mBlnBgColor
                mPaint.style = Paint.Style.FILL
            }
            PaintType.PROGRESS_PAUSE -> {
                mPaint.color = Color.WHITE
                mPaint.strokeCap = Paint.Cap.BUTT
                mPaint.style = Paint.Style.STROKE
            }
        }
        return mPaint
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //icon呼吸和背景
        drawIconAnim(canvas)

        //外圆圈
        drawProgress(canvas)

        //icon
        drawIcon(canvas)
    }

    private fun drawIconAnim(canvas: Canvas) {
        if (mStatus == Status.PAUSE || mStatus == Status.RECORDING) {
            canvas.drawCircle(
                mCenterX,
                mCenterX,
                measuredWidth / 2F,
                getPaint(PaintType.BLN_BG)
            )
        }
        canvas.drawCircle(mCenterX, mCenterY, mBlnRadius, getPaint(PaintType.BLN_ANIM))
    }

    private fun drawProgress(canvas: Canvas) {
        when (mStatus) {
            Status.IDLE -> canvas.drawCircle(
                mCenterX,
                mCenterY,
                mStartBitmap.width / 2f + mPaint.strokeWidth + 9,
                getPaint(PaintType.PROGRESS)
            )
            else -> {
                canvas.drawArc(
                    mRectF,
                    275f,
                    360f * mRecordSecond / mTotalSecond,
                    false,
                    getPaint(PaintType.PROGRESS)
                )
                var sumTime = 0f
                var starAngle: Float
                mRecordTimeList.forEach {
                    sumTime += it
                    starAngle = 275f + 360f * sumTime / mTotalSecond

                    canvas.drawArc(
                        mRectF,
                        starAngle,
                        mPauseArcDegree,
                        false,
                        getPaint(PaintType.PROGRESS_PAUSE)
                        //paint
                    )
                }
            }
        }

    }

    private fun drawIcon(canvas: Canvas) {
        fun drawSrc(canvas: Canvas, bitmap: Bitmap) {
            val left = mRectF.left + (mRectF.right - mRectF.left - bitmap.width) / 2.0f
            val top = mRectF.top + (mRectF.bottom - mRectF.top - bitmap.height) / 2.0f
            canvas.drawBitmap(bitmap, left, top, getPaint(PaintType.PROGRESS))
        }
        when (mStatus) {
            Status.RECORDING -> drawSrc(canvas, mPauseBitmap)
            else -> drawSrc(canvas, mStartBitmap)
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val viewWide = measuredWidth - paddingLeft - paddingRight
        val viewHigh = measuredHeight - paddingTop - paddingBottom
        val mRectLength = mPaint.strokeWidth.toInt() / 2

        mRectF = RectF(
            (paddingLeft + mRectLength).toFloat(),
            (paddingTop + mRectLength).toFloat(),
            (paddingLeft + viewWide - mRectLength).toFloat(),
            (paddingTop + viewHigh - mRectLength).toFloat()
        )

        mPauseArcDegree = (3 * (360f / ((mRectF.right - mRectF.left) / 2 * PI))).toFloat()

        mCenterX = measuredWidth / 2F
        mCenterY = measuredHeight / 2F

    }

    fun start() {
        mStatus = Status.RECORDING
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
                Log.e("wqs", "sum:${mRecordTimeList.sum()}")
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
            mRecordTimeList.add(currentPlayTime / 1000f)
            Log.e("wqs", "item:${(currentPlayTime / 1000f).toInt()}")
            mBlnAnimator = null
            mBlnRadius = mStartBitmap.width / 2f
            invalidate()
        }
    }

}