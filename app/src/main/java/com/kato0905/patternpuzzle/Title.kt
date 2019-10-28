package com.kato0905.patternpuzzle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent



class Title : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)

        findViewById<Button>(R.id.start_button).setOnClickListener {
            val intent = Intent(this, PuzzleMain::class.java)
            startActivity(intent)
        }
    }
}
