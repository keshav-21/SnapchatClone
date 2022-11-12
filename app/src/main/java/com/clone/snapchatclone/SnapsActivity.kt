package com.clone.snapchatclone

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SnapsActivity : AppCompatActivity() {

    lateinit var snapsListView: ListView

    companion object {
        var snapsArray = ArrayList<String>()
        var snapsDataSnapshot = ArrayList<DataSnapshot>()
        lateinit var snapsAdapter: ArrayAdapter<String>
    }

    var getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val intent = Intent(this, SendSnapActivity::class.java)
            intent.putExtra("item", uri)
            startActivity(intent)
        } else Toast.makeText(this@SnapsActivity, "No Image Selected", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.snaps_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snaps)

        snapsListView = findViewById(R.id.snapsListView)

        snapsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, snapsArray)
        snapsListView.adapter = snapsAdapter

        val userUid = Firebase.auth.currentUser!!.uid
        val databaseRef = Firebase.database.reference.child("users/$userUid/snaps")

        snapsArray.clear()
        databaseRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapsDataSnapshot.add(snapshot)
                snapsArray.add(snapshot.child("from").value.toString())
                snapsAdapter.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

        snapsListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            val intent = Intent(this@SnapsActivity,ViewSnapActivity::class.java)
            intent.putExtra("index",position)
            startActivity(intent)
        }

    }

    fun selectImage(menuItem: MenuItem) {
        getContent.launch("image/*")
    }

    fun logOut(menuItem: MenuItem) {
        Firebase.auth.signOut()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
