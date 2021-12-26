package dev.ithurts.controller.api.dto

data class TechDebtReport(
    val title: String,
    val description: String,
    val remoteUrl: String,
    val filePath: String,
    val startLine: Int,
    val endLine:Int
)