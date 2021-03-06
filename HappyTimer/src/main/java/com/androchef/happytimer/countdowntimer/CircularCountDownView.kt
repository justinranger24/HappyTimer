package com.androchef.happytimer.countdowntimer

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.androchef.happytimer.R
import com.androchef.happytimer.utils.DateTimeUtils
import kotlinx.android.synthetic.main.layout_circular_count_down_timer.view.*

class CircularCountDownView(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {

    var strokeThicknessForeground: Float = 3f.dpToPx()
        set(value) {
            /**
             * Foreground thickness can't be lesser than background thickness.
             */
            if (value >= strokeThicknessBackground) {
                field = value.dpToPx()
            }
            circleProgressBar.setStrokeWidth(strokeThicknessForeground, strokeThicknessBackground)
            circleProgressBar.invalidate()
            invalidate()
        }
        get() {
            return field.pxToDp()
        }

    var strokeThicknessBackground: Float = 3f.dpToPx()
        set(value) {
            /**
             * Background thickness can't be grater than foreground thickness.
             */
            if (value <= strokeThicknessForeground) {
                field = value.dpToPx()
            }
            circleProgressBar.setStrokeWidth(strokeThicknessForeground, strokeThicknessBackground)
            circleProgressBar.invalidate()
            invalidate()
        }
        get() {
            return field.pxToDp()
        }

    var strokeColorForeground: Int = Color.GRAY
        set(value) {
            field = value
            circleProgressBar.setColor(field, strokeColorBackground)
            circleProgressBar.invalidate()
            invalidate()
        }

    var strokeColorBackground: Int = strokeColorForeground
        set(value) {
            field = value
            circleProgressBar.setColor(strokeColorForeground, field)
            circleProgressBar.invalidate()
            invalidate()
        }

    var timerTextColor: Int = Color.BLACK
        set(value) {
            field = value
            tvTimerText.setTextColor(value)
            circleProgressBar.invalidate()
            invalidate()
        }

    var timerTextSize: Float = 10f.spToPx()
        set(value) {
            field = value
            tvTimerText.textSize = field
            circleProgressBar.invalidate()
            invalidate()
        }

    var timerTextIsBold: Boolean = resources.getBoolean(R.bool.default_timer_text_is_bold)
        set(value) {
            field = value
            if (value)
                tvTimerText.setTypeface(null, Typeface.BOLD)
            else
                tvTimerText.setTypeface(null, Typeface.NORMAL)
            circleProgressBar.invalidate()
            invalidate()
        }

    var timerTextFormat: TextFormat = TextFormat.MINUTE_SECOND
        set(value) {
            field = value
            circleProgressBar.invalidate()
            invalidate()
        }

    var timerType: HappyTimer.Type = HappyTimer.Type.COUNT_DOWN
        set(value) {
            field = value
            circleProgressBar.invalidate()
            invalidate()
        }

    var isTimerTextShown: Boolean = true
        set(value) {
            field = value
            tvTimerText.isToShow(field)
            invalidate()
        }

    private var timerTotalSeconds: Int = resources.getInteger(R.integer.default_timer_total_seconds)

    private var happyTimer: HappyTimer? = HappyTimer(timerTotalSeconds)

    private var onTickListener: HappyTimer.OnTickListener? = null

    private var onStateChangeListener: HappyTimer.OnStateChangeListener? = null

    fun getTotalSeconds() = timerTotalSeconds

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_circular_count_down_timer, this)
        val typedArray = context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.CircularCountDownView,
            0, 0
        )
        //Reading values from the XML layout
        try {
            strokeColorForeground = typedArray.getColor(
                R.styleable.CircularCountDownView_stroke_foreground_color,
                strokeColorForeground
            )

            strokeColorBackground = typedArray.getColor(
                R.styleable.CircularCountDownView_stroke_background_color,
                strokeColorBackground
            )

            strokeThicknessForeground = typedArray.getDimension(
                R.styleable.CircularCountDownView_stroke_foreground_thickness,
                strokeThicknessForeground
            ).pxToDp()

            strokeThicknessBackground = typedArray.getDimension(
                R.styleable.CircularCountDownView_stroke_background_thickness,
                strokeThicknessBackground
            ).pxToDp()

            timerTextColor = typedArray.getColor(
                R.styleable.CircularCountDownView_timer_text_color,
                timerTextColor
            )

            timerTextIsBold = typedArray.getBoolean(
                R.styleable.CircularCountDownView_timer_text_isBold,
                timerTextIsBold
            )

            isTimerTextShown = typedArray.getBoolean(
                R.styleable.CircularCountDownView_timer_text_shown,
                isTimerTextShown
            )

            timerTextSize = typedArray.getDimension(
                R.styleable.CircularCountDownView_timer_text_size,
                timerTextSize
            ).pxToSp()

            timerTotalSeconds =
                typedArray.getInt(
                    R.styleable.CircularCountDownView_timer_total_seconds,
                    timerTotalSeconds
                )
            timerTextFormat = TextFormat.values()[
                    typedArray.getInt(
                        R.styleable.CircularCountDownView_timer_text_format,
                        0
                    )]

            timerType = HappyTimer.Type.values()[
                    typedArray.getInt(
                        R.styleable.CircularCountDownView_timer_type,
                        0
                    )]

        } finally {
            typedArray.recycle()
        }
    }

    fun initTimer(totalTimeInSeconds: Int) {
        this.timerTotalSeconds = totalTimeInSeconds
        stopTimer()
        happyTimer = HappyTimer(totalTimeInSeconds, 3000)
        setOnTickListener()
        onInitTimerState()
    }

    fun startTimer() {
        happyTimer?.start()
    }

    fun stopTimer() {
        happyTimer?.stop()
    }

    fun pauseTimer() {
        happyTimer?.pause()
    }

    fun resumeTimer() {
        happyTimer?.resume()
    }

    fun resetTimer() {
        happyTimer?.resetTimer()
    }

    private fun setOnTickListener() {
        happyTimer?.setOnTickListener(object : HappyTimer.OnTickListener {
            override fun onTick(completedSeconds: Int, remainingSeconds: Int) {
                onTickListener?.onTick(completedSeconds, remainingSeconds)
                setTimerText(completedSeconds, remainingSeconds)
                circleProgressBar.setProgressWithAnimation(remainingSeconds.toFloat())
            }

            override fun onTimeUp() {
                onTickListener?.onTimeUp()
            }
        })

        onStateChangeListener?.let {
            happyTimer?.setOnStateChangeListener(it)
        }
    }

    private fun setTimerText(completedSeconds: Int, remainingSeconds: Int) {
        tvTimerText.text = when (timerType) {
            HappyTimer.Type.COUNT_UP -> getFormattedTime(completedSeconds)
            HappyTimer.Type.COUNT_DOWN -> getFormattedTime(remainingSeconds)
        }
    }

    private fun onInitTimerState() {
        setTimerTextInitial()
        circleProgressBar.setMin(0)
        circleProgressBar.setMax(timerTotalSeconds)
        circleProgressBar.setProgressWithAnimation(timerTotalSeconds.toFloat())
        circleProgressBar.invalidate()
    }

    private fun setTimerTextInitial() {
        tvTimerText.text = when (timerType) {
            HappyTimer.Type.COUNT_UP -> getFormattedTime(0)
            HappyTimer.Type.COUNT_DOWN -> getFormattedTime(timerTotalSeconds)
        }
    }

    private fun getFormattedTime(seconds: Int): String {
        return when (timerTextFormat) {
            TextFormat.HOUR_MINUTE_SECOND -> DateTimeUtils.getHourMinutesSecondsFormat(seconds)
            TextFormat.MINUTE_SECOND -> DateTimeUtils.getMinutesSecondsFormat(seconds)
            TextFormat.SECOND -> seconds.toString()
        }
    }

    enum class TextFormat {
        HOUR_MINUTE_SECOND, MINUTE_SECOND, SECOND
    }


    fun setStateChangeListener(stateChangeListener: HappyTimer.OnStateChangeListener) {
        this.onStateChangeListener = stateChangeListener
    }

    fun setOnTickListener(onTickListener: HappyTimer.OnTickListener) {
        this.onTickListener = onTickListener
    }

    //region Extensions Utils
    private fun Float.dpToPx(): Float =
        this * Resources.getSystem().displayMetrics.density

    private fun Float.pxToDp(): Float =
        this / Resources.getSystem().displayMetrics.density

    //region Extensions Utils
    private fun Float.spToPx(): Float =
        this * Resources.getSystem().displayMetrics.scaledDensity

    private fun Float.pxToSp(): Float =
        this / Resources.getSystem().displayMetrics.scaledDensity

    private fun View.isToShow(boolean: Boolean) {
        if (boolean)
            this.visible()
        else
            this.gone()
    }

    private fun View.visible() {
        this.visibility = View.VISIBLE
    }

    private fun View.gone() {
        this.visibility = View.GONE
    }

    private fun View.invisible() {
        this.visibility = View.VISIBLE
    }

}