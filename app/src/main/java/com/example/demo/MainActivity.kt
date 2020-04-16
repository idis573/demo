package com.example.demo

import android.graphics.Color
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    var camera:Camera?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA),1)
        setContentView(R.layout.activity_main)
        btn.setOnClickListener{
            val tv = TextView(this)
            tv.setText("hello word")
            tv.setBackgroundColor(Color.BLUE)
            FloatBallWindowManager.getInstance(this).addFloatBall(tv,0,100,100,null)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        camera?.release()
    }

}
