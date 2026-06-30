package com.example.data.repository

import com.example.data.dao.TransferHistoryDao
import com.example.data.model.TransferHistory
import kotlinx.coroutines.flow.Flow

class TransferHistoryRepository(private val transferHistoryDao: TransferHistoryDao) {
    val allHistory: Flow<List<TransferHistory>> = transferHistoryDao.getAllHistory()

    suspend fun insert(history: TransferHistory): Long {
        return transferHistoryDao.insertHistory(history)
    }

    suspend fun update(history: TransferHistory) {
        transferHistoryDao.updateHistory(history)
    }

    suspend fun delete(history: TransferHistory) {
        transferHistoryDao.deleteHistory(history)
    }

    suspend fun clearAll() {
        transferHistoryDao.clearHistory()
    }
}
