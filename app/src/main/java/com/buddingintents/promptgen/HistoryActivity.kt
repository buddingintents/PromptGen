package com.buddingintents.promptgen


import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val listView: ListView = findViewById(R.id.listView)
        val records = HistoryStore.getRecords(this)

        listView.adapter = HistoryAdapter(this, records)
    }
}