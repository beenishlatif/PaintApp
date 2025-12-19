package com.example.paintingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val etConfirmNewPassword = findViewById<EditText>(R.id.etConfirmNewPassword)
        val btnSetPassword = findViewById<Button>(R.id.btnSetPassword)

        val sharedPref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        btnSetPassword.setOnClickListener {
            val newPass = etNewPassword.text.toString()
            val confirm = etConfirmNewPassword.text.toString()

            if (newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Enter both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedPref.edit().apply {
                putString("password", newPass)
                apply()
            }

            Toast.makeText(this, "Password Updated!", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, LoginSignupActivity::class.java))
            finish()
        }
    }
}
