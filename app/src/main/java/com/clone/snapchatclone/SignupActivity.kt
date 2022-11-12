package com.clone.snapchatclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {
    
    private lateinit var emailTIL: TextInputLayout
    private lateinit var passwordTIL: TextInputLayout
    
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var signUpProgressBar: ProgressBar

    private var firebaseAuth: FirebaseAuth = Firebase.auth

    private var isSigningUp = true

    lateinit var imm: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        emailTIL = findViewById(R.id.signUpEmailTIL)
        passwordTIL = findViewById(R.id.signUpPasswordTIL)
        emailEditText = findViewById(R.id.signUpEmailET)
        passwordEditText = findViewById(R.id.signUpPasswordET)
        signUpProgressBar = findViewById(R.id.signUpProgressBar)

        emailEditText.requestFocus()

        isSigningUp = intent.getBooleanExtra("isSigningUp", true)

        if (!isSigningUp) {
            val signUpSignUpTV: TextView = findViewById(R.id.signUpSignUpTV)
            signUpSignUpTV.text = "Log In"
        }

        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        passwordEditText.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) {
                signUp(View(this))
            }
            return@setOnEditorActionListener false
        }

        emailEditText.postDelayed ({
            imm.showSoftInput(emailEditText,InputMethodManager.SHOW_IMPLICIT)
        },1000)
    }


    fun signUp(view: View) {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        var isEmpty = false
        if (email.isEmpty()) {
            isEmpty = true
            emailTIL.error = "Email cannot be empty"
        }
        if (password.isEmpty()) {
            isEmpty = true
            passwordTIL.error = "Password cannot be empty"
        }

        emailTIL.error = null
        passwordTIL.error = null

        signUpProgressBar.visibility = View.VISIBLE

        if (isSigningUp) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.i("SignUp","Successful")

                        val userId = task.result.user?.uid

                        val firebaseDatabase: FirebaseDatabase = Firebase.database
                        val databaseReference: DatabaseReference = firebaseDatabase.reference
                        val databaseTask: Task<Void> = databaseReference.child("users").child(userId!!).child("email").setValue(email)

                        databaseTask.addOnSuccessListener {
                            signUpProgressBar.visibility = View.INVISIBLE

                            Toast.makeText(
                                this@SignupActivity,
                                "Sign Up Successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            startSnapsActivity()
                        }

                        databaseTask.addOnFailureListener {
                            signUpProgressBar.visibility = View.INVISIBLE
                            it.printStackTrace()
                        }

                    } else {
                        signUpProgressBar.visibility = View.INVISIBLE

                        val e = task.exception
                        e?.printStackTrace()

//                        java.lang.IllegalArgumentException: Given String is empty or null
//                        com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted.
//                        com.google.firebase.auth.FirebaseAuthWeakPasswordException: The given password is invalid. [Password should be at least 6 characters]
//                        com.google.firebase.auth.FirebaseAuthException: The given sign-in provider is disabled for this Firebase project.
//                                                                        Enable it in the Firebase console, under the sign-in method tab of the Auth section.
//                        com.google.firebase.auth.FirebaseAuthUserCollisionException: The email address is already in use by another account.

                        if (e!!.message == "The email address is badly formatted.") {
                            emailTIL.error = "The email address is invalid."
                        } else if (e.message == "The given password is invalid. [ Password should be at least 6 characters ]") {
                            passwordTIL.error = "Password should be at least 6 characters."
                        } else if (e.message == "The email address is already in use by another account."){
                            Toast.makeText(this@SignupActivity,
                                "The email address is already in use by another account.",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    signUpProgressBar.visibility = View.INVISIBLE

                    if (task.isSuccessful) {
                        Toast.makeText(this@SignupActivity, "Log In Successful", Toast.LENGTH_SHORT).show()
                        startSnapsActivity()
                    } else {
                        val e = task.exception
                        e?.printStackTrace()

//                        java.lang.IllegalArgumentException: Given String is empty or null
//                        com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted.
//                        com.google.firebase.auth.FirebaseAuthInvalidUserException: There is no user record corresponding to this identifier.
//                                                                                   The user may have been deleted.
//                        com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The password is invalid or the user does not have a password.

                        if (e!!.message == "The email address is badly formatted.") {
                            emailTIL.error = "The email address is invalid."
                        } else if (e.message == "The given password is invalid. [ Password should be at least 6 characters ]") {
                            passwordTIL.error = "Password should be at least 6 characters."
                        } else if (e.message == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                            Toast.makeText(this@SignupActivity, "no user with that email", Toast.LENGTH_SHORT)
                                .show()
                        } else if (e.message == "The password is invalid or the user does not have a password."){
                            Toast.makeText(this@SignupActivity, "Incorrect password", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
        }
    }

    private fun startSnapsActivity() {
        val intent = Intent(this@SignupActivity,SnapsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}