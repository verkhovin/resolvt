package dev.ithurts.application.sourceprovider.bitbucket.dto

import com.fasterxml.jackson.annotation.JsonProperty

class Values<T>(val values: List<T>)

class BitbucketUserEmailInfo(val email: String, val isPrimary: Boolean)

class BitbucketWorkspace(val slug: String, val name: String)

data class Token(@JsonProperty("access_token") val accessToken: String)

data class BitbucketRepository(
    val name: String,
    val mainbranch: BitbucketBranch
)

data class BitbucketBranch(
    val name: String
)