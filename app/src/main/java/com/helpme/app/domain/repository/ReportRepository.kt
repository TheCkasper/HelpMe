package com.helpme.app.domain.repository

import com.helpme.app.domain.model.OperationResult
import com.helpme.app.domain.model.Report
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    suspend fun createReport(report: Report): OperationResult
    fun observeReports(): Flow<List<Report>>
    suspend fun getReportById(reportId: String): Report?
    suspend fun updateReportStatus(reportId: String, newStatus: String): OperationResult
    suspend fun deleteReport(reportId: String): OperationResult
    fun observeUserReports(userId: String): Flow<List<Report>>
}
