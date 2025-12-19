package com.example.paintingapp

import android.content.Intent
import android.graphics.Paint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.paintingapp.paint.PaintView

class PaintActivity : AppCompatActivity() {

    private lateinit var paintView: PaintView
    private var brushSize = 15f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)

        paintView = findViewById(R.id.paintView)
        val frameLayout = findViewById<FrameLayout>(R.id.frameLayout)

        val btnBrush = findViewById<ImageView>(R.id.btnBrush)
        val btnEraser = findViewById<ImageView>(R.id.btnEraser)
        val btnZoomIn = findViewById<ImageView>(R.id.btnZoomIn)
        val btnZoomOut = findViewById<ImageView>(R.id.btnZoomOut)

        // ----- Brush -----
        btnBrush.setOnClickListener {
            paintView.selectBrush(true)
            paintView.disableEraser()
            btnBrush.setBackgroundColor(Color.LTGRAY)
            btnEraser.setBackgroundColor(Color.TRANSPARENT)
            Toast.makeText(this, "Brush mode activated", Toast.LENGTH_SHORT).show()
        }

        // ----- Eraser -----
        btnEraser.setOnClickListener {
            paintView.enableEraser()
            btnEraser.setBackgroundColor(Color.LTGRAY)
            btnBrush.setBackgroundColor(Color.TRANSPARENT)
            Toast.makeText(this, "Eraser mode activated", Toast.LENGTH_SHORT).show()
        }

        // ----- Zoom In / Out -----
        btnZoomIn.setOnClickListener {
            brushSize += 5f
            paintView.setBrushSize(brushSize)
            Toast.makeText(this, "Brush Size: $brushSize", Toast.LENGTH_SHORT).show()
        }

        btnZoomOut.setOnClickListener {
            brushSize -= 5f
            paintView.setBrushSize(brushSize)
            Toast.makeText(this, "Brush Size: $brushSize", Toast.LENGTH_SHORT).show()
        }

        // ----- Undo / Redo / Clear -----
        findViewById<ImageView>(R.id.btnUndo).setOnClickListener { paintView.undo() }
        findViewById<ImageView>(R.id.btnRedo).setOnClickListener { paintView.redo() }
        findViewById<ImageView>(R.id.btnClear).setOnClickListener { paintView.clearCanvas() }

        // ----- Save / Share -----
        findViewById<ImageView>(R.id.btnSave).setOnClickListener { saveCanvas() }
        findViewById<ImageView>(R.id.btnShare).setOnClickListener { shareCanvas() }

        // ----- Colors -----
        val colorGrid = findViewById<GridLayout>(R.id.colorGrid)
        for (i in 0 until colorGrid.childCount) {
            val colorView = colorGrid.getChildAt(i)
            colorView.setOnClickListener {
                val colorDrawable = (it.background as? android.graphics.drawable.ColorDrawable)
                colorDrawable?.let { paintView.setBrushColor(it.color) }
                btnBrush.setBackgroundColor(Color.LTGRAY)
                btnEraser.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        // ----- Stickers & Shapes -----
        val stickersGrid = findViewById<GridLayout>(R.id.stickersGrid)
        val shapesGrid = findViewById<GridLayout>(R.id.shapesGrid)

        fun setupMovable(grid: GridLayout, outlinedShapes: Boolean = false) {
            for (i in 0 until grid.childCount) {
                val child = grid.getChildAt(i) as ImageView
                child.setOnClickListener {
                    val drawable = child.drawable
                    val bitmap = drawableToBitmap(drawable, outlinedShapes)
                    paintView.addDrawable(bitmap, startX = 100f, startY = 100f)
                }
            }
        }

        setupMovable(stickersGrid, false) // Stickers filled
        setupMovable(shapesGrid, true)    // Shapes outlined only
    }

    // Convert Drawable to Bitmap; outlined shapes are not filled
    private fun drawableToBitmap(drawable: Drawable, outlined: Boolean = false): Bitmap {
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 200
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        if (outlined) {
            // Clear fill to make it outlined only
            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.color = Color.BLACK
            paint.strokeWidth = 4f
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
        }

        return bitmap
    }

    private fun saveCanvas() {
        val frame = findViewById<FrameLayout>(R.id.frameLayout)
        val bitmap = Bitmap.createBitmap(frame.width, frame.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        frame.draw(canvas)
        val savedURL = android.provider.MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Drawing", "Paint App Drawing")
        if (savedURL != null) Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show()
        else Toast.makeText(this, "Error Saving", Toast.LENGTH_SHORT).show()
    }

    private fun shareCanvas() {
        val frame = findViewById<FrameLayout>(R.id.frameLayout)
        val bitmap = Bitmap.createBitmap(frame.width, frame.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        frame.draw(canvas)
        val uri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Drawing", null))
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
}
