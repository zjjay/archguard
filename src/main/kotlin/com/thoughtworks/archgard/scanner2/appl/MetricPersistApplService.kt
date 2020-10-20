package com.thoughtworks.archgard.scanner2.appl

import com.thoughtworks.archgard.scanner2.domain.model.CircularDependenciesCount
import com.thoughtworks.archgard.scanner2.domain.model.ClassMetric
import com.thoughtworks.archgard.scanner2.domain.model.JClass
import com.thoughtworks.archgard.scanner2.domain.model.MethodMetric
import com.thoughtworks.archgard.scanner2.domain.model.ModuleMetric
import com.thoughtworks.archgard.scanner2.domain.model.PackageMetric
import com.thoughtworks.archgard.scanner2.domain.repository.CircularDependencyMetricRepository
import com.thoughtworks.archgard.scanner2.domain.repository.ClassMetricRepository
import com.thoughtworks.archgard.scanner2.domain.repository.DataClassRepository
import com.thoughtworks.archgard.scanner2.domain.repository.JClassRepository
import com.thoughtworks.archgard.scanner2.domain.repository.JMethodRepository
import com.thoughtworks.archgard.scanner2.domain.repository.MethodMetricRepository
import com.thoughtworks.archgard.scanner2.domain.repository.ModuleMetricRepository
import com.thoughtworks.archgard.scanner2.domain.repository.PackageMetricRepository
import com.thoughtworks.archgard.scanner2.domain.service.CircularDependencyService
import com.thoughtworks.archgard.scanner2.domain.service.DataClassService
import com.thoughtworks.archgard.scanner2.domain.service.DitService
import com.thoughtworks.archgard.scanner2.domain.service.FanInFanOutService
import com.thoughtworks.archgard.scanner2.domain.service.LCOM4Service
import com.thoughtworks.archgard.scanner2.domain.service.NocService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class MetricPersistApplService(val ditService: DitService,
                               val lcoM4Service: LCOM4Service,
                               val nocService: NocService,
                               val dataClassService: DataClassService,
                               val jClassRepository: JClassRepository,
                               val jMethodRepository: JMethodRepository,
                               val fanInFanOutService: FanInFanOutService,
                               val circularDependencyService: CircularDependencyService,
                               val classMetricRepository: ClassMetricRepository,
                               val methodMetricRepository: MethodMetricRepository,
                               val packageMetricRepository: PackageMetricRepository,
                               val moduleMetricRepository: ModuleMetricRepository,
                               val dataClassRepository: DataClassRepository,
                               val circularDependencyMetricRepository: CircularDependencyMetricRepository) {
    private val log = LoggerFactory.getLogger(MetricPersistApplService::class.java)

    fun persistLevel2Metrics(systemId: Long) = runBlocking {

        log.info("**************************************************************************")
        log.info(" Begin calculate and persist Level 2 Metric in systemId $systemId")
        log.info("**************************************************************************")

        val jClasses = jClassRepository.getJClassesNotThirdPartyAndNotTest(systemId)

        val threadPool = newFixedThreadPoolContext(4, "level2_metrics")
        async(threadPool) { persistClassLevel2Metrics(systemId, jClasses) }.await()
        async(threadPool) { persistMethodLevel2Metrics(systemId) }.await()
        async(threadPool) { persistPackageLevel2Metrics(systemId, jClasses) }.await()
        async(threadPool) { persistModuleLevel2Metrics(systemId, jClasses) }.await()

    }

    @Transactional
    fun persistDataClass(systemId: Long) {
        val dataClasses = dataClassService.findAllDataClasses(systemId)
        dataClassRepository.insertOrUpdateDataClass(systemId, dataClasses)
        log.info("-----------------------------------------------------------------------")
        log.info("Finished persist data class Metric for systemId $systemId")
        log.info("-----------------------------------------------------------------------")
    }

    @Transactional
    fun persistCircularDependencyMetrics(systemId: Long) {
        val moduleCircularDependency = circularDependencyService.getModuleCircularDependency(systemId)
        circularDependencyMetricRepository.insertOrUpdateModuleCircularDependency(systemId, moduleCircularDependency)
        log.info("Finished persist moduleCircularDependency in systemId $systemId")

        val packageCircularDependency = circularDependencyService.getPackageCircularDependency(systemId)
        circularDependencyMetricRepository.insertOrUpdatePackageCircularDependency(systemId, packageCircularDependency)
        log.info("Finished persist packageCircularDependency in systemId $systemId")

        val classCircularDependency = circularDependencyService.getClassCircularDependency(systemId)
        circularDependencyMetricRepository.insertOrUpdateClassCircularDependency(systemId, classCircularDependency)
        log.info("Finished persist classCircularDependency in systemId $systemId")

        val methodCircularDependency = circularDependencyService.getMethodCircularDependency(systemId)
        circularDependencyMetricRepository.insertOrUpdateMethodCircularDependency(systemId, methodCircularDependency)
        log.info("Finished persist methodCircularDependency in systemId $systemId")

        val circularDependenciesCount = CircularDependenciesCount(systemId, moduleCircularDependency.size, packageCircularDependency.size, classCircularDependency.size, methodCircularDependency.size)
        log.info("-----------------------------------------------------------------------")
        log.info("Finished persist circularDependenciesCount for systemId $systemId")
        log.info("-----------------------------------------------------------------------")

    }

    private suspend fun persistModuleLevel2Metrics(systemId: Long, jClasses: List<JClass>) {
        val moduleFanInFanOutMap = fanInFanOutService.calculateAtModuleLevel(systemId, jClasses)
        val moduleMetrics = moduleFanInFanOutMap.map {
            ModuleMetric(systemId, it.key, it.value.fanIn, it.value.fanOut)
        }
        log.info("Finished calculate moduleMetric in systemId $systemId")

        moduleMetricRepository.insertOrUpdateModuleMetric(systemId, moduleMetrics)

        log.info("-----------------------------------------------------------------------")
        log.info("Finished persist module Metric to mysql for systemId $systemId")
        log.info("-----------------------------------------------------------------------")
    }

    private suspend fun persistPackageLevel2Metrics(systemId: Long, jClasses: List<JClass>) {
        val packageFanInFanOutMap = fanInFanOutService.calculateAtPackageLevel(systemId, jClasses)
        val packageMetrics = packageFanInFanOutMap.map {
            PackageMetric(systemId, getModuleNameFromPackageFullName(it.key), getPackageNameFromPackageFullName(it.key),
                    it.value.fanIn, it.value.fanOut)
        }
        log.info("Finished calculate packageMetric in systemId $systemId")
        packageMetricRepository.insertOrUpdatePackageMetric(systemId, packageMetrics)

        log.info("-----------------------------------------------------------------------")
        log.info("Finished persist package Metric to mysql for systemId $systemId")
        log.info("-----------------------------------------------------------------------")
    }

    private suspend fun persistMethodLevel2Metrics(systemId: Long) {
        val methods = jMethodRepository.getMethodsNotThirdPartyAndNotTest(systemId)
        val methodFanInFanOutMap = fanInFanOutService.calculateAtMethodLevel(systemId)

        val methodMetrics = methods.map {
            MethodMetric(systemId, it.toVO(),
                    methodFanInFanOutMap[it.id]?.fanIn ?: 0, methodFanInFanOutMap[it.id]?.fanOut ?: 0)
        }
        log.info("Finished calculate methodMetric in systemId $systemId")

        methodMetricRepository.insertOrUpdateMethodMetric(systemId, methodMetrics)

        log.info("-----------------------------------------------------------------------")
        log.info("Finished persist method Metric to mysql for in systemId $systemId")
        log.info("-----------------------------------------------------------------------")
    }

    private suspend fun persistClassLevel2Metrics(systemId: Long, jClasses: List<JClass>) = coroutineScope {
        val threadPool = newFixedThreadPoolContext(4, "class_metrics")
        val ditMap = async(threadPool) { ditService.calculate(systemId, jClasses) }
        val nocMap = async(threadPool) { nocService.calculate(systemId, jClasses) }
        val lcom4Map = async(threadPool) { lcoM4Service.calculate(systemId, jClasses) }
        val classFanInFanOutMap = async(threadPool) { fanInFanOutService.calculateAtClassLevel(systemId) }

        val classMetrics = jClasses.map {
            ClassMetric(systemId, it.toVO(), ditMap.await()[it.id], nocMap.await()[it.id], lcom4Map.await()[it.id],
                    classFanInFanOutMap.await()[it.id]?.fanIn ?: 0,
                    classFanInFanOutMap.await()[it.id]?.fanOut ?: 0)
        }
        classMetricRepository.insertOrUpdateClassMetric(systemId, classMetrics)

        log.info("-----------------------------------------------------------------------")
        log.info("Finished persist class Metric to mysql for systemId $systemId")
        log.info("-----------------------------------------------------------------------")
    }
}
