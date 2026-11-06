package com.visioncoders.i220795_i210611

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FollowRequestsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var requestsRecyclerView: RecyclerView
    private lateinit var adapter: FollowRequestAdapter
    private val requestList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_requests)

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        requestsRecyclerView = findViewById(R.id.requests_recyclerview)
        requestsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FollowRequestAdapter(requestList) { userId, accepted ->
            handleRequest(userId, accepted)
        }
        requestsRecyclerView.adapter = adapter

        loadFollowRequests()
    }

    private fun loadFollowRequests() {
        val currentUid = auth.currentUser?.uid ?: return
        val requestsRef = database.reference.child("users").child(currentUid).child("followRequests")

        requestsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requestList.clear()
                for (requestSnapshot in snapshot.children) {
                    requestSnapshot.key?.let { requestList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    private fun handleRequest(requesterId: String, accepted: Boolean) {
        val currentUid = auth.currentUser?.uid ?: return
        val currentUserRef = database.reference.child("users").child(currentUid)
        val requesterUserRef = database.reference.child("users").child(requesterId)

        // Remove the request
        currentUserRef.child("followRequests").child(requesterId).removeValue()

        if (accepted) {
            // Add to followers list of current user
            currentUserRef.child("followers").child(requesterId).setValue(true)
            // Add to following list of requester
            requesterUserRef.child("following").child(currentUid).setValue(true)
        }
    }
}