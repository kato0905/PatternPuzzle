package com.kato0905.patternpuzzle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View


var query = arrayListOf(arrayListOf(0,5,8), arrayListOf(0,5,8), arrayListOf(0,5,8), arrayListOf(0,5,8), arrayListOf(0,5,8))
var id_num = 0
var first_flag = 1


class QueryView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    internal var paint: Paint
    private val dp: Float

    init {
        paint = Paint()
        dp = resources.displayMetrics.density
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var num = id_num
        Log.d("system_output", ""+id_num+"のクエリに"+query[num]+"を描画")


        for(i in 0..query.size){
            //RoundRect
            paint.color = Color.argb(255, 236, 141, 83)
            canvas.drawRoundRect(0 * dp, 0 * dp, 80 * dp, 80 * dp,10.toFloat(), 10.toFloat(), paint)
        }


        for (i in 0..query[num].size - 2) {
            // Lines
            paint.strokeWidth = 10f
            paint.color = Color.argb(255, 73, 235, 143)
            canvas.drawLine((10 + query[num][i] % 3 * 30) * dp, (10 + query[num][i] / 3 * 30) * dp, (10 + query[num][i + 1] % 3 * 30) * dp, (10 + query[num][i + 1] / 3 * 30) * dp, paint)

        }

        for(i in 0..query[num].size-1){
            // Circle
            paint.color = Color.argb(255, 88, 123, 244)
            paint.strokeWidth = 5f
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL_AND_STROKE
            canvas.drawCircle((10 + query[num][i] % 3 * 30) * dp, (10 + query[num][i] / 3 * 30) * dp, 5 * dp, paint)
        }


        if(first_flag == 1) {
            if (id_num >= query.size - 1) {
                first_flag = 0
                id_num = 0
            } else {
                id_num++
            }
        }

    }

    fun make(content: ArrayList<Int>, num:Int){

        query[num] = content

    }

    fun next_id(num:Int){
        id_num = num

    }

    fun init_flag(){
        first_flag = 1
        id_num = 0
    }

}