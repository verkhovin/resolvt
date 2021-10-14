package dev.ithurts.sourceprovider.bitbucket.dto

class Values<T>(val values: List<T>)

class BitbucketUserEmailInfo(val email: String, val isPrimary: Boolean)

class BitbucketWorkspace(val slug: String, val name: String)