package dev.alexrincon.aichat.ui.screens.chat

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.alexrincon.aichat.data.datasource.OpenAIDataSource
import dev.alexrincon.aichat.data.local.entity.ConversationEntity
import dev.alexrincon.aichat.data.local.entity.MessageEntity
import dev.alexrincon.aichat.data.model.ChatMessage
import dev.alexrincon.aichat.data.model.MessageRole
import dev.alexrincon.aichat.data.local.dao.ConversationDao
import dev.alexrincon.aichat.data.local.dao.MessageDao
import dev.alexrincon.aichat.data.repository.ChatRepository
import dev.alexrincon.aichat.ui.theme.AiChatTheme
import dev.alexrincon.aichat.util.StringProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicLong

@RunWith(AndroidJUnit4::class)
class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class InMemoryConversationDao : ConversationDao {
        private val nextId = AtomicLong(1L)
        private val conversations = MutableStateFlow<List<ConversationEntity>>(emptyList())

        override suspend fun insertConversation(conversation: ConversationEntity): Long {
            val id = nextId.getAndIncrement()
            val toInsert = conversation.copy(id = id)
            conversations.value = listOf(toInsert) + conversations.value
            return id
        }

        override suspend fun updateConversation(conversation: ConversationEntity) {
            conversations.value = conversations.value.map { if (it.id == conversation.id) conversation else it }
        }

        override fun getAllConversations(): Flow<List<ConversationEntity>> = conversations

        override fun getConversationById(conversationId: Long): Flow<ConversationEntity?> =
            flowOf(conversations.value.find { c -> c.id == conversationId })

        override suspend fun deleteConversation(conversationId: Long) {
            conversations.value = conversations.value.filterNot { it.id == conversationId }
        }

        override suspend fun getConversationByIdSync(conversationId: Long): ConversationEntity? =
            conversations.value.find { it.id == conversationId }

        override suspend fun getLastConversation(): ConversationEntity? = conversations.value.firstOrNull()
    }

    private class InMemoryMessageDao : MessageDao {
        private val storage = mutableMapOf<Long, MutableList<MessageEntity>>()
        private val flows = mutableMapOf<Long, MutableStateFlow<List<MessageEntity>>>()

        private fun stateFlowFor(conversationId: Long): MutableStateFlow<List<MessageEntity>> {
            return flows.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
        }

        override suspend fun insertMessage(message: MessageEntity) {
            val conv = message.conversationId
            val list = storage.getOrPut(conv) { mutableListOf() }
            list.add(message)
            stateFlowFor(conv).value = list.toList()
        }

        override suspend fun insertMessages(messages: List<MessageEntity>) {
            messages.groupBy { it.conversationId }.forEach { (conv, list) ->
                val storageList = storage.getOrPut(conv) { mutableListOf() }
                storageList.addAll(list)
                stateFlowFor(conv).value = storageList.toList()
            }
        }

        override fun getMessagesByConversationId(conversationId: Long): Flow<List<MessageEntity>> =
            stateFlowFor(conversationId)

        override suspend fun deleteMessagesByConversationId(conversationId: Long) {
            storage.remove(conversationId)
            flows[conversationId]?.value = emptyList()
        }

        override suspend fun getMessagesByConversationIdSync(conversationId: Long): List<MessageEntity> =
            storage[conversationId]?.toList() ?: emptyList()
    }

    private fun fakeOpenAiDataSource(): OpenAIDataSource {
        val openAI = mockk<OpenAIDataSource>(relaxed = true)
        coEvery { openAI.getChatCompletion(any()) } answers {
            val messages = firstArg<List<ChatMessage>>()
            "Assistant reply to: ${messages.lastOrNull { it.role == MessageRole.USER }?.content ?: ""}"
        }
        coEvery { openAI.generateConversationTitle(any()) } answers {
            firstArg<String>().take(6)
        }
        return openAI
    }

    private fun newViewModel(): ChatViewModel {
        // Application context: safe before setContent() (composeTestRule.activity is not ready yet)
        val context: Context = ApplicationProvider.getApplicationContext()
        val stringProvider = StringProvider(context)
        val repo = ChatRepository(
            openAIDataSource = fakeOpenAiDataSource(),
            conversationDao = InMemoryConversationDao(),
            messageDao = InMemoryMessageDao(),
            stringProvider = stringProvider,
        )
        return ChatViewModel(repo)
    }

    @Test
    fun chatScreen_showsTitleAndEmptyState() {
        val viewModel = newViewModel()

        composeTestRule.setContent {
            AiChatTheme {
                ChatScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("AI Chat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Send a message to start chatting").assertIsDisplayed()
    }

    @Test
    fun chatScreen_sendMessage_showsUserAndAssistantMessages() {
        val viewModel = newViewModel()

        composeTestRule.setContent {
            AiChatTheme {
                ChatScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Write a message…").performTextInput("Hello Compose")

        composeTestRule.onNodeWithContentDescription("Send message").performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Hello Compose").assertIsDisplayed()
        composeTestRule.onNodeWithText("Assistant reply to: Hello Compose").assertIsDisplayed()
    }
}
