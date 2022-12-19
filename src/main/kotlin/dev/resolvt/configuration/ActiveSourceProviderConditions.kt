package dev.resolvt.configuration

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata

class GithubCondition: Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return context.environment.getRequiredProperty("resolvt.source-providers.enabled", List::class.java)
            .contains("github")
    }
}

class BitbucketCondition: Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return context.environment.getRequiredProperty("resolvt.source-providers.enabled", List::class.java)
            .contains("bitbucket")
    }
}

@Conditional(GithubCondition::class)
annotation class Github

@Conditional(BitbucketCondition::class)
annotation class Bitbucket