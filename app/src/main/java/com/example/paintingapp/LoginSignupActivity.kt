package com.example.paintingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginSignupActivity : AppCompatActivity() {

    private lateinit var btnGuestLogin: Button
    private lateinit var btnLogin: Button
    private lateinit var tvSignupLink: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)

        // ------------------- Views -------------------
        btnGuestLogin = findViewById(R.id.btnGuestLogin)
        btnLogin = findViewById(R.id.btnLoginSignup)
        tvSignupLink = findViewById(R.id.tvSignupLink)
        tvForgotPassword = findViewById(R.id.tvForgetPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        // SharedPreferences for stored credentials
        val sharedPref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        // ---------------- Guest Login ----------------
        btnGuestLogin.setOnClickListener {
            Toast.makeText(this, "Continuing as Guest", Toast.LENGTH_SHORT).show()
            openPaintActivity()
        }

        // ---------------- Login Button ----------------
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            val savedEmail = sharedPref.getString("email", null)
            val savedPassword = sharedPref.getString("password", null)

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email & Password required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email == savedEmail && password == savedPassword) {
                Toast.makeText(this, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                openPaintActivity()
            } else {
                Toast.makeText(this, "Invalid credentials. Please Sign Up!", Toast.LENGTH_SHORT).show()
            }
        }

        // ---------------- SignUp Link ----------------
        tvSignupLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // ---------------- Forgot Password ----------------
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    // ---------------- Open PaintActivity ----------------
    private fun openPaintActivity() {
        val intent = Intent(this, PaintActivity::class.java)
        startActivity(intent)
        finish()
    }
}
