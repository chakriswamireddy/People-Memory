package com.peoplememory.app.ai

import android.content.Context
import com.google.ai.edge.litertlm.LlmInference
import com.google.ai.edge.litertlm.LlmInferenceOptions
import com.peoplememory.app.model.CloudMemory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalAIService(private val context: Context) {

    private var llmInference: LlmInference? = null
    private var isInitialized = false

    suspend fun initialize(modelFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!modelFile.exists() || modelFile.length() < 100_000_000L) {
                return@withContext Result.failure(IllegalStateException("Model file is incomplete or missing."))
            }

            release()

            val options = LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(1024)
                .setTopK(20)
                .setTemperature(0.2f) // Low temperature for high factual accuracy
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            isInitialized = true
            Result.success(Unit)
        } catch (e: Exception) {
            isInitialized = false
            llmInference = null
            Result.failure(e)
        }
    }

    fun isReady(): Boolean = isInitialized && llmInference != null

    /**
     * AI Feature 1: Rewrite Memory with Strict Factual Preservation
     */
    suspend fun rewriteMemory(originalText: String): Result<String> = withContext(Dispatchers.Default) {
        val inference = llmInference
            ?: return@withContext Result.failure(IllegalStateException("Local AI model is not initialized."))

        if (originalText.isBlank()) return@withContext Result.success(originalText)

        val prompt = buildString {
            append("<start_of_turn>user\n")
            append("You are an on-device personal memory assistant. Improve this memory note according to these strict rules:\n")
            append("1. Correct any spelling, typing, and grammatical errors.\n")
            append("2. Improve sentence structure and readability.\n")
            append("3. Strictly preserve the original meaning, facts, and events.\n")
            append("4. Preserve all names, dates, numbers, places, and relationships exactly as written.\n")
            append("5. Preserve the author's emotional tone.\n")
            append("6. Do NOT invent details, add events, or speculate.\n")
            append("7. Do NOT change positive experiences into negative experiences or vice versa.\n")
            append("8. Do NOT make the text unnecessarily longer.\n")
            append("9. Output ONLY the rewritten memory text without commentary or preamble.\n\n")
            append("Original memory:\n")
            append(originalText.trim())
            append("\n<end_of_turn>\n<start_of_turn>model\n")
        }

        try {
            val response = inference.generateResponse(prompt).trim()
            val sanitized = response
                .removePrefix("Here is the rewritten memory:")
                .removePrefix("Rewritten memory:")
                .trim()
            Result.success(sanitized)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * AI Feature 2: Contact Summary with Context Window Batching
     */
    suspend fun summarizeContact(
        contactName: String,
        memories: List<CloudMemory>
    ): Result<String> = withContext(Dispatchers.Default) {
        val inference = llmInference
            ?: return@withContext Result.failure(IllegalStateException("Local AI model is not initialized."))

        if (memories.isEmpty()) return@withContext Result.success("No memories recorded yet for $contactName.")

        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        val processedContext = if (memories.size > 8) {
            batchSummarizeMemories(contactName, memories, dateFormat)
        } else {
            memories.joinToString("\n---\n") { mem ->
                val dateStr = dateFormat.format(Date(mem.createdAt))
                "• Date: $dateStr | Category: ${mem.category}\n  Title: ${mem.title}\n  Details: ${mem.content}"
            }
        }

        val prompt = buildString {
            append("<start_of_turn>user\n")
            append("You are an on-device personal relationship memory assistant. Generate a concise, factual, and balanced summary of the user's recorded experiences with $contactName based ONLY on these memories.\n\n")
            append("Rules:\n")
            append("1. Use ONLY the facts provided in the memories. Never invent conversations, events, emotions, or intentions.\n")
            append("2. Never make unsupported psychological judgments about $contactName.\n")
            append("3. Distinguish between a single isolated incident and recurring patterns.\n")
            append("4. Cover milestones, positive experiences, disagreements, and overall nature.\n")
            append("5. Keep the summary concise, balanced, and empathetic.\n\n")
            append("Memories for $contactName:\n")
            append(processedContext)
            append("\n<end_of_turn>\n<start_of_turn>model\n")
        }

        try {
            val response = inference.generateResponse(prompt).trim()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun batchSummarizeMemories(
        contactName: String,
        memories: List<CloudMemory>,
        dateFormat: SimpleDateFormat
    ): String {
        val chunks = memories.chunked(6)
        val intermediateNotes = mutableListOf<String>()

        for ((index, chunk) in chunks.withIndex()) {
            val chunkText = chunk.joinToString("\n") { m ->
                val d = dateFormat.format(Date(m.createdAt))
                "[$d] (${m.category}) ${m.title}: ${m.content}"
            }
            val batchPrompt = "<start_of_turn>user\nExtract key factual events and sentiments with $contactName from batch ${index + 1}:\n$chunkText\n<end_of_turn>\n<start_of_turn>model\n"
            try {
                val intermediate = llmInference?.generateResponse(batchPrompt)?.trim()
                if (!intermediate.isNullOrBlank()) intermediateNotes.add(intermediate)
            } catch (e: Exception) {
                intermediateNotes.add(chunkText.take(500))
            }
        }
        return intermediateNotes.joinToString("\n\n")
    }

    fun release() {
        try {
            llmInference?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            llmInference = null
            isInitialized = false
        }
    }
}