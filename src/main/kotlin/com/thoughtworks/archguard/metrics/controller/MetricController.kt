package com.thoughtworks.archguard.metrics.controller

import com.thoughtworks.archguard.metrics.domain.MetricPersistService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/projects/{projectId}/metric")
class MetricController(val metricPersistService: MetricPersistService) {
    @PostMapping("/class/persist")
    fun persistClassMetrics(@PathVariable("projectId") projectId: Long) {
        return metricPersistService.persistClassMetrics(projectId)
    }
}