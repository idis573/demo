package com.example.demo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
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
        const val ANIM_DOT_DURATION = 900L
    }

    enum class Status {
        IDLE,
        RECORDING,
        PAUSE,
        COMPLETE
    }

    enum class PaintType {
        PROGRESS,
        PROGRESS_PAUSE,
        BLN_ANIM,
        BLN_BG,
        RECORD_TIME,
        RECORD_DOT
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

    //圆圈中心点
    private var mCircleCenterX = 0f
    private var mCircleCenterY = 0f

    //icon呼吸圈半径
    private var mBlnMaxRadius = 0f

    //暂停弧长的角度
    private var mPauseArcDegree = 0f

    // 呼吸圈画笔
    private var mBlnRadius = 0f
    private var scaleAnimator: ValueAnimator
    private var alphaAnimator: ValueAnimator

    private var mProgressColor = Color.RED
    private var mBlnBgColor = Color.WHITE

    private var mRecordTimeMarginBottom = 0f
    private var mRecordTimeHeight = 0f
    private var mRecordDotRadius = 0f
    private var mRecordDotMarginRight = 0f
    private var mRecordDotAlpha = 0

    var recordListener: RecordListener? = null


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BbjRecordButton)

        mPauseBitmap = BitmapFactory.decodeResource(
                resources,
                typedArray.getResourceId(R.styleable.BbjRecordButton_srcPause, R.drawable.pause)
        )
        mStartBitmap = BitmapFactory.decodeResource(
                resources,
                typedArray.getResourceId(R.styleable.BbjRecordButton_srcStart, R.drawable.shoot)
        )

        mRecordTimeHeight = typedArray.getDimension(R.styleable.BbjRecordButton_recordTimeHeight, 60f)
        mRecordTimeMarginBottom = typedArray.getDimension(R.styleable.BbjRecordButton_recordTimeMarginBottom, 12f)
        mRecordDotRadius = typedArray.getDimension(R.styleable.BbjRecordButton_recordDotRadius, 12f)
        mRecordDotMarginRight = typedArray.getDimension(R.styleable.BbjRecordButton_recordDotMarginRight, 6f)
        mPaint.strokeWidth =
                typedArray.getDimension(R.styleable.BbjRecordButton_progressWidth, 10f)
        mPaint.isAntiAlias = true
        mPaint.isDither = true

        mProgressColor = typedArray.getColor(R.styleable.BbjRecordButton_progressColor, Color.RED)

        mBlnBgColor =
                typedArray.getColor(R.styleable.BbjRecordButton_blnBackgroundColor, Color.WHITE)


        // 初始化进度
        mTotalSecond = typedArray.getFloat(R.styleable.BbjRecordButton_totalSecond, 0f)

        mBlnMaxRadius = typedArray.getDimension(R.styleable.BbjRecordButton_blnMaxRadius, 228f)

        typedArray.recycle()

        scaleAnimator = ValueAnimator().apply {
            setFloatValues(mStartBitmap.width / 2f, mBlnMaxRadius)
            addUpdateListener { animation ->
                mBlnRadius = animation.animatedValue as Float
                mRecordSecond = mRecordTimeList.sum() + (animation.currentPlayTime / 1000f)
                if (mRecordSecond >= mTotalSecond) {
                    recordListener?.onComplete()
                    releaseAnim()
                }
                invalidate()
            }
            duration = ANIM_BREATHING_LIGHT_DURATION
            repeatMode = ValueAnimator.REVERSE
            repeatCount = -1
        }

        alphaAnimator = ValueAnimator().apply {
            setIntValues(255, 0)
            addUpdateListener { animation ->
                mRecordDotAlpha = animation.animatedValue as Int
                invalidate()
            }
            duration = ANIM_DOT_DURATION
            repeatMode = ValueAnimator.REVERSE
            repeatCount = -1
        }

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
            PaintType.RECORD_TIME -> {
                mPaint.color = Color.WHITE
                //todo
                mPaint.textSize = 42f
                mPaint.strokeCap = Paint.Cap.BUTT
                mPaint.style = Paint.Style.FILL
            }
            PaintType.RECORD_DOT -> {
                mPaint.color = mProgressColor
                mPaint.strokeCap = Paint.Cap.BUTT
                mPaint.style = Paint.Style.FILL
                mPaint.alpha = mRecordDotAlpha
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

        //时间
        drawRecordTime(canvas)
    }

    private fun drawRecordTime(canvas: Canvas) {
        val text = "%02d:%02d".format((mRecordSecond / 60).toInt(), (mRecordSecond % 60).toInt())
        val textPaint = getPaint(PaintType.RECORD_TIME)
        val fontMetrics = textPaint.fontMetrics
        val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        val baseline = mRecordTimeHeight / 2 + distance
        val txtWidth = textPaint.measureText(text)
        canvas.drawText(text, (measuredWidth - txtWidth) / 2, baseline, textPaint)

        val dotX = (measuredWidth - txtWidth) / 2 - mRecordDotRadius - mRecordDotMarginRight
        val dotY = mRecordTimeHeight / 2
        canvas.drawCircle(dotX, dotY, mRecordDotRadius, getPaint(PaintType.RECORD_DOT))
    }

    private fun drawIconAnim(canvas: Canvas) {
        if (mStatus == Status.PAUSE || mStatus == Status.RECORDING) {
            canvas.drawCircle(
                    mCircleCenterX,
                    mCircleCenterY,
                    measuredWidth / 2f,
                    getPaint(PaintType.BLN_BG)
            )
        }
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, mBlnRadius, getPaint(PaintType.BLN_ANIM))
    }

    private fun drawProgress(canvas: Canvas) {
        when (mStatus) {
            Status.IDLE -> canvas.drawCircle(
                    mCircleCenterX,
                    mCircleCenterY,
                    mStartBitmap.width / 2f + mPaint.strokeWidth + 9,
                    getPaint(PaintType.PROGRESS)
            )
            else -> {
                canvas.drawArc(
                        mRectF,
                        270f,
                        360f * mRecordSecond / mTotalSecond,
                        false,
                        getPaint(PaintType.PROGRESS)
                )
                var sumTime = 0f
                var starAngle: Float
                mRecordTimeList.forEach {
                    sumTime += it
                    starAngle = 270f + 360f * sumTime / mTotalSecond

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
                (paddingTop + mRectLength).toFloat() + mRecordTimeHeight + mRecordTimeMarginBottom,
                (paddingLeft + viewWide - mRectLength).toFloat(),
                (paddingTop + viewHigh - mRectLength).toFloat()
        )

        mPauseArcDegree = (1 * (360f / ((mRectF.right - mRectF.left) / 2 * PI))).toFloat()

        mCircleCenterX = measuredWidth / 2F
        mCircleCenterY = (measuredHeight + mRecordTimeHeight + mRecordTimeMarginBottom) / 2F

    }

    fun startRecord() {
        mStatus = Status.RECORDING
        if (scaleAnimator.isRunning || alphaAnimator.isRunning) {
            return
        }
        recordListener?.onStart()
        scaleAnimator.start()
        alphaAnimator.start()
    }

    fun pauseRecord() {
        //至少大于1s才能暂停
        if (scaleAnimator.currentPlayTime < 1000L) {
            return
        }
        recordListener?.onPause()
        mStatus = Status.PAUSE
        mRecordTimeList.add(scaleAnimator.currentPlayTime / 1000f)
        releaseAnim()
    }

    private fun releaseAnim() {
        mRecordDotAlpha = 255
        mBlnRadius = mStartBitmap.width / 2f
        alphaAnimator.pause()
        alphaAnimator.cancel()
        scaleAnimator.pause()
        scaleAnimator.cancel()
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pauseRecord()
    }

    interface RecordListener {
        fun onStart()
        fun onPause()
        fun onComplete()
    }

}