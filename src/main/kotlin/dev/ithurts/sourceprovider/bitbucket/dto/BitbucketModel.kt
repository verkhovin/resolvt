package dev.ithurts.sourceprovider.bitbucket.dto

import com.fasterxml.jackson.annotation.JsonProperty

class Values<T>(val values: List<T>)

class BitbucketUserEmailInfo(val email: String, val isPrimary: Boolean)

class BitbucketWorkspace(val slug: String, val name: String)

data class Token(@JsonProperty("access_token") val accessToken: String)