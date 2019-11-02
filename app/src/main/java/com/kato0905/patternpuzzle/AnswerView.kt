package com.kato0905.patternpuzzle


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View


var answer_ids = arrayListOf(1,2)


class AnswerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    internal var paint: Paint
    private val dp: Float

    init {
        paint = Paint()
        dp = resources.displayMetrics.density
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val answer_width = width / dp
        val answer_height = width / dp

        for (i in 0..query.size) {
            //RoundRect
            paint.color = Color.argb(255, 236, 141, 83)
            canvas.drawRoundRect(0 * dp, 0 * dp, answer_width * dp, answer_height * dp, 10.toFloat(), 10.toFloat(), paint)
        }


        for (i in 0..answer_ids.size - 2) {
            // Lines
            paint.strokeWidth = 15f
            paint.color = Color.argb(255, 73, 235, 143)
            canvas.drawLine((answer_width / 8 + answer_ids[i] % 3 * answer_width * 3 / 8) * dp, (answer_height / 8 + answer_ids[i] / 3 * answer_height * 3 / 8) * dp, (answer_width / 8 + answer_ids[i + 1] % 3 * answer_width * 3 / 8) * dp, (answer_height/8 + answer_ids[i + 1] / 3 * answer_height * 3 / 8) * dp, paint)

        }

        for (i in 0..answer_ids.size - 1) {
            // Circle
            if(answer_ids[i] == answer_ids[0] || answer_ids[i] == answer_ids[answer_ids.size - 1]) {
                paint.color = Color.argb(255, 88, 255, 244)
            }else {
                paint.color = Color.argb(255, 88, 123, 244)
            }
            paint.strokeWidth = 5f
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL_AND_STROKE
            canvas.drawCircle((answer_width / 8 + answer_ids[i] % 3 * answer_width * 3 / 8) * dp, (answer_height / 8 + answer_ids[i] / 3 * answer_height * 3 / 8) * dp, answer_width / 16 * dp, paint)
        }


    }

    fun make(content: ArrayList<Int>) {

        answer_ids = content

    }
}