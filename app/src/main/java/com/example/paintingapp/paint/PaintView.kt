package com.example.paintingapp.paint

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

class PaintView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var drawPath: Path = Path()
    private var drawPaint: Paint = Paint()
    private var canvasPaint: Paint = Paint(Paint.DITHER_FLAG)
    private lateinit var drawCanvas: Canvas
    private lateinit var canvasBitmap: Bitmap

    private var currentColor = Color.BLACK
    private var brushSize = 15f
    private var isEraser = false
    private var brushSelected = false

    // Brush paths
    private val paths = ArrayList<CustomPath>()
    private val undoPaths = ArrayList<CustomPath>()

    // Shapes & Stickers
    private val drawableItems = ArrayList<DrawableItem>()
    private val undoneDrawables = ArrayList<DrawableItem>()

    private var selectedItem: DrawableItem? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private val DEFAULT_DRAWABLE_SIZE = 200f

    // ================================
    // UPDATED: BLACK thin dotted border
    // ================================
    private val selectionPaint = Paint().apply {
        color = Color.BLACK       // BLUE → BLACK
        style = Paint.Style.STROKE
        strokeWidth = 3f          // thinner line
        pathEffect = DashPathEffect(floatArrayOf(12f, 12f), 0f) // dotted
    }

    // Scale detector
    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                selectedItem?.let {
                    it.scale *= detector.scaleFactor
                    it.scale = it.scale.coerceIn(0.3f, 10f)
                    invalidate()
                }
                return true
            }
        })

    init { setupPaint() }

    private fun setupPaint() {
        drawPaint.color = currentColor
        drawPaint.isAntiAlias = true
        drawPaint.strokeWidth = brushSize
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
        drawPaint.xfermode =
            if (isEraser) PorterDuffXfermode(PorterDuff.Mode.CLEAR) else null
    }

    // Brush & Eraser
    fun setBrushColor(color: Int) { currentColor = color; isEraser = false; brushSelected = true; setupPaint() }
    fun setBrushSize(size: Float) { brushSize = size.coerceIn(5f, 80f); setupPaint() }
    fun enableEraser() { isEraser = true; brushSelected = true; setupPaint() }
    fun disableEraser() { isEraser = false; setupPaint() }
    fun selectBrush(selected: Boolean) { brushSelected = selected }
    fun isBrushSelected(): Boolean = brushSelected

    // Canvas controls
    fun clearCanvas() {
        paths.clear(); undoPaths.clear()
        drawableItems.clear(); undoneDrawables.clear()
        canvasBitmap.eraseColor(Color.TRANSPARENT)
        invalidate()
    }

    // Undo / Redo
    fun undo() {
        when {
            paths.isNotEmpty() -> undoPaths.add(paths.removeAt(paths.lastIndex))
            drawableItems.isNotEmpty() -> undoneDrawables.add(drawableItems.removeAt(drawableItems.lastIndex))
        }
        redrawCanvas()
    }

    fun redo() {
        when {
            undoPaths.isNotEmpty() -> paths.add(undoPaths.removeAt(undoPaths.lastIndex))
            undoneDrawables.isNotEmpty() -> drawableItems.add(undoneDrawables.removeAt(undoneDrawables.lastIndex))
        }
        redrawCanvas()
    }

    private fun redrawCanvas() {
        canvasBitmap.eraseColor(Color.TRANSPARENT)
        for (p in paths) drawCanvas.drawPath(p.path, p.paint)
        invalidate()
    }

    // Canvas setup
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!::canvasBitmap.isInitialized) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawCanvas = Canvas(canvasBitmap)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw previous paths
        canvas.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)

        // Draw shapes & stickers
        for (item in drawableItems) {
            val save = canvas.save()
            canvas.translate(item.x, item.y)
            canvas.scale(
                item.scale, item.scale,
                item.bitmap.width / 2f,
                item.bitmap.height / 2f
            )
            canvas.drawBitmap(item.bitmap, 0f, 0f, null)

            // DRAW selection border
            if (item == selectedItem) {
                val rect = RectF(
                    0f, 0f,
                    item.bitmap.width.toFloat(),
                    item.bitmap.height.toFloat()
                )
                canvas.drawRect(rect, selectionPaint)
            }

            canvas.restoreToCount(save)
        }

        if (!isEraser && brushSelected) {
            canvas.drawPath(drawPath, drawPaint)
        }
    }

    // Touch handling
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!brushSelected && drawableItems.isEmpty()) return false

        scaleDetector.onTouchEvent(event)

        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> handleTouchDown(x, y)
            MotionEvent.ACTION_MOVE -> handleTouchMove(x, y)
            MotionEvent.ACTION_UP -> handleTouchUp()
        }

        invalidate()
        return true
    }

    private fun handleTouchDown(x: Float, y: Float) {

        // ================================
        // UPDATED: Select any touched part
        // ================================
        selectedItem = drawableItems.findLast { item ->
            val width = item.bitmap.width * item.scale
            val height = item.bitmap.height * item.scale
            x >= item.x && x <= item.x + width &&
                    y >= item.y && y <= item.y + height
        }

        selectedItem?.let {
            // STORE offsets for movement
            lastTouchX = x - it.x
            lastTouchY = y - it.y
        } ?: startTouch(x, y)
    }

    private fun handleTouchMove(x: Float, y: Float) {
        selectedItem?.let {
            it.x = x - lastTouchX
            it.y = y - lastTouchY
        } ?: moveTouch(x, y)
    }

    private fun handleTouchUp() {
        // ================================
        // UPDATED: Auto unselect on release
        // ================================
        selectedItem = null
        upTouch()
    }

    // Brush drawing methods
    private fun startTouch(x: Float, y: Float) {
        drawPath = Path()
        drawPath.moveTo(x, y)
        undoPaths.clear()
    }

    private fun moveTouch(x: Float, y: Float) {
        drawPath.lineTo(x, y)
        drawCanvas.drawPath(drawPath, drawPaint)
    }

    private fun upTouch() {
        if (!drawPath.isEmpty) {
            paths.add(CustomPath(Path(drawPath), Paint(drawPaint)))
            drawPath.reset()
        }
    }

    // Add sticker/shape
    fun addDrawable(bitmap: Bitmap, startX: Float = 100f, startY: Float = 100f) {
        val maxDim = bitmap.width.coerceAtLeast(bitmap.height).toFloat()
        val scale = DEFAULT_DRAWABLE_SIZE / maxDim

        drawableItems.add(DrawableItem(bitmap, startX, startY, scale))
        undoneDrawables.clear()
        invalidate()
    }

    // Data classes
    data class CustomPath(val path: Path, val paint: Paint)

    data class DrawableItem(
        val bitmap: Bitmap,
        var x: Float,
        var y: Float,
        var scale: Float = 1f
    )
}
