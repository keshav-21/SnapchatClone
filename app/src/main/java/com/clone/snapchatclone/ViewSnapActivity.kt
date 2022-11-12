package com.clone.snapchatclone

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors

class ViewSnapActivity : AppCompatActivity() {

    lateinit var viewSnapImageView: ImageView
    lateinit var viewSnapTextView: TextView
    lateinit var viewSnapProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_snap)

        viewSnapImageView = findViewById(R.id.viewSnapImageView)
        viewSnapTextView = findViewById(R.id.viewSnapTextView)
        viewSnapProgressBar = findViewById(R.id.viewSnapProgressBar)

        val index = intent.getIntExtra("index", -1)
        if (index == -1) finish()

        val myExecutor = Executors.newSingleThreadExecutor()
        val myHandler = Handler(Looper.getMainLooper())

        myExecutor.execute {
            var bitmap: Bitmap? = null

            try {
                val url = URL(SnapsActivity.snapsDataSnapshot.get(index).child("imageURL").value as String)
                val httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.connect()

                val inputStream = httpURLConnection.inputStream
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: MalformedURLException) {
                Log.i("ERROR", "Invalid URL")
                e.printStackTrace()
            } catch (e: IOException) {
                Log.i("ERROR", "IO EXCEPTION")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.i("ERROR", "EXCEPTION")
                e.printStackTrace()
            }

            myHandler.post {
                viewSnapProgressBar.visibility = View.INVISIBLE
                if (bitmap != null) {
                    viewSnapImageView.setImageBitmap(bitmap)
//                  android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
                    viewSnapTextView.text = SnapsActivity.snapsDataSnapshot.get(index).child("message").value as String

                    //Deleting Data
                    val userUid = Firebase.auth.currentUser!!.uid
                    val databaseRef = Firebase.database.reference.child("users/$userUid/snaps")
                    databaseRef.child(SnapsActivity.snapsDataSnapshot.get(index).key!!).removeValue()

                    //Deleting Snap UI
                    SnapsActivity.snapsArray.removeAt(index)
                    SnapsActivity.snapsDataSnapshot.removeAt(index)
                    SnapsActivity.snapsAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}
