package com.wuabstudio.testypuzzle

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HowToPlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_play)
        
        // Set back button
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        // Set instructions text
        findViewById<TextView>(R.id.tvHowToPlay).text = getString(R.string.how_to_play_text)
    }
} 