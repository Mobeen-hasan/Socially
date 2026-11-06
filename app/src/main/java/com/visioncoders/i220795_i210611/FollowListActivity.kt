package com.visioncoders.i220795_i210611

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class FollowListActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<FullUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        database = FirebaseDatabase.getInstance()

        val toolbar: Toolbar = findViewById(R.id.follow_list_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.follow_list_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(userList)
        recyclerView.adapter = adapter

        val listType = intent.getStringExtra("LIST_TYPE") // "Followers" or "Following"
        val userId = intent.getStringExtra("USER_ID")

        if (listType == null || userId == null) {
            finish()
            return
        }

        supportActionBar?.title = listType
        loadUserList(userId, listType)
    }

    private fun loadUserList(userId: String, listType: String) {
        val path = if (listType == "Followers") "followers" else "following"
        val listRef = database.reference.child("users").child(userId).child(path)

        listRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userIds = snapshot.children.mapNotNull { it.key }
                fetchUsers(userIds)
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    private fun fetchUsers(userIds: List<String>) {
        userList.clear()
        val usersRef = database.reference.child("users")
        userIds.forEach { userId ->
            usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(FullUser::class.java)
                    if (user != null) {
                        user.uid = snapshot.key.toString()
                        userList.add(user)
                        adapter.notifyDataSetChanged()
                    }
                }
                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}