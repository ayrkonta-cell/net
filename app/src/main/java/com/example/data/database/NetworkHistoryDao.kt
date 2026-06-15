package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkHistoryDao {
    @Query("SELECT * FROM network_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<NetworkHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: NetworkHistoryEntity)

    @Query("DELETE FROM network_history")
    suspend fun clearAllHistory()

    @Query("DELETE FROM network_history WHERE id = :id")
    suspend fun deleteHistoryItem(id: Int)
}
