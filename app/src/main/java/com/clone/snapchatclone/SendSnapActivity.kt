package com.clone.snapchatclone

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

class SendSnapActivity : AppCompatActivity() {

    lateinit var sendSnapImageView: ImageView
    lateinit var captionEditText: EditText
    lateinit var sendSnapProgressBar: ProgressBar
    lateinit var sendSnapLoadTV: TextView

    lateinit var uri: Uri
    lateinit var imageName: String
    lateinit var newImageRef: StorageReference

    var downloadURL : String? = null

    var getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { newUri ->
        if (newUri != null) {
            this.uri = newUri
            if (downloadURL == null) {
                sendSnapImageView.setImageURI(uri)
            } else {
                downloadURL = null
            }
        } else Toast.makeText(this@SendSnapActivity, "No Image Selected", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.send_snap_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_snap)

        sendSnapImageView = findViewById(R.id.sendSnapImageView)
        captionEditText = findViewById(R.id.captionEditText)
        sendSnapProgressBar = findViewById(R.id.sendSnapProgressBar)
        sendSnapLoadTV = findViewById(R.id.sendSnapLoadTV)

        uri = intent.getParcelableExtra("item")!!

        sendSnapImageView.setImageURI(uri)


        val mStorage = Firebase.storage
        val mainStorageRef = mStorage.reference
        val imagesFolderRef = mainStorageRef.child("images")

        imageName = UUID.randomUUID().toString()

        newImageRef = imagesFolderRef.child("$imageName.jpg")
    }

    fun sendSnap(view: View) {
        Firebase.auth.currentUser ?: return

        if ( downloadURL!= null) {
            startChooseUsersActivity()
            return
        }

        uploadImage()
    }

    fun changeImage(menuItem: MenuItem) {
        getContent.launch("image/*")
    }

    fun uploadImage() {
        sendSnapProgressBar.visibility = View.VISIBLE
        sendSnapLoadTV.visibility = View.VISIBLE

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos)
        val data = baos.toByteArray()

        val uploadTask = newImageRef.putBytes(data)

        uploadTask.addOnSuccessListener { taskSnapShot ->
            newImageRef.downloadUrl.addOnCompleteListener { task ->
                sendSnapProgressBar.visibility = View.INVISIBLE
                sendSnapLoadTV.visibility = View.INVISIBLE
                if (task.isSuccessful) {
                    Log.i("URL", task.result.toString())

                    downloadURL = task.result.toString()

                    startChooseUsersActivity()
                } else {
                    Log.i("URL", "Failed to get URL")
                    task.exception?.printStackTrace()
//                  com.google.firebase.storage.StorageException: Object does not exist at location.
                }
            }
        }

        uploadTask.addOnFailureListener { e ->
            Toast.makeText(this@SendSnapActivity, "Sending Snap Failed. Try again.", Toast.LENGTH_SHORT)
                .show()
            sendSnapProgressBar.visibility = View.INVISIBLE
            sendSnapLoadTV.visibility = View.INVISIBLE
            e.printStackTrace()
        }
    }

    fun startChooseUsersActivity() {
        val user: FirebaseUser = Firebase.auth.currentUser ?: return

        val intent = Intent(this,ChooseUsersActivity::class.java)
        intent.putExtra("from",user.email)
        intent.putExtra("imageName",imageName)
        intent.putExtra("downloadURL",downloadURL)
        intent.putExtra("message",captionEditText.text.toString())

        startActivity(intent)
    }
}