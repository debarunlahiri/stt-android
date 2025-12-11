package com.debarunlahiri.stt

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.debarunlahiri.stt.ui.screen.HomeActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}