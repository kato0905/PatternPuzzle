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

        // 背景、半透明
        canvas.drawColor(Color.argb(127, 0, 127, 63))

        var num = id_num
        Log.d("system_output", ""+id_num+"のクエリに"+query[num]+"を描画")


        for (i in 0..query[num].size - 2) {
            // Circle
            paint.color = Color.argb(255, 125, 125, 255)
            paint.strokeWidth = 5f
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE
            canvas.drawCircle(10 + query[num][i] % 3 * 30 * dp, 10 + query[num][i] / 3 * 30 * dp, 5 * dp, paint)

            // Lines
            paint.strokeWidth = 5f
            paint.color = Color.argb(255, 0, 255, 0)
            canvas.drawLine(10 + query[num][i] % 3 * 30 * dp, 10 + query[num][i] / 3 * 30 * dp, 10 + query[num][i + 1] % 3 * 30 * dp, 10 + query[num][i + 1] / 3 * 30 * dp, paint)

        }

        paint.color = Color.argb(255, 125, 125, 255)
        paint.strokeWidth = 5f
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        canvas.drawCircle(10 + query[num][query[num].size-1] % 3 * 30 * dp, 10 + query[num][query[num].size-1] / 3 * 30 * dp, 5 * dp, paint)


        if(id_num >= query.size-1){
            id_num=0
        }else{
            id_num++
        }

    }

    fun make(content: ArrayList<Int>, num:Int){

        query[num] = content

    }

    fun next_id(num:Int){
        id_num = num

    }

}