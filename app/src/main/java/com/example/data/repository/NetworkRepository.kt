package com.example.data.repository

import com.example.data.database.NetworkHistoryDao
import com.example.data.database.NetworkHistoryEntity
import kotlinx.coroutines.flow.Flow

class NetworkRepository(private val networkHistoryDao: NetworkHistoryDao) {
    val allHistory: Flow<List<NetworkHistoryEntity>> = networkHistoryDao.getAllHistory()

    suspend fun insert(entity: NetworkHistoryEntity) {
        networkHistoryDao.insertHistory(entity)
    }

    suspend fun deleteItem(id: Int) {
        networkHistoryDao.deleteHistoryItem(id)
    }

    suspend fun clearAllHistory() {
        networkHistoryDao.clearAllHistory()
    }
}
