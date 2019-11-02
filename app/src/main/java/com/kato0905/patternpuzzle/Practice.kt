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
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.math.max



class Practice : AppCompatActivity() {
    var difficulty = 3
    var latency = 0
    var current_level = 1
    var ideal_ids: ArrayList<Int> = arrayListOf(0, 1, 2, 3, 4, 5, 6, 7, 8)

    private val mHandler = Handler()

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
            content= arrayListOf(1,2,8)
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

        fun ideal_ids_conversion(ids: ArrayList<Int>): ArrayList<Int>{

            var i = 0
            while(ids.size-2-i >= 0){
                error_array[ids[i]].forEach{
                    if(ids[i+1] == it){
                        //エラーアレイが既に出ていたらそのまま
                        if(ids.indexOf((it+ids[i])/2) < i){

                        }else{
                            var swap = ids[i+1]
                            var swap_id = ids.indexOf((it+ids[i])/2)
                            ids[i+1] = ids[swap_id]
                            ids[swap_id] = swap

                        }
                    }
                }
                i++
            }

            return ids
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

        fun content_move(){

            //contentはできるだけ右下に配置
            for(i in 0..max(column_num, row_num)-1){
                //下にずらす
                if(content.all{ it <= row_num*(column_num-1)-1 }){
                    for(j in 0..content.size-1){
                        content[j] += row_num
                    }
                }
                //右にずらす
                if(content.all{ (it+1)%row_num != 0 }){
                    for(j in 0..content.size-1){
                        content[j] += 1
                    }
                }
            }

        }
    }


    override fun onRestart(){
        super.onRestart()
        /*
        val intent = Intent(this, Title::class.java)
        finish()
        startActivity(intent)
        */
    }

    override fun onStop(){
        super.onStop()

    }

    override fun onBackPressed() {
        val intent = Intent(this, Title::class.java)
        finish()
        startActivity(intent)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onResume(){
        super.onResume()
        setContentView(R.layout.activity_practice)

        QueryView(this, null).init_flag()

        val query_num = getResources().getInteger(R.integer.query_num)

        val query = Array(query_num, {Query()})
        val queryView = Array(query_num, {QueryView(this, null)})
        val answerView = AnswerView(this, null)

        val patternLockView = findViewById<com.itsxtt.patternlock.PatternLockView>(R.id.patternLockView)

        val after_background = findViewById<Button>(R.id.after_layout_background)
        val after_layout = findViewById<android.support.constraint.ConstraintLayout>(R.id.after_layout)
        after_layout.setVisibility(View.GONE)
        val after_background_create = ObjectAnimator.ofFloat(after_background, "alpha", 0.3f)


        /*
         *  クエリ作成
         */
        fun make_query() {
            ideal_ids = arrayListOf(0, 1, 2, 3, 4, 5, 6, 7, 8)

            //ideal_idsを一筆書きできる形にする
            Collections.shuffle(ideal_ids)

            ideal_ids = Query().ideal_ids_conversion(ideal_ids)

            ideal_ids = Query().ids_conversion(ideal_ids)

            Log.d("system_output","ideal_ids is "+ideal_ids)

            val query_id_s = arrayListOf<Int>(0, 1, 2, 3, 4)
            Collections.shuffle(query_id_s)

            //queryをもっといい感じにしたい

            var i = 0
            query.forEach {
                var sublist_start = (ideal_ids.size - 3)/4.toInt()

                var sub_array: List<Int>
                if(i != 4){
                    if((0..100).random() < 30){
                        sub_array = ideal_ids.subList(i * sublist_start, i * sublist_start + 4)
                    }else{
                        sub_array = ideal_ids.subList(i * sublist_start, i * sublist_start + 3)
                    }
                }else{
                    sub_array = ideal_ids.subList(ideal_ids.size-3, ideal_ids.size)
                }
                var hoge: ArrayList<Int> = arrayListOf()
                sub_array.forEach {
                    hoge.add(it)
                }
                query[query_id_s[i]].content = hoge
                query[query_id_s[i]].content_move()
                i++
            }
        }

        make_query()

        /*
         *  初期クエリ表示
         */
        fun display_query() {
            var i = 1
            latency = 0
            query.forEach {

                val hand_query_id = i
                val hand_content: ArrayList<Int> = it.content

                latency += 50

                val move_queryId = getResources().getIdentifier("queryview" + i, "id", "com.kato0905.patternpuzzle")
                val move_query = findViewById<com.kato0905.patternpuzzle.QueryView>(move_queryId)
                val object_vanish = ObjectAnimator.ofFloat(move_query, "alpha", 0f)
                val object_create = ObjectAnimator.ofFloat(move_query, "alpha", 1f)

                Handler().postDelayed(Runnable {
                    object_vanish.duration = 300
                    object_vanish.start()
                }, latency.toLong())

                val next_latency = latency + 300

                Handler().postDelayed(Runnable {
                    queryView[hand_query_id - 1].make(hand_content, hand_query_id - 1)


                    queryView[hand_query_id - 1].next_id(hand_query_id - 1)


                    val query_view_id = getResources().getIdentifier("queryview" + hand_query_id, "id", "com.kato0905.patternpuzzle")


                    findViewById<com.kato0905.patternpuzzle.QueryView>(query_view_id).invalidate()

                    object_create.duration = 300
                    object_create.start()
                    Log.d("system_output", "初期クエリ is " + hand_content)
                }, next_latency.toLong())

                i++
            }

        }

        display_query()

        after_background.setOnClickListener {
            val intent = Intent(this, Title::class.java)
            finish()
            startActivity(intent)
        }

        findViewById<Button>(R.id.title_button).setOnClickListener {
            val intent = Intent(this, Title::class.java)
            finish()
            startActivity(intent)
        }

        QueryView(this, null).init_flag()


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

                var i = 1
                var clear_num = 0
                Log.d("system_output","ids is "+ids)

                //クエリを消す
                query.forEach {
                    Log.d("system_output","query"+i+" is "+it.content)
                    if(it.isMatch(ids)) {
                        clear_num++
                        latency += 50

                        val move_queryId = getResources().getIdentifier("queryview" + i, "id", "com.kato0905.patternpuzzle")
                        val move_query = findViewById<com.kato0905.patternpuzzle.QueryView>(move_queryId)
                        val object_vanish = ObjectAnimator.ofFloat(move_query, "alpha", 0f)

                        Handler().postDelayed(Runnable {
                            object_vanish.duration=300
                            object_vanish.start()
                        }, latency.toLong())
                    }
                    i++
                }


                if(clear_num == query_num){
                    current_level++
                    findViewById<TextView>(R.id.level).text="Puzzle\nSTAGE"+current_level
                    make_query()
                    display_query()

                }else{

                    var after_latency = latency
                    answerView.make(ideal_ids)
                    val answer_id = findViewById<com.kato0905.patternpuzzle.AnswerView>(R.id.answer_view)
                    val answer_create = ObjectAnimator.ofFloat(answer_id, "alpha", 1f)
                    after_layout.setVisibility(View.VISIBLE)

                    Handler().postDelayed(Runnable {
                        after_background_create.duration = 500
                        after_background_create.start()
                    }, after_latency.toLong())

                    Handler().postDelayed(Runnable {
                        findViewById<TextView>(R.id.after_layout_scored).alpha = 0.8.toFloat()
                        findViewById<TextView>(R.id.after_layout_score).alpha = 0.8.toFloat()
                    }, after_latency+500.toLong())


                    Handler().postDelayed(Runnable {
                        display_query()
                        answer_id.invalidate()
                        answer_create.duration = 500
                        answer_create.start()

                    }, after_latency+1500.toLong())



                }

                //Toast.makeText(applicationContext, ""+query[0].content, Toast.LENGTH_LONG).show()
                return false
            }
        })

    }

}


