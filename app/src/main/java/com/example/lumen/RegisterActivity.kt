package com.example.lumen


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailTextView: EditText
    private lateinit var passwordTextView: EditText
    private lateinit var button: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        auth = FirebaseAuth.getInstance()
        emailTextView = findViewById(R.id.email_edittext)
        passwordTextView = findViewById(R.id.password_edittext)
        button = findViewById(R.id.button)

        val login:TextView=findViewById(R.id.clickHereToLogin)

        login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        button.setOnClickListener {
            registerNewUser()
        }
    }

    private fun registerNewUser(){
        val email = emailTextView.text.toString()
        val password = passwordTextView.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please Enter Crendentials", Toast.LENGTH_LONG).show()
        } else{
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                }else{
                    val errorMessage=task.exception?.message ?: "Registration failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}