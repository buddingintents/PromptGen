package com.buddingintents.promptgen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast

class HistoryAdapter(
    private val ctx: Context,
    private val records: List<PromptRecord>
) : BaseAdapter() {

    override fun getCount() = records.size
    override fun getItem(position: Int) = records[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(ctx)
            .inflate(R.layout.item_history, parent, false)
        val tvUser: TextView = view.findViewById(R.id.tvUser)
        val tvPrompt: TextView = view.findViewById(R.id.tvPrompt)

        val rec = records[position]
        tvUser.text = "User: ${rec.userText}"
        tvPrompt.text = "Prompt: ${rec.generatedPrompt}"

        // Long-press to copy both texts
        view.setOnLongClickListener {
            val clipText = "User: ${rec.userText}\nPrompt: ${rec.generatedPrompt}"
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("PromptRecord", clipText))
            Toast.makeText(ctx, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            true
        }

        return view
    }
}