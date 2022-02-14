package dev.ithurts.application.service.codechange


fun trimDiffFilepath(filePath: String) =
    filePath.substringAfter("/").substringBefore(" ")