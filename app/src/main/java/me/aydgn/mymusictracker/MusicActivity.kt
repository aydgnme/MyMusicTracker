package me.aydgn.mymusictracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MusicActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val username = intent.getStringExtra("username") ?: "User"

        val textView = findViewById<TextView>(R.id.tvHelloUsername)
        textView.text = "Hello, $username!"
    }
}