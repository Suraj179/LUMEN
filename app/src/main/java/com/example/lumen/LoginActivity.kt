package com.example.lumen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var button: Button
    private lateinit var remeberMe: CheckBox
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstancesState: Bundle?){
        super.onCreate(savedInstancesState)
        setContentView(R.layout.activity_login)

        auth=FirebaseAuth.getInstance()
        email=findViewById(R.id.editTextTextEmailAddress)
        password=findViewById(R.id.editTextNumberPassword)
        button=findViewById(R.id.button)
        remeberMe=findViewById(R.id.checkBox)

        val register:TextView=findViewById(R.id.clickHereToRegister)

        register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity:: class.java))
        }

        button.setOnClickListener {
            loginUserAccount()
        }

    }

    fun loginUserAccount(){
        val email=email.text.toString()
        val password= password.text.toString()

        if (email.isEmpty() || password.isEmpty() ){
            Toast.makeText(this,"Please enter credentials", Toast.LENGTH_LONG).show()
        }else{
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Login Successful!!", Toast.LENGTH_LONG).show()
//                    startActivity(Intent(this, Dashboard))
                } else{
                    Toast.makeText(this,"Login failed!!",Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

