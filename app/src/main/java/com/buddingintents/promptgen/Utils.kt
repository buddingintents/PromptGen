
package com.buddingintents.promptgen

object Utils {
    // Basic heuristics to block answers and keep output prompt-like.
    fun isValidPrompt(text: String): Boolean {
        return true
//        val t = text.trim()
//        if (t.isEmpty()) return false
//        val lower = t.lowercase()
//        val bannedStart = listOf("sure", "here is", "answer:", "the answer", "i will", "i'm going to")
//        if (bannedStart.any { lower.startsWith(it) }) return false
//        if (t.length > 800) return false
//        return true
    }

    // If the model includes "Sure, here's..." trim common prefixes
    fun ensurePromptOnly(text: String): String {
        var t = text.trim()
//        val prefixes = listOf("sure,", "sure:", "here's", "here is", "answer:", "the answer:")
//        for (p in prefixes) {
//            if (t.lowercase().startsWith(p)) {
//                t = t.substring(p.length).trim()
//            }
//        }
        return t
    }
}
