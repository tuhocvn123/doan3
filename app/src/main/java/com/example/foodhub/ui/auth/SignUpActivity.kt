package com.example.foodhub.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.foodhub.R
import com.example.foodhub.ui.home.MainActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbAgree: CheckBox
    private lateinit var tvNameError: TextView
    private lateinit var tvEmailError: TextView
    private lateinit var tvPhoneError: TextView
    private lateinit var tvPasswordError: TextView
    private lateinit var tvAgreeError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        etName        = findViewById(R.id.etName)
        etEmail       = findViewById(R.id.etEmail)
        etPhone       = findViewById(R.id.etPhone)
        etPassword    = findViewById(R.id.etPassword)
        cbAgree       = findViewById(R.id.cbAgree)
        tvNameError   = findViewById(R.id.tvNameError)
        tvEmailError  = findViewById(R.id.tvEmailError)
        tvPhoneError  = findViewById(R.id.tvPhoneError)
        tvPasswordError = findViewById(R.id.tvPasswordError)
        tvAgreeError  = findViewById(R.id.tvAgreeError)

        findViewById<Button>(R.id.btnRegister).setOnClickListener { handleRegister() }
        findViewById<TextView>(R.id.tvGoLogin).setOnClickListener { finish() }
    }

    private fun handleRegister() {
        val name     = etName.text.toString().trim()
        val email    = etEmail.text.toString().trim()
        val phone    = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val agreed   = cbAgree.isChecked
        var hasError = false

        if (name.isEmpty()) { showError(etName, tvNameError, "Vui lòng nhập họ và tên"); hasError = true }
        else hideError(etName, tvNameError)

        when {
            email.isEmpty() -> { showError(etEmail, tvEmailError, "Vui lòng nhập email"); hasError = true }
            !email.contains("@") -> { showError(etEmail, tvEmailError, "Email phải chứa dấu @"); hasError = true }
            else -> hideError(etEmail, tvEmailError)
        }

        if (phone.isEmpty()) { showError(etPhone, tvPhoneError, "Vui lòng nhập số điện thoại"); hasError = true }
        else hideError(etPhone, tvPhoneError)

        if (password.length < 6) { showError(etPassword, tvPasswordError, "Mật khẩu tối thiểu 6 ký tự"); hasError = true }
        else hideError(etPassword, tvPasswordError)

        if (!agreed) { tvAgreeError.text = "Vui lòng đồng ý điều khoản dịch vụ"; tvAgreeError.visibility = View.VISIBLE; hasError = true }
        else tvAgreeError.visibility = View.GONE

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
