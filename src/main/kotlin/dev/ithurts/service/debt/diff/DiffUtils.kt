package dev.ithurts.application.service.internal.diff


fun trimDiffFilepath(filePath: String) =
    filePath.substringAfter("/").substringBefore(" ")