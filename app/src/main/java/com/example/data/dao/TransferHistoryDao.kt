package com.example.data.dao

import androidx.room.*
import com.example.data.model.TransferHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferHistoryDao {
    @Query("SELECT * FROM transfer_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<TransferHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: TransferHistory): Long

    @Update
    suspend fun updateHistory(history: TransferHistory)

    @Delete
    suspend fun deleteHistory(history: TransferHistory)

    @Query("DELETE FROM transfer_history")
    suspend fun clearHistory()
}
