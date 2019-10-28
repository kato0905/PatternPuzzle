package com.kato0905.patternpuzzle

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.itsxtt.patternlock.PatternLockView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
//import kotlin.coroutines.experimental.suspendCoroutine




class PuzzleMain : AppCompatActivity() {
    var difficulty = 3
    var score = 0
    var latency = 0
    var score_rate = 1


/*    suspend fun View.startAnimationAsync(anim: Animation) {

        return suspendCoroutine { continuation ->
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) { }

                override fun onAnimationEnd(animation: Animation?) {
                    continuation.resume(Unit)
                }

                override fun onAnimationRepeat(animation: Animation?) { }
            })

            this.startAnimation(anim)
        }
    }*/

    class Query(){
        var content : ArrayList<Int> = arrayListOf(0, 1)
        val row_num = 3
        val column_num = 3

        //3x3
        var error_array = arrayOf(
                arrayOf(2, 6, 8),
                arrayOf(7),
                arrayOf(0, 6, 8),
                arrayOf(5),
                arrayOf(-1),
                arrayOf(3),
                arrayOf(0, 2, 8),
                arrayOf(1),
                arrayOf(0, 2, 6))

        init{
            generate_random_content(3)
        }

        fun isMatch(ids: ArrayList<Int>): Boolean{
            if(ids.size <= 1){
                return false
            }

            var entered_value = ids_conversion(ids)
            var content_d = Array(content.size, {i -> i})
            var i = 0
            var horizontal_slide = 0

            val ids_str = entered_value.joinToString(separator = "")
            var content_str = content.joinToString(separator = "")

            for(column in 0..column_num-1) {
                for(row in 0..row_num-1) {
                    //contentの値をコピー
                    i = 0
                    content.forEach {
                        content_d[i] = it
                        i++
                    }

                    //上にスライドできる場合
                    if (content_d.all { it >= column * row_num }) {
                        i = 0
                        content_d.forEach {
                            content_d[i] = it - column * row_num
                            i++
                        }
                    }
                    //左にスライドできる場合
                    if(row != 0 && content_d.all{ (it-row+1)%row_num != 0} && horizontal_slide == row-1){
                        i = 0
                        content_d.forEach {
                            content_d[i] = it - row
                            i++
                        }
                        horizontal_slide++
                    }else{
                        horizontal_slide = 0
                    }

                    content_str = content_d.joinToString(separator = "")

                    //判定
                    var exact_edge = 0
                    for(j in 0..content_str.length-2){
                        var result = content_str.substring(j, j+2)
                        if(ids_str.contains(result.reversed()) || ids_str.contains(result)){
                            exact_edge++
                        }
                    }

                    if(exact_edge == content_str.length-1) {
                        return true
                    }

                }
            }
            return false
        }

        fun ids_conversion(ids: ArrayList<Int>): ArrayList<Int>{

            for(i in 0..ids.size-2){
                error_array[ids[i]].forEach{
                    if(ids[i+1] == it){
                        ids.add(i+1, (ids[i]+it)/2)
                    }
                }
            }
            return ids
        }


        fun generate_random_content(content_size: Int){

            var numlist = List(row_num*column_num,{i -> i})
            Collections.shuffle(numlist)
            var canMove = false

            if(content_size < 2){
                Log.d("system_output", "Error in PuzzleMain generate_random_content 1")
                return
            }else if(content_size > row_num*column_num){
                Log.d("system_output", "Error in PuzzleMain generate_random_content 2")
                return
            }

            content = arrayListOf(numlist[0])

            for(i in 0..content_size-2){
                content.add(numlist[i+1])
            }

            var content_length = content.size-2
            var i = 0

            while(i <= content_length){
                run loop@ {
                    error_array[content[i]].forEach {
                        if (content[i + 1] == it) {
                            content.add(i + 1, (content[i] + it) / 2)
                            content_length++
                            return@loop
                        }
                    }
                    i++
                }
            }

            //contentはできるだけ右下に配置
            for(i in 0..max(column_num, row_num)-1){
                //下にずらす
                if(content.all{ it <= row_num*(column_num-1)-1 }){
                    for(j in 0..content.size-1){
                        content[j] += row_num
                    }
                    canMove = true
                }
                //右にずらす
                if(content.all{ (it+1)%row_num != 0 }){
                    for(j in 0..content.size-1){
                        content[j] += 1
                    }
                    canMove = true
                }
            }

            //描き方が1パターンでないクエリを増加させたい
            if(canMove == false && (0..100).random() < 70){
                generate_random_content(content_size)
            }


        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle_main)

        var query_num = getResources().getInteger(R.integer.query_num)

        var query = Array(query_num, {Query()})
        var queryView = Array(query_num, {QueryView(this, null)})

        var patternLockView = findViewById<com.itsxtt.patternlock.PatternLockView>(R.id.patternLockView)
        Log.d("system_output", "PuzzleMain done")


        /*
         *  初期クエリ表示
         */

        var i = 1
        query.forEach {

            var hand_query_id = i
            var hand_content : ArrayList<Int> = it.content

            Handler().postDelayed(Runnable {
                queryView[hand_query_id - 1].make(hand_content, hand_query_id - 1)
                val query_view_id = getResources().getIdentifier("queryview" + hand_query_id, "id", "com.kato0905.patternpuzzle")
                findViewById<com.kato0905.patternpuzzle.QueryView>(query_view_id).invalidate()
                Log.d("system_output","HAND is "+hand_content)
            }, i.toLong())


            val query_id = getResources().getIdentifier("query" + i, "id", "com.kato0905.patternpuzzle")
            findViewById<TextView>(query_id).text = "" + it.content

            i++
        }

        findViewById<TextView>(R.id.score).text=""+score



        patternLockView.setOnPatternListener(object : PatternLockView.OnPatternListener {
            override fun onStarted() {
                super.onStarted()
            }

            override fun onProgress(ids: ArrayList<Int>) {
                super.onProgress(ids)
            }

            override fun onComplete(ids: ArrayList<Int>): Boolean {
                latency = 0
                score_rate = 1
                var i = 1
                query.forEach {
                    if(it.isMatch(ids)) {
                        latency += 50
                        it.generate_random_content(difficulty)
                        var hand_id = i
                        var hand_content = it.content
                        Handler().postDelayed(Runnable {
                            queryView[hand_id - 1].make(hand_content, hand_id - 1)
                            queryView[hand_id - 1].next_id(hand_id-1)
                            Log.d("system_output", "next_id is "+hand_id)
                            val query_view_id = getResources().getIdentifier("queryview" + hand_id, "id", "com.kato0905.patternpuzzle")
                            findViewById<com.kato0905.patternpuzzle.QueryView>(query_view_id).invalidate()
                        }, latency.toLong())

                        val query_id = getResources().getIdentifier("query" + i, "id", "com.kato0905.patternpuzzle")
                        findViewById<TextView>(query_id).text = "" + it.content

                        score += 100*score_rate
                        score_rate++

                        findViewById<TextView>(R.id.score).text=""+score
                    }

                    i++
                }

                //Toast.makeText(applicationContext, ""+query[0].content, Toast.LENGTH_LONG).show()
                return false
            }
        })



    }

}


