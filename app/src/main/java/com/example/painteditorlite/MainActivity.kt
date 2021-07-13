package com.example.painteditorlite

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var paintColor: Colors = Colors.RED
    private lateinit var btnCurveLine:Button
    private lateinit var btnRectangle: Button
    private lateinit var btnLines:Button
    private lateinit var btnScale:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()

    }

    private fun initViews() {
        val drawView: EditorView = findViewById(R.id.iv_editor)
        val btnRed: Button = findViewById(R.id.btn_red)
        val btnYellow: Button = findViewById(R.id.btn_yellow)
        val btnBlue: Button = findViewById(R.id.btn_blue)

        btnCurveLine = findViewById<Button>(R.id.btn_curve).apply{
            setOnClickListener{
                drawView.onModeCurves()

            }
        }
        btnRectangle = findViewById<Button>(R.id.btn_rectangle).apply {
            setOnClickListener {
                drawView.onModeRect()

            }
        }
        btnLines= findViewById<Button>(R.id.btn_line).apply {
            setOnClickListener {
                drawView.onModeLines()

            }
        }

        btnScale = findViewById<Button>(R.id.btn_scale).apply {
            setOnClickListener {
                drawView.onModeScale()
            }
        }



        btnRed.setOnClickListener {
            paintColor = Colors.RED
            drawView.setColor(paintColor)
        }
        btnBlue.setOnClickListener {
            paintColor = Colors.BLUE
            drawView.setColor(paintColor)

        }
        btnYellow.setOnClickListener {
            paintColor = Colors.YELLOW
            drawView.setColor(paintColor)

        }


    }
}