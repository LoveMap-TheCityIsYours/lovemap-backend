package com.lovemap.lovemapbackend.configuration

// Did not work out: com.google.api.gax.rpc.UnavailableException: io.grpc.StatusRuntimeException: UNAVAILABLE: Channel shutdown invoked

//@Configuration
//@Profile("!dev")
//class LoveMapGcpMonitoringConfig(
//    private val resourceLoader: ResourceLoader,
//    private val stackdriverProperties: StackdriverProperties
//) : GcpStackdriverPropertiesConfigAdapter(stackdriverProperties) {
//
//    @Bean
//    fun stackdriverConfig(credentialsProvider: CredentialsProvider): StackdriverConfig {
//        return object : StackdriverConfig {
//            override fun projectId(): String = stackdriverProperties.projectId
//
//            override fun batchSize(): Int {
//                return 199
//            }
//
//            override fun get(key: String): String? = null
//            override fun credentials(): CredentialsProvider {
//                return credentialsProvider
//            }
//        }
//    }
//
//    @Bean
//    fun meterRegistry(stackdriverConfig: StackdriverConfig): MeterRegistry {
//        return StackdriverMeterRegistry.builder(stackdriverConfig).build()
//    }
//
//    @Bean
//    override fun credentials(): CredentialsProvider {
//        return credentialsProvider()
//    }
//
//    @Bean
//    fun credentialsProvider(): CredentialsProvider = CredentialsProvider {
//        val inputStream = resourceLoader.getResource("classpath:monitoring-account-key.json").inputStream
//        ServiceAccountCredentials.fromStream(inputStream)
//    }
//
//    override fun projectId(): String {
//        return stackdriverProperties.projectId
//    }
//}
//
//@Configuration
//@Profile("dev")
//@EnableAutoConfiguration(
//    exclude = [StackdriverMetricsExportAutoConfiguration::class]
//)
//class DevMonitoringConfig {
//}
