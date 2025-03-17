package com.wuabstudio.testypuzzle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.wuabstudio.testypuzzle.GameActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Ana menü butonlarını ayarlama
        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnHowToPlay).setOnClickListener {
            startActivity(Intent(this, HowToPlayActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}