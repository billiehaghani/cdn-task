package com.bitmovin.platform.challenge.client.aws.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.costexplorer.CostExplorerClient
import java.net.URI

@Configuration
class AwsClientConfig {
    @Bean
    fun cloudWatch(): CloudWatchClient {
        return CloudWatchClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")))
            .endpointOverride(URI.create("http://localhost:4566"))
            .build()
    }

    @Bean
    fun costExplorer(): CostExplorerClient {
        return CostExplorerClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")))
            .endpointOverride(URI.create("http://localhost:4566"))
            .build()
    }
}
