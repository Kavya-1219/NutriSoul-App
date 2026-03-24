package com.simats.nutrisoul

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.network.ChatRequest
import com.simats.nutrisoul.data.network.NutriSoulApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
)

@HiltViewModel
class HelpSupportViewModel @Inject constructor(
    private val apiService: NutriSoulApiService
) : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>(
        ChatMessage("Hi! I'm NutriSoul AI. How can I help you today?", false)
    )
    val messages: List<ChatMessage> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var initialMessageSet = false

    fun setInitialMessage(text: String) {
        if (initialMessageSet || text.isBlank()) return
        initialMessageSet = true
        sendMessage(text)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text, true)
        _messages.add(userMessage)
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getAiAssistantResponse(ChatRequest(text))
                _messages.add(ChatMessage(response.response, false))
            } catch (e: Exception) {
                _messages.add(ChatMessage("Sorry, I'm having trouble connecting to my servers. Please try again later.", false))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
