package com.xbot.clockview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.withRotation
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import java.time.DateTimeException
import java.time.ZoneId

class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var clockFace: Drawable? = null
    private var clockFaceTintInfo: TintInfo = TintInfo()

    private var hourHand: Drawable? = null
    private var hourHandTintInfo: TintInfo = TintInfo()

    private var minuteHand: Drawable? = null
    private var minuteHandTintInfo: TintInfo = TintInfo()

    private var secondHand: Drawable? = null
    private var secondHandTintInfo: TintInfo = TintInfo()

    private var clock: Clock? = null
    private var zoneId: ZoneId = ZoneId.systemDefault()

    private val defaultWidth: Int =
        context.resources.getDimensionPixelSize(R.dimen.default_clock_view_width)
    private val defaultHeight: Int =
        context.resources.getDimensionPixelSize(R.dimen.default_clock_view_height)

    private val hourRotationDegrees: Float
        get() = ((requireNotNull(clock).hours) + (requireNotNull(clock).minutes) / 60.0f) * 360.0f / 12

    private val minuteRotationDegrees: Float
        get() = ((requireNotNull(clock).minutes) + (requireNotNull(clock).seconds) / 60.0f) * 360.0f / 60

    private val secondRotationDegrees: Float
        get() = (requireNotNull(clock).seconds) * 360.0f / 60

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ClockView, defStyleAttr, 0).apply {
            try {
                clockFace = getDrawable(R.styleable.ClockView_clockFace)
                    ?: AppCompatResources.getDrawable(context, R.drawable.clockface)
                clockFaceTintInfo = TintInfo(
                    tintList = getColorStateList(R.styleable.ClockView_clockFaceTint),
                    tintBlendMode = parseBlendMode(getInt(R.styleable.ClockView_clockFaceTintMode, -1))
                )
                clockFace = clockFace.applyTint(clockFaceTintInfo)
                hourHand = getDrawable(R.styleable.ClockView_hourHand)
                    ?: AppCompatResources.getDrawable(context, R.drawable.hourhand)
                hourHandTintInfo = TintInfo(
                    tintList = getColorStateList(R.styleable.ClockView_hourHandTint),
                    tintBlendMode = parseBlendMode(getInt(R.styleable.ClockView_hourHandTintMode, -1))
                )
                hourHand = hourHand.applyTint(hourHandTintInfo)
                minuteHand = getDrawable(R.styleable.ClockView_minuteHand)
                    ?: AppCompatResources.getDrawable(context, R.drawable.minutehand)
                minuteHandTintInfo = TintInfo(
                    tintList = getColorStateList(R.styleable.ClockView_minuteHandTint),
                    tintBlendMode = parseBlendMode(getInt(R.styleable.ClockView_minuteHandTintMode, -1))
                )
                minuteHand = minuteHand.applyTint(minuteHandTintInfo)
                secondHand = getDrawable(R.styleable.ClockView_secondHand)
                    ?: AppCompatResources.getDrawable(context, R.drawable.secondhand)
                secondHandTintInfo = TintInfo(
                    tintList = getColorStateList(R.styleable.ClockView_secondHandTint),
                    tintBlendMode = parseBlendMode(getInt(R.styleable.ClockView_secondHandTintMode, -1))
                )
                secondHand = secondHand.applyTint(secondHandTintInfo)
                zoneId = getString(R.styleable.ClockView_timeZone).toZoneId()
            } finally {
                recycle()
            }
        }
    }

    @Suppress("UNUSED")
    var timeZone: String?
        get() = zoneId.id
        set(value) {
            zoneId = value.toZoneId()
            createClock()
            invalidate()
        }

    @Suppress("UNUSED")
    fun setClockFace(drawable: Drawable?) {
        clockFace = drawable?.applyTint(clockFaceTintInfo)
        invalidate()
    }

    @Suppress("UNUSED")
    var clockFaceTintList: ColorStateList?
        get() = clockFaceTintInfo.tintList
        set(value) {
            clockFaceTintInfo = clockFaceTintInfo.copy(tintList = value)
            clockFace = clockFace?.applyTint(clockFaceTintInfo)
        }

    @Suppress("UNUSED")
    var clockFaceTintBlendMode: BlendMode?
        get() = clockFaceTintInfo.tintBlendMode
        set(value) {
            clockFaceTintInfo = clockFaceTintInfo.copy(tintBlendMode = value)
            clockFace = clockFace?.applyTint(clockFaceTintInfo)
        }

    @Suppress("UNUSED")
    fun setHourHand(drawable: Drawable?) {
        hourHand = drawable.applyTint(hourHandTintInfo)
        invalidate()
    }

    @Suppress("UNUSED")
    var hourHandTintList: ColorStateList?
        get() = hourHandTintInfo.tintList
        set(value) {
            hourHandTintInfo = hourHandTintInfo.copy(tintList = value)
            hourHand = hourHand?.applyTint(hourHandTintInfo)
        }

    @Suppress("UNUSED")
    var hourHandTintBlendMode: BlendMode?
        get() = hourHandTintInfo.tintBlendMode
        set(value) {
            hourHandTintInfo = hourHandTintInfo.copy(tintBlendMode = value)
            hourHand = hourHand?.applyTint(hourHandTintInfo)
        }

    @Suppress("UNUSED")
    fun setMinuteHand(drawable: Drawable?) {
        minuteHand = drawable.applyTint(minuteHandTintInfo)
        invalidate()
    }

    @Suppress("UNUSED")
    var minuteHandTintList: ColorStateList?
        get() = minuteHandTintInfo.tintList
        set(value) {
            minuteHandTintInfo = minuteHandTintInfo.copy(tintList = value)
            minuteHand = minuteHand?.applyTint(minuteHandTintInfo)
        }

    @Suppress("UNUSED")
    var minuteHandTintBlendMode: BlendMode?
        get() = minuteHandTintInfo.tintBlendMode
        set(value) {
            minuteHandTintInfo = minuteHandTintInfo.copy(tintBlendMode = value)
            minuteHand = minuteHand?.applyTint(minuteHandTintInfo)
        }

    @Suppress("UNUSED")
    fun setSecondHand(drawable: Drawable?) {
        secondHand = drawable.applyTint(secondHandTintInfo)
        invalidate()
    }

    @Suppress("UNUSED")
    var secondHandTintList: ColorStateList?
        get() = secondHandTintInfo.tintList
        set(value) {
            secondHandTintInfo = secondHandTintInfo.copy(tintList = value)
            secondHand = secondHand?.applyTint(hourHandTintInfo)
        }

    @Suppress("UNUSED")
    var secondHandTintBlendMode: BlendMode?
        get() = secondHandTintInfo.tintBlendMode
        set(value) {
            secondHandTintInfo = secondHandTintInfo.copy(tintBlendMode = value)
            secondHand = secondHand?.applyTint(secondHandTintInfo)
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredWidth = minimumWidth.takeIf { it != 0 } ?: defaultWidth
        val desiredHeight = minimumHeight.takeIf { it != 0 } ?: defaultHeight

        val width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0)
        val height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        with(canvas) {
            clockFace?.drawFitCenter(this, width, height)
            hourHand?.let {
                withRotation(hourRotationDegrees, centerX, centerY) {
                    it.drawFitCenter(this, width, height)
                }
            }
            minuteHand?.let {
                withRotation(minuteRotationDegrees, centerX, centerY) {
                    it.drawFitCenter(this, width, height)
                }
            }
            secondHand?.let {
                withRotation(secondRotationDegrees, centerX, centerY) {
                    it.drawFitCenter(this, width, height)
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        createClock()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        resetClock()
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).apply {
            this.timeZone = zoneId.id
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            timeZone = state.timeZone
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun resetClock() {
        clock?.stop()
        clock = null
    }

    private fun createClock() {
        resetClock()
        val lifecycleOwner = findViewTreeLifecycleOwner()
        if (lifecycleOwner != null) {
            val scope = lifecycleOwner.lifecycleScope
            clock = Clock(scope, zoneId).apply {
                doOnTimeTick { invalidate() }
                start()
            }
        } else {
            throw IllegalStateException("$TAG must be attached to a LifecycleOwner")
        }
    }

    private fun String?.toZoneId(): ZoneId {
        return if (this == null) {
            ZoneId.systemDefault()
        } else try {
            ZoneId.of(this)
        } catch (e: DateTimeException) {
            Log.w(TAG, "Failed to parse time zone from $this", e)
            ZoneId.systemDefault()
        }
    }

    private data class TintInfo(
        val tintList: ColorStateList? = null,
        val tintBlendMode: BlendMode? = null
    )

    private fun Drawable?.applyTint(tintInfo: TintInfo) = this?.mutate()?.apply {
        tintInfo.tintList?.let { setTintList(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tintInfo.tintBlendMode?.let { setTintBlendMode(it) }
        }
        if (this.isStateful) {
            setState(drawableState)
        }
    }

    private fun parseBlendMode(value: Int): BlendMode? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (value) {
                3 -> BlendMode.SRC_OVER
                5 -> BlendMode.SRC_IN
                9 -> BlendMode.SRC_ATOP
                14 -> BlendMode.MODULATE
                15 -> BlendMode.SCREEN
                16 -> BlendMode.PLUS
                else -> null
            }
        } else null
    }

    private class SavedState : BaseSavedState {
        var timeZone: String? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            timeZone = source.readString()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(timeZone)
        }

        companion object {
            @Suppress("UNUSED")
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    companion object {
        private const val TAG = "ClockView"
    }
}