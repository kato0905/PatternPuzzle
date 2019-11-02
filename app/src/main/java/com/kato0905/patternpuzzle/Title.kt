package com.kato0905.patternpuzzle

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.widget.TextView


class Title : AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        val data = getSharedPreferences("Data", Context.MODE_PRIVATE)
        var HighScore = data.getInt("HighScore", 0)
        findViewById<TextView>(R.id.high_score).text = "High score:"+HighScore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)

        findViewById<Button>(R.id.start_button).setOnClickListener {
            val intent = Intent(this, PuzzleMain::class.java)
            finish()
            startActivity(intent)
        }

        findViewById<Button>(R.id.practice_button).setOnClickListener {
            val intent = Intent(this, Practice::class.java)
            finish()
            startActivity(intent)
        }
    }
}
