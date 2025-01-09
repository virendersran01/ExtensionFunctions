package com.virtualstudios.extensionfunctions.exception_handling

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.virtualstudios.extensionfunctions.R

class ExceptionLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                this
            )
        )
        setContentView(R.layout.activity_exceptionlog)

        val error: TextView = findViewById(R.id.error)
        val btn_sendreport: Button = findViewById(R.id.btn_sendreport)
        val s = intent.getStringExtra("exceptionlog")
        error.setText(s)


        btn_sendreport.setOnClickListener(View.OnClickListener { view: View? -> })
    }
}