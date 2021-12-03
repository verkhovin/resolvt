package dev.ithurts.model.api

data class TechDebtReport(
    val title: String,
    val description: String,
    val remoteUrl: String,
    val filePath: String,
    val startLine: Int,
    val endLine:Int
)