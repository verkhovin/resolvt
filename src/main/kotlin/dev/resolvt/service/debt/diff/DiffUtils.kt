package dev.resolvt.application.service.internal.diff


fun trimDiffFilepath(filePath: String) =
    filePath.substringAfter("/").substringBefore(" ")