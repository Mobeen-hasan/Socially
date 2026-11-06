package com.visioncoders.i220795_i210611

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class SearchMain : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<FullUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_main)


        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }


        database = FirebaseDatabase.getInstance()
        searchView = findViewById(R.id.search_view)
        recyclerView = findViewById(R.id.search_results_recyclerview)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(userList)
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchUsers(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchUsers(it) }
                return false
            }
        })
    }

    private fun searchUsers(searchText: String) {
        val usersRef = database.reference.child("users")
        val query = usersRef.orderByChild("username").startAt(searchText).endAt(searchText + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(FullUser::class.java)
                    user?.let {
                        it.uid = userSnapshot.key ?: ""
                        userList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }
}
