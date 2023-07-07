package com.dos.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btLoadForm = findViewById<View>(R.id.bt_load_form)
        val btShowForm = findViewById<View>(R.id.bt_show_form)
        val btReset = findViewById<View>(R.id.bt_reset)

        btLoadForm.setOnClickListener {
            AMConsentManager.instance.init(this, successCallback = {
                showToast("Load form success")
                btReset.isEnabled = true
                btShowForm.isEnabled = true
            }, errorCallback = { errorMessage, errorCode ->
                showToast("$errorMessage $errorCode")
            })
        }

        btShowForm.setOnClickListener {
            AMConsentManager.instance.showFormIfAvailable(this, {
                showToast(it?.message.toString())
            })
        }

        btReset.setOnClickListener {
            showToast("Reset consent status")
            AMConsentManager.instance.reset()
            btReset.isEnabled = false
            btShowForm.isEnabled = false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}