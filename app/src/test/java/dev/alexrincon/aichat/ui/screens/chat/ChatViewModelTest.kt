package dev.alexrincon.aichat.ui.screens.chat

import dev.alexrincon.aichat.data.local.entity.ConversationEntity
import dev.alexrincon.aichat.data.model.ChatMessage
import dev.alexrincon.aichat.data.model.MessageRole
import dev.alexrincon.aichat.data.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun mockChatRepository(
        messages: MutableStateFlow<List<ChatMessage>> = MutableStateFlow(emptyList()),
        conversationId: MutableStateFlow<Long?> = MutableStateFlow(1L),
        conversations: MutableStateFlow<List<ConversationEntity>> = MutableStateFlow(emptyList()),
    ): ChatRepository {
        val repo = mockk<ChatRepository>(relaxed = true)
        every { repo.currentConversation } returns messages
        every { repo.currentConversationId } returns conversationId
        every { repo.allConversations } returns conversations
        coEvery { repo.initializeConversation() } returns Unit
        return repo
    }

    @Test
    fun `initializeConversation runs once on init`() = runTest(testDispatcher) {
        val repo = mockChatRepository()
        ChatViewModel(repo)
        advanceUntilIdle()
        coVerify(exactly = 1) { repo.initializeConversation() }
    }

    @Test
    fun `sendMessage with blank does not call repository`() = runTest(testDispatcher) {
        val repo = mockChatRepository()
        val vm = ChatViewModel(repo)

        vm.sendMessage("   ")
        advanceUntilIdle()

        coVerify(exactly = 0) { repo.sendMessage(any()) }
    }

    @Test
    fun `sendMessage success updates loading and clears error`() = runTest(testDispatcher) {
        val repo = mockChatRepository()
        coEvery { repo.sendMessage("hello") } returns Result.success(Unit)

        val vm = ChatViewModel(repo)
        val job = launch { vm.state.collect { } }

        advanceUntilIdle()

        vm.sendMessage("hello")
        advanceUntilIdle()

        job.cancel()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        coVerify { repo.sendMessage("hello") }
    }

    @Test
    fun `sendMessage failure sets error`() = runTest(testDispatcher) {
        val repo = mockChatRepository()
        coEvery { repo.sendMessage("boom") } returns Result.failure(Exception("boom"))

        val vm = ChatViewModel(repo)
        val job = launch { vm.state.collect { } }

        advanceUntilIdle()

        vm.sendMessage("boom")
        advanceUntilIdle()

        job.cancel()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertTrue(state.error?.contains("boom") == true)
        coVerify { repo.sendMessage("boom") }
    }

    @Test
    fun `switchToConversation calls repository`() = runTest(testDispatcher) {
        val repo = mockChatRepository()
        val vm = ChatViewModel(repo)

        advanceUntilIdle()

        vm.switchToConversation(42L)

        verify { repo.switchToConversation(42L) }
    }

    @Test
    fun `createNewConversation launches repository call`() = runTest(testDispatcher) {
        val repo = mockChatRepository()
        coEvery { repo.createNewConversationAndSwitch() } returns Unit

        val vm = ChatViewModel(repo)

        advanceUntilIdle()

        vm.createNewConversation()
        advanceUntilIdle()

        coVerify { repo.createNewConversationAndSwitch() }
    }

    @Test
    fun `deleteConversation launches repository call`() = runTest(testDispatcher) {
        val repo = mockChatRepository()
        coEvery { repo.deleteConversation(99L) } returns Unit

        val vm = ChatViewModel(repo)

        advanceUntilIdle()

        vm.deleteConversation(99L)
        advanceUntilIdle()

        coVerify { repo.deleteConversation(99L) }
    }

    @Test
    fun `state excludes system messages`() = runTest(testDispatcher) {
        val messages = MutableStateFlow(
            listOf(
                ChatMessage(content = "sys", role = MessageRole.SYSTEM),
                ChatMessage(content = "hi", role = MessageRole.USER),
            ),
        )
        val repo = mockChatRepository(messages = messages)
        val vm = ChatViewModel(repo)
        val job = launch { vm.state.collect { } }

        advanceUntilIdle()

        job.cancel()

        assertEquals(1, vm.state.value.messages.size)
        assertEquals("hi", vm.state.value.messages.single().content)
        assertEquals(MessageRole.USER, vm.state.value.messages.single().role)
    }
}
