package com.visioncoders.i220795_i210611

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val usernameInput = findViewById<EditText>(R.id.username_input)
        val nameInput = findViewById<EditText>(R.id.name_input)
        val lastnameInput = findViewById<EditText>(R.id.lastname_input)
        val dobInput = findViewById<EditText>(R.id.dob_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val createAccountButton = findViewById<Button>(R.id.create_account_button)
        val backArrow = findViewById<ImageView>(R.id.back_arrow)

        createAccountButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val name = nameInput.text.toString().trim()
            val lastname = lastnameInput.text.toString().trim()
            val dob = dobInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty() || name.isEmpty() || lastname.isEmpty()) {
                Toast.makeText(baseContext, "Please fill out all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            // Correctly create the user object with named arguments
                            val user = FullUser(
                                uid = currentUser.uid,
                                username = username,
                                firstName = name,
                                lastName = lastname,
                                dob = dob,
                                email = email
                            )
                            val usersRef = database.getReference("users")

                            usersRef.child(currentUser.uid).setValue(user)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        val intent = Intent(this, HomeActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        val dbErrorMessage = dbTask.exception?.message ?: "Database error."
                                        Log.e("SignupActivity", "Failed to save user data: $dbErrorMessage")
                                        Toast.makeText(baseContext, "Failed to save profile: $dbErrorMessage", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    } else {
                        val authErrorMessage = task.exception?.message ?: "Authentication failed."
                        Log.e("SignupActivity", "Authentication failed: $authErrorMessage")
                        Toast.makeText(baseContext, "Authentication failed: $authErrorMessage", Toast.LENGTH_LONG).show()
                    }
                }
        }

        backArrow.setOnClickListener { finish() }
    }
}