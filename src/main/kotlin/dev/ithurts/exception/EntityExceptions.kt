package dev.ithurts.exception

class EntityNotFoundException(private val name: String, private val field: String, private val value: String) :
    Exception(
        "$name not found"
    )