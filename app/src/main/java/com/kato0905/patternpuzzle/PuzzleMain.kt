package com.kato0905.patternpuzzle

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import android.widget.*
import com.itsxtt.patternlock.PatternLockView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.math.max



class PuzzleMain : AppCompatActivity() {
    var difficulty = 3
    var score = 0
    var latency = 0
    var score_rate = 1
    var time = 90

    private var mTimer: Timer? = null
    private val mHandler = Handler()
    var stop_flag = false

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

            var i = 0
            while(ids.size-2-i >= 0){
                error_array[ids[i]].forEach{
                    if(ids[i+1] == it){
                        ids.add(i+1, (ids[i]+it)/2)
                    }
                }
                i++
            }

            return ids
        }


        fun generate_random_content(content_size: Int){

            var numlist = List(row_num*column_num,{i -> i})
            Collections.shuffle(numlist)
            var canMove = 0

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


            //contentはできるだけ右下に配置
            for(i in 0..max(column_num, row_num)-1){
                //下にずらす
                if(content.all{ it <= row_num*(column_num-1)-1 }){
                    for(j in 0..content.size-1){
                        content[j] += row_num
                    }
                    canMove++
                }
                //右にずらす
                if(content.all{ (it+1)%row_num != 0 }){
                    for(j in 0..content.size-1){
                        content[j] += 1
                    }
                    canMove++
                }
            }

            //一周するようなクエリ
            if(canMove >= 2 && (0..100).random() <= 40){
                content.add(content[0])
            }

            //エラークエリを修正
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

            //4以上の大きさの場合、普通に作っていても解決不可能なクエリがでる可能性がある
            if(content_size >= 4 && canMove < 2 && content[0] == content[content_size-1]){
                generate_random_content(content_size)
            }

            //描き方が1パターンでないクエリを増加させたい
            if(canMove == 0 && (0..100).random() <= 80){
                generate_random_content(content_size)
            }
        }
    }

    //TODO: 音楽追加

    override fun onRestart(){
        super.onRestart()
        if(stop_flag == false) {
            stop_flag = true
            mTimer!!.cancel()
            mTimer = null
            val intent = Intent(this, Title::class.java)
            finish()
            startActivity(intent)
        }
    }

    override fun onStop(){
        super.onStop()

    }

    override fun onBackPressed() {
        if(stop_flag == false) {
            stop_flag = true
            mTimer!!.cancel()
            mTimer = null
            val intent = Intent(this, Title::class.java)
            finish()
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onResume(){
        super.onResume()
        setContentView(R.layout.activity_puzzle_main)


        QueryView(this, null).init_flag()

        val data = getSharedPreferences("Data", Context.MODE_PRIVATE)
        val editor = data.edit()

        val query_num = getResources().getInteger(R.integer.query_num)


        val query = Array(query_num, {Query()})
        val queryView = Array(query_num, {QueryView(this, null)})

        val patternLockView = findViewById<com.itsxtt.patternlock.PatternLockView>(R.id.patternLockView)

        val after_background = findViewById<Button>(R.id.after_layout_background)
        val after_layout = findViewById<android.support.constraint.ConstraintLayout>(R.id.after_layout)
        after_layout.setVisibility(View.GONE)
        val after_background_create = ObjectAnimator.ofFloat(after_background, "alpha", 0.3f)


        var HighScore = data.getInt("HighScore", 0)
        findViewById<TextView>(R.id.high_score).text = "High score:"+HighScore


        //タイマー表示
        mTimer = Timer()
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                mHandler.post {
                    findViewById<TextView>(R.id.timer).text="Time\n"+time
                    time--

                    if(time < 10){
                        //235,209,190から235,190,216
                        //変化量0,-19,+26
                        findViewById<android.support.constraint.ConstraintLayout>(R.id.main_layout).setBackgroundColor(Color.rgb(235,209-(10-time)*5,216+(10-time)*5))
                    }

                    if(time < 0){
                        if(stop_flag == false) {
                            stop_flag = true
                            mTimer!!.cancel()
                            mTimer = null
                            after_layout.setVisibility(View.VISIBLE)
                            Handler().postDelayed(Runnable {
                                after_background_create.duration = 500
                                after_background_create.start()
                            }, 0)

                            Handler().postDelayed(Runnable {
                                findViewById<TextView>(R.id.after_layout_scored).alpha = 0.8.toFloat()
                                findViewById<TextView>(R.id.after_layout_score).alpha = 0.8.toFloat()
                                findViewById<TextView>(R.id.after_layout_score).text = "" + score
                            }, 500)

                            if (HighScore < score) {
                                editor.putInt("HighScore", score)
                                editor.apply()
                            }
                        }
                    }
                }
            }
        }, 1000, 1000)


        /*
         *  初期クエリ表示
         */

        var i = 1
        latency = 0
        query.forEach {

            val hand_query_id = i
            val hand_content : ArrayList<Int> = it.content

            latency += 50

            val move_queryId = getResources().getIdentifier("queryview" + i, "id", "com.kato0905.patternpuzzle")
            val move_query = findViewById<com.kato0905.patternpuzzle.QueryView>(move_queryId)
            val object_vanish = ObjectAnimator.ofFloat(move_query, "alpha", 0f)
            val object_create = ObjectAnimator.ofFloat(move_query, "alpha", 1f)

            Handler().postDelayed(Runnable {
                object_vanish.duration=300
                object_vanish.start()
            }, latency.toLong())

            val next_latency = latency + 300

            Handler().postDelayed(Runnable {
                queryView[hand_query_id - 1].make(hand_content, hand_query_id - 1)


                queryView[hand_query_id - 1].next_id(hand_query_id-1)


                val query_view_id = getResources().getIdentifier("queryview" + hand_query_id, "id", "com.kato0905.patternpuzzle")


                findViewById<com.kato0905.patternpuzzle.QueryView>(query_view_id).invalidate()

                object_create.duration=300
                object_create.start()
                Log.d("system_output","初期クエリ is "+hand_content)
        }, next_latency.toLong())

            i++
        }


        findViewById<TextView>(R.id.score).text="score\n"+score

        after_background.setOnClickListener {
            val intent = Intent(this, Title::class.java)
            finish()
            startActivity(intent)
        }


        patternLockView.setOnPatternListener(object : PatternLockView.OnPatternListener {
            override fun onStarted() {
                super.onStarted()
            }

            override fun onProgress(ids: ArrayList<Int>) {
                super.onProgress(ids)
                var i=1
                query.forEach {
                    if(it.isMatch(ids)){
                        val query_view_id = getResources().getIdentifier("queryview" + i, "id", "com.kato0905.patternpuzzle")
                        findViewById<com.kato0905.patternpuzzle.QueryView>(query_view_id).alpha=0.5.toFloat()
                    }
                    i++
                }
            }

            override fun onComplete(ids: ArrayList<Int>): Boolean {
                latency = 0
                score_rate = 1
                var i = 1
                query.forEach {
                    if(it.isMatch(ids)) {
                        //新しいクエリの作成
                        if ((0..100).random() <= 70) {
                            it.generate_random_content(difficulty)
                        } else {
                            it.generate_random_content(difficulty + 1)
                        }
                        latency += 50

                        val move_queryId = getResources().getIdentifier("queryview" + i, "id", "com.kato0905.patternpuzzle")
                        val move_query = findViewById<com.kato0905.patternpuzzle.QueryView>(move_queryId)
                        val object_vanish = ObjectAnimator.ofFloat(move_query, "alpha", 0f)
                        val object_create = ObjectAnimator.ofFloat(move_query, "alpha", 1f)

                        val next_content = it.content
                        val current_i = i

                        Handler().postDelayed(Runnable {
                            object_vanish.duration = 300
                            object_vanish.start()
                        }, latency.toLong())

                        val next_latency = latency + 300
                        Handler().postDelayed(Runnable {
                            queryView[current_i - 1].make(next_content, current_i - 1)
                            queryView[current_i - 1].next_id(current_i - 1)
                            val query_view_id = getResources().getIdentifier("queryview" + current_i, "id", "com.kato0905.patternpuzzle")
                            findViewById<com.kato0905.patternpuzzle.QueryView>(query_view_id).invalidate()
                            object_create.duration = 300
                            object_create.start()
                        }, next_latency.toLong())


                        score += 100 * score_rate
                        score_rate *= 2

                        findViewById<TextView>(R.id.score).text = "score\n" + score

                        /*
                        var evaluate_id = getResources().getIdentifier("good", "id", "com.kato0905.patternpuzzle")

                        when (score_rate) {
                            4 -> {
                                evaluate_id = getResources().getIdentifier("good", "id", "com.kato0905.patternpuzzle")
                            }

                            8 -> {
                                evaluate_id = getResources().getIdentifier("good", "id", "com.kato0905.patternpuzzle")
                            }

                            16 -> {

                            }
                        }

                        if (score_rate >= 4) {

                            val evaluate = findViewById<ImageView>(evaluate_id)
                            val evaluate_vanish = ObjectAnimator.ofFloat(evaluate, "alpha", 0f)
                            val evaluate_create = ObjectAnimator.ofFloat(evaluate, "alpha", 1f)
                            val evaluate_x = ObjectAnimator.ofFloat(evaluate, "translationX", 200f)
                            val evaluate_return_x =ObjectAnimator.ofFloat(evaluate, "translationX", 0f)

                            val evaluate_y = ObjectAnimator.ofFloat(evaluate, "translationY", -20f)
                            val evaluate_return_y = ObjectAnimator.ofFloat(evaluate, "translationY", 0f)

                            Handler().postDelayed(Runnable {
                                evaluate_create.duration = 500
                                evaluate_create.start()
                                evaluate_y.duration = 700
                                evaluate_y.start()
                            }, latency.toLong())

                            Handler().postDelayed(Runnable {
                                evaluate_vanish.duration = 300
                                evaluate_vanish.start()
                            }, latency+500.toLong())

                            Handler().postDelayed(Runnable {
                                evaluate_return_y.duration = 0
                                evaluate_return_y.start()
                            }, latency+800.toLong())


                        }
                        */
                    }

                    i++
                }


                //Toast.makeText(applicationContext, ""+query[0].content, Toast.LENGTH_LONG).show()
                return false
            }
        })



    }

}


