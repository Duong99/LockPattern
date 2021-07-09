package vn.com.nghiemduong.lockpattern.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vn.com.nghiemduong.lockpattern.R
import vn.com.nghiemduong.lockpattern.model.Coordinate
import kotlin.math.abs

class LockPatternView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val SUCCESS = 1
        const val FAIL = 2
        const val SET_PASSWORD = 3
        const val CONFIRM_PASSWORD = 4
        const val UNLOCK_PASSWORD = 5
    }

    private val NUMBER_DOT = 9
    private var mStartX = 0f
    private var mStartY = 0f
    private var mPosition = -1

    private var mPointSelected = ""
    private var mPaint: Paint
    private var mPaintBlack: Paint

    private var mBounceDotRadius = 15f
    private var mListCoordinate: MutableList<Coordinate>
    private lateinit var mExtraBitmap: Bitmap
    private lateinit var mExtraCanvas: Canvas
    private var mOnClickPasswordListener: OnClickPasswordListener? = null

    var isClickAction = true

    init {
        setBackgroundResource(R.color.black)

        mPaint = Paint().apply {
            strokeWidth = 10f
            color = resources.getColor(R.color.white, resources.newTheme())
        }

        mPaintBlack = Paint().apply {
            strokeWidth = 10f
            style = Paint.Style.FILL
            color = resources.getColor(R.color.black, resources.newTheme())
        }

        mListCoordinate = mutableListOf()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mExtraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mExtraCanvas = Canvas(mExtraBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mExtraBitmap, 0f, 0f, null)
        createDot(canvas, mPaint)
    }

    fun clear() {
        if (mPointSelected.isNotEmpty()) {
            mPointSelected = ""
            touchMoveBlack()
            for (i in 0 until mListCoordinate.size) {
                mListCoordinate[i].isClicked = false
            }
            invalidate()
        }
    }

    private fun createDot(canvas: Canvas, paint: Paint) {
        mPaint.color = resources.getColor(R.color.white, resources.newTheme())
        val mX = width / 4
        val mY = height / 4
        for (i in 0 until NUMBER_DOT) {
            var x = 0
            var y = 0
            when {
                i < 3 -> {
                    x = mX * (i + 1)
                    y = mY
                }
                i < 6 -> {
                    x = mX * (i - 2)
                    y = mY * 2

                }
                i < NUMBER_DOT -> {
                    x = mX * (i - 5)
                    y = mY * 3
                }
            }

            if (mListCoordinate.size < NUMBER_DOT) {
                mListCoordinate.add(Coordinate(x.toFloat(), y.toFloat()))
            }
            if (i == mPosition) {
                canvas.drawCircle(x.toFloat(), y.toFloat(), mBounceDotRadius * 2, paint)

                GlobalScope.launch {
                    delay(300)
                    mPosition = -1
                }
            } else {
                canvas.drawCircle(x.toFloat(), y.toFloat(), mBounceDotRadius, paint)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isClickAction) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    clear()
                    mPaint.color = resources.getColor(R.color.white, resources.newTheme())

                    val position = checkCoordinate(event.x.toInt(), event.y.toInt())
                    if (position != -1) {
                        touchStart(mListCoordinate[position].x, mListCoordinate[position].y)
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    val position = checkCoordinate(event.x.toInt(), event.y.toInt())
                    if (position != -1) {
                        touchMoveBlack()
                        redrawLinePointSelected(mPaint)
                        touchStart(mListCoordinate[position].x, mListCoordinate[position].y)
                        touchMove(mListCoordinate[position].x, mListCoordinate[position].y)
                    } else {
                        if (mStartX != 0f && mStartY != 0f) {
                            touchMoveBlack()
                            redrawLinePointSelected(mPaint)
                            touchMove(event.x, event.y)
                            invalidate()
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    touchMoveBlack()
                    touchUp()
                    mPosition = -1
                    redrawLinePointSelected(mPaint)
                    if (mPointSelected.length > 1) {
                        mOnClickPasswordListener?.onPassword(mPointSelected)
                    }
                    invalidate()
                }
            }
        }
        return true
    }

    private fun checkCoordinate(xClick: Int, yClick: Int): Int {
        for (i in 0 until mListCoordinate.size) {
            val x = mListCoordinate[i].x
            val y = mListCoordinate[i].y
            if ((abs(xClick - x) <= mBounceDotRadius * 2
                        && abs(yClick - y) <= mBounceDotRadius * 2) && !mListCoordinate[i].isClicked
            ) {
                mListCoordinate[i].isClicked = true
                mPointSelected += i.toString()
                onVibrator()
                mPosition = i
                return i
            }
        }

        return -1
    }

    private fun touchStart(x: Float, y: Float) {
        this.mStartX = x
        this.mStartY = y
    }

    private fun touchMove(x: Float, y: Float) {
        mExtraCanvas.drawLine(mStartX, mStartY, x, y, mPaint)
    }

    private fun touchMoveBlack() {
        mExtraCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mPaintBlack)
    }

    private fun touchUp() {
        this.mStartX = 0f
        this.mStartY = 0f
    }

    private fun redrawLinePointSelected(paint: Paint) {
        if (mPointSelected.isNotEmpty() && mPointSelected.length > 1) {
            for (i in mPointSelected.indices) {
                if (i > 0) {
                    mExtraCanvas.drawLine(
                        mListCoordinate[mPointSelected[i - 1].toString().toInt()].x,
                        mListCoordinate[mPointSelected[i - 1].toString().toInt()].y,
                        mListCoordinate[mPointSelected[i].toString().toInt()].x,
                        mListCoordinate[mPointSelected[i].toString().toInt()].y, paint
                    )
                }
            }
        }
    }

    fun redrawPassword(state: Int) {
        val paint = Paint().apply {
            strokeWidth = 10f
        }
        when (state) {
            SUCCESS -> {
                paint.color = resources.getColor(R.color.green, resources.newTheme())
            }

            FAIL -> {
                paint.color = resources.getColor(R.color.red, resources.newTheme())
            }

            SET_PASSWORD -> {
                paint.color = resources.getColor(R.color.blue, resources.newTheme())
            }
        }

        redrawLinePointSelected(paint)
        invalidate()
    }

    private fun onVibrator() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(150)
        }
    }

    fun setOnClickPasswordListener(onClickPasswordListener: OnClickPasswordListener?) {
        this.mOnClickPasswordListener = onClickPasswordListener
    }

    interface OnClickPasswordListener {
        fun onPassword(password: String)
    }
}