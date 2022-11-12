package com.clone.snapchatclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.util.contains
import androidx.core.util.isEmpty
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class ChooseUsersActivity : AppCompatActivity() {

    lateinit var chooseUsersListView: ListView
    var emailsArrayList = ArrayList<String>()
    var keysArrayList = ArrayList<String>()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.choose_users_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_users)
        title = "Choose users"

        chooseUsersListView = findViewById(R.id.chooseUsersListView)
        chooseUsersListView.choiceMode = AbsListView.CHOICE_MODE_MULTIPLE

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, emailsArrayList)
        chooseUsersListView.adapter = arrayAdapter

        val userUid = Firebase.auth.currentUser!!.uid

        val databaseRef = Firebase.database.reference.child("users")
        databaseRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val email = snapshot.child("email").value.toString()
                val uid = snapshot.key!!
                if (userUid.equals(uid)) return

                keysArrayList.add(uid)
                emailsArrayList.add(email)
                arrayAdapter.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun sendSnapToSelectedUsers(menuItem: MenuItem) {
        val array = chooseUsersListView.checkedItemPositions
        Log.i("array",array.toString())

        if (array.isEmpty()) {
            Toast.makeText(this, "Select atleast one user", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = intent

        val imageName = intent.getStringExtra("imageName")!!
        val snapMap = mapOf(
            "from" to intent.getStringExtra("from"),
            "imageURL" to intent.getStringExtra("downloadURL"),
            "message" to intent.getStringExtra("message")
        )

        val databaseRef = Firebase.database.reference

        val usersRef = databaseRef.child("users")
//        com.google.firebase.database.DatabaseException: Invalid Firebase Database path: fb6a116f-d77d-4254-9ffc-45ea6fde8697.jpg.
//                                                        Firebase Database paths must not contain '.', '#', '$', '[', or ']'

        val sparseArray = ArrayList<Int>()
        //0: Need to Send
        //1: Already sended
        //2: No need to send
        for (i in 0 until emailsArrayList.size) {
            if (array.contains(i)) {
                if (array.get(i)) {
                    sparseArray.add(0)
                } else {
                    sparseArray.add(2)
                }
            } else {
                sparseArray.add(2)
            }
        }
        Log.i("sparse",sparseArray.toString())


        for (index in 0 until sparseArray.size){
            if (sparseArray.get(index) == 2) continue
            usersRef.child(keysArrayList[index]).child("snaps").child(imageName).setValue(snapMap)
                .addOnSuccessListener {
                    Log.i("snaps", "SNAP SEND TO ${emailsArrayList.get(index)}")
                    sparseArray.set(index,1)
                    Log.i("sparse",sparseArray.toString())

                    var canGo = true
                    for (i in 0 until sparseArray.size) {
                        val value = sparseArray.get(i)
                        if (value == 0) {
                            canGo = false
                            break
                        }
                    }

                    if (canGo) startSnapsActivity()

                }.addOnFailureListener {

                    Toast.makeText(this@ChooseUsersActivity,
                        "snap failed to send $${emailsArrayList.get(index)}",
                        Toast.LENGTH_SHORT).show()

                    Log.i("snaps", "SNAP FAILED TO SEND ${emailsArrayList.get(index)}")
                    it.printStackTrace()
                }
        }
    }

    private fun startSnapsActivity() {
        val intent = Intent(this@ChooseUsersActivity,SnapsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}