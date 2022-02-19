package dev.ithurts.application.internal.git


fun trimDiffFilepath(filePath: String) =
    filePath.substringAfter("/").substringBefore(" ")