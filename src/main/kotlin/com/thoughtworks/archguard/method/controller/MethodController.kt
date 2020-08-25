package com.thoughtworks.archguard.method.controller

import com.thoughtworks.archguard.method.domain.JMethod
import com.thoughtworks.archguard.method.domain.MethodService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/projects/{projectId}/methods")
class MethodController {
    @Autowired
    private lateinit var methodService: MethodService

    @GetMapping("/{name}/callees")
    fun getMethodCallees(@PathVariable("projectId") projectId: Long,
                         @PathVariable("name") methodName: String,
                         @RequestParam(value = "clazz") clazzName: String,
                         @RequestParam(value = "deep", required = false, defaultValue = "3") deep: Int,
                         @RequestParam(value = "needIncludeImpl", required = false, defaultValue = "true") needIncludeImpl: Boolean,
                         @RequestParam(value = "module", required = false, defaultValue = "") moduleName: String): ResponseEntity<List<JMethod>> {
        val jMethod = methodService.findMethodCallees(projectId, moduleName, clazzName, methodName, deep, needIncludeImpl)
        return ResponseEntity.ok(jMethod)
    }

    @GetMapping("/{name}/callers")
    fun getMethodCallees(@PathVariable("projectId") projectId: Long,
                         @PathVariable("name") methodName: String,
                         @RequestParam(value = "clazz") clazzName: String,
                         @RequestParam(value = "deep", required = false, defaultValue = "3") deep: Int,
                         @RequestParam(value = "module", required = false, defaultValue = "") moduleName: String): ResponseEntity<List<JMethod>> {
        val jMethod = methodService.findMethodCallers(projectId, moduleName, clazzName, methodName, deep)
        return ResponseEntity.ok(jMethod)
    }

    @GetMapping("/{name}/invokes")
    fun getMethodCallees(@PathVariable("projectId") projectId: Long,
                         @PathVariable("name") methodName: String,
                         @RequestParam(value = "clazz") clazzName: String,
                         @RequestParam(value = "deep", required = false, defaultValue = "3") deep: Int,
                         @RequestParam(value = "callerDeep", required = false) callerDeep: Int?,
                         @RequestParam(value = "calleeDeep", required = false) calleeDeep: Int?,
                         @RequestParam(value = "needIncludeImpl", required = false, defaultValue = "true") needIncludeImpl: Boolean,
                         @RequestParam(value = "module", required = false, defaultValue = "") moduleName: String): ResponseEntity<List<JMethod>> {
        val jMethod = methodService.findMethodInvokes(projectId, moduleName, clazzName, methodName, callerDeep ?: deep, calleeDeep
                ?: deep, needIncludeImpl)
        return ResponseEntity.ok(jMethod)
    }
}
