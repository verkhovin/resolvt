package dev.ithurts.sourceprovider.bitbucket.model

class BitbucketUserEmails(val values: List<BitbucketUserEmailInfo>)

class BitbucketUserEmailInfo(val email: String, val isPrimary: Boolean)