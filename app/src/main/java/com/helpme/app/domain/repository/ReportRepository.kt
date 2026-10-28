package com.helpme.app.domain.repository

import com.helpme.app.domain.model.OperationResult
import com.helpme.app.domain.model.Report

interface ReportRepository {
    suspend fun createReport(report: Report): OperationResult
}
