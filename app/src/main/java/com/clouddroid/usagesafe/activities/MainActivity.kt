package com.clouddroid.usagesafe.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.clouddroid.usagesafe.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
    }
}
