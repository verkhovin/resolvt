package dev.ithurts.application.service.internal.git


fun trimDiffFilepath(filePath: String) =
    filePath.substringAfter("/").substringBefore(" ")