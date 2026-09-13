package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PulseDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, id ASC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestampMillis ASC")
    fun getMessagesForConversation(convId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("UPDATE messages SET isPinned = :isPinned WHERE id = :messageId")
    suspend fun setPinned(messageId: String, isPinned: Boolean)

    @Query("UPDATE messages SET content = :newContent, isEdited = 1 WHERE id = :messageId")
    suspend fun editMessage(messageId: String, newContent: String)

    @Query("UPDATE messages SET reactionsRaw = :reactionsRaw WHERE id = :messageId")
    suspend fun updateReactions(messageId: String, reactionsRaw: String)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :convId")
    suspend fun markConversationAsRead(convId: String)

    @Query("SELECT COUNT(*) FROM conversations")
    suspend fun getConversationCount(): Int
}
