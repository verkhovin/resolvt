package dev.ithurts.application.service.diff


fun trimDiffFilepath(filePath: String) =
    filePath.substringAfter("/").substringBefore(" ")