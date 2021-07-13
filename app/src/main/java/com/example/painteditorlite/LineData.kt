package com.example.painteditorlite

import android.graphics.PointF

data class LineData(
val origin: PointF,
var current: PointF,
var color: Int
)
