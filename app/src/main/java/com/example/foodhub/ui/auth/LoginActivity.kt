package com.example.foodhub.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.foodhub.R
import com.example.foodhub.ui.home.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvEmailError: TextView
    private lateinit var tvPasswordError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail       = findViewById(R.id.etEmail)
        etPassword    = findViewById(R.id.etPassword)
        tvEmailError  = findViewById(R.id.tvEmailError)
        tvPasswordError = findViewById(R.id.tvPasswordError)

        findViewById<Button>(R.id.btnLogin).setOnClickListener { handleLogin() }

        findViewById<TextView>(R.id.tvGoRegister).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun handleLogin() {
        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        var hasError = false

        // Validate email
        when {
            email.isEmpty() -> {
                showError(etEmail, tvEmailError, "Vui lòng nhập email"); hasError = true
            }
            !email.contains("@") -> {
                showError(etEmail, tvEmailError, "Email không hợp lệ, phải có dấu @"); hasError = true
            }
            else -> hideError(etEmail, tvEmailError)
        }

        // Validate password
        if (password.isEmpty()) {
            showError(etPassword, tvPasswordError, "Vui lòng nhập mật khẩu"); hasError = true
        } else {
            hideError(etPassword, tvPasswordError)
        }

        if (!hasError) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showError(field: EditText, errorView: TextView, msg: String) {
        field.setBackgroundResource(R.drawable.bg_field_error)
        errorView.text = msg
        errorView.visibility = View.VISIBLE
    }

    private fun hideError(field: EditText, errorView: TextView) {
        field.setBackgroundResource(R.drawable.bg_field)
        errorView.visibility = View.GONE
    }
}
