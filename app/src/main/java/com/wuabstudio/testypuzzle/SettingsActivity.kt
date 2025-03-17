package com.wuabstudio.testypuzzle

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var soundSwitch: Switch
    private lateinit var musicSwitch: Switch
    private lateinit var vibrationSwitch: Switch
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        soundSwitch = findViewById(R.id.switchSound)
        musicSwitch = findViewById(R.id.switchMusic)
        vibrationSwitch = findViewById(R.id.switchVibration)
        
        // Load settings
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        soundSwitch.isChecked = prefs.getBoolean("sound", true)
        musicSwitch.isChecked = prefs.getBoolean("music", true)
        vibrationSwitch.isChecked = prefs.getBoolean("vibration", true)
        
        // Back button
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            saveSettings()
            finish()
        }
    }
    
    private fun saveSettings() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        editor.putBoolean("sound", soundSwitch.isChecked)
        editor.putBoolean("music", musicSwitch.isChecked)
        editor.putBoolean("vibration", vibrationSwitch.isChecked)
        
        editor.apply()
    }
    
    override fun onPause() {
        super.onPause()
        saveSettings()
    }
} 