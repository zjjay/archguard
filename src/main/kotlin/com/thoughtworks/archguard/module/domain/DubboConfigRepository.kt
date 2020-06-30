package com.thoughtworks.archguard.module.domain

interface DubboConfigRepository {
    fun getReferenceConfigBy(interfaceName: String, module: SubModule): List<ReferenceConfig>
    fun getModuleByName(name: String): SubModule
    fun getServiceConfigBy(referenceConfig: ReferenceConfig): List<ServiceConfig>
}