package com.shmedo.mcloudapp.common.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shmedo.mcloudapp.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent()
            intent.setClass(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
}