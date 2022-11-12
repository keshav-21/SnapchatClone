package com.clone.snapchatclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        val window = window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        // finally change the color
        window.statusBarColor = ContextCompat.getColor(this,R.color.snapYellow)

        val auth = Firebase.auth
        if (auth.currentUser == null) {
            val linearLayout = findViewById<LinearLayout>(R.id.mainActivityLinearLayout)
            linearLayout.visibility = View.VISIBLE
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, SnapsActivity::class.java)
                startActivity(intent)
                finish()
            },1500)
        }

        /*val intent = Intent(this,ChooseUsersActivity::class.java)
        startActivity(intent)*/

    }

    fun goToLogInActivity(view: View) {
        val intent = Intent(this, SignupActivity::class.java)
        intent.putExtra("isSigningUp", false)
        startActivity(intent)
    }

    fun goToSignUpActivity(view: View) {
        val intent = Intent(this, SignupActivity::class.java)
        intent.putExtra("isSigningUp", true)
        startActivity(intent)
    }
}