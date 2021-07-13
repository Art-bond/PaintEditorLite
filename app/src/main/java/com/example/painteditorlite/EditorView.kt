package com.example.painteditorlite

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import com.example.painteditorlite.Mode.*
import kotlin.math.abs


private const val STROKE_WIDTH = 12f

class EditorView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mode = CURVE

    //begin parameters
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    private var currentX = 0f
    private var currentY = 0f


    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private var drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)

    private val paintCurveLine = Paint().apply {
        color = drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }


    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private var currentPath = Path()
    private val paths = SparseArray<Path>(5)

    private val rectangles: MutableList<RectData> = mutableListOf()
    private var currentRect: RectData? = null

    private val lines: MutableList<LineData> = mutableListOf()
    private var currentLine: LineData? = null

/*    private var actionIndex: Int = 0
    private var pointerId: Int = 0*/

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y


        if (mode != CURVE) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> touchStart(event)
                MotionEvent.ACTION_MOVE -> touchMove(event)
                MotionEvent.ACTION_UP -> touchUp()
            }
        }
        if (mode == CURVE) {
            multiTouchEvent(event)
        }
        return true
    }

    private fun multiTouchEvent(event: MotionEvent) {

        val actionIndex = event.actionIndex
        var pointerId = event.getPointerId(actionIndex)
        //Log.i("XX", "pointer id == $pointerId")

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                currentPath = Path()
                motionTouchEventX = event.getX(actionIndex)
                motionTouchEventY = event.getY(actionIndex)
                currentPath.moveTo(motionTouchEventX, motionTouchEventY)
                paths.put(pointerId, currentPath)
                Log.i("XX", "paths size == " + paths.size())
            }
            MotionEvent.ACTION_MOVE -> {
                for (index in 0 until event.pointerCount) {
                    pointerId = event.getPointerId(index)
                    val pointerPath = paths[pointerId]
                    pointerPath.lineTo(event.getX(index), event.getY(index))
                }
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                //currentPath.reset()
                //invalidate()
            }
        }
    }


    private fun touchStart(event: MotionEvent) {
        when (mode) {
            RECT -> touchStartRect()
            LINE -> touchStartLines()
            else -> Log.i("touchStart", "unknown")
        }
    }

    private fun touchMove(event: MotionEvent) {
        when (mode) {
            RECT -> touchMoveRect()
            LINE -> touchMoveLines()
            else -> Log.i("touchMove", "unknown")

        }
        invalidate()
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        currentRect = null
        currentLine = null
    }


    private fun touchStartRect() {
        currentRect = RectData(
            PointF(motionTouchEventX, motionTouchEventY),
            PointF(motionTouchEventX, motionTouchEventY),
            drawColor
        )
        if (currentRect != null) {
            rectangles.add(currentRect!!)

        }
    }

    private fun touchStartLines() {
        currentLine = LineData(
            PointF(motionTouchEventX, motionTouchEventY),
            PointF(motionTouchEventX, motionTouchEventY),
            drawColor
        )
        if (currentLine != null) {
            lines.add(currentLine!!)

        }
    }


    private fun touchMoveCurves() {
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            currentPath.quadTo(
                currentX,
                currentY,
                (motionTouchEventX + currentX) / 2,
                (motionTouchEventY + currentY) / 2
            )
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to cache it.
            paintCurveLine.color = drawColor
            extraCanvas.drawPath(currentPath, paintCurveLine)
        }
    }

    private fun touchMoveRect() {
        currentRect?.current = PointF(motionTouchEventX, motionTouchEventY)
    }

    private fun touchMoveLines() {
        currentLine?.current = PointF(motionTouchEventX, motionTouchEventY)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //canvas?.drawBitmap(extraBitmap, 0f, 0f, null)
        drawRect(canvas)
        drawLines(canvas)
        drawMultiLines(canvas)
    }

    private fun drawMultiLines(canvas: Canvas?) {
        for (i in 0 until paths.size()) {
            paintCurveLine.color = drawColor
            Log.i("path $i", "path drew ${paths.valueAt(i)} }")
            canvas?.drawPath(paths.valueAt(i), paintCurveLine)
        }
    }

    private fun drawLines(canvas: Canvas?) {
        if (!lines.isNullOrEmpty()) {
            for (line in lines) {
                val paintLine = paintCurveLine
                paintLine.color = line.color
                canvas?.drawLine(
                    line.origin.x,
                    line.origin.y,
                    line.current.x,
                    line.current.y,
                    paintLine
                )
            }
        }
    }

    private fun drawRect(canvas: Canvas?) {
        if (!rectangles.isNullOrEmpty()) {
            for (rect in rectangles) {
                val left: Float = rect.origin.x.coerceAtMost(rect.current.x)
                val right: Float = rect.origin.x.coerceAtLeast(rect.current.x)
                val top: Float = rect.origin.y.coerceAtMost(rect.current.y)
                val bottom: Float = rect.origin.y.coerceAtLeast(rect.current.y)
                val paintRect = paintCurveLine
                paintRect.color = rect.color
                canvas?.drawRect(left, top, right, bottom, paintRect)
            }
        }
    }

    fun onModeRect() {
        mode = RECT
    }

    fun onModeCurves() {
        mode = CURVE
    }

    fun onModeLines() {
        mode = LINE
    }

    fun setColor(color: Colors) {
        drawColor = when (color) {
            Colors.RED -> ResourcesCompat.getColor(resources, R.color.colorRed, null)
            Colors.BLUE -> ResourcesCompat.getColor(resources, R.color.colorBlue, null)
            Colors.YELLOW -> ResourcesCompat.getColor(resources, R.color.colorYellow, null)
        }


        Log.i("color draw", this.drawColor.toString())

    }


}