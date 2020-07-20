package com.thoughtworks.archguard.module.domain.dependency

import com.thoughtworks.archguard.module.domain.model.Dependency
import com.thoughtworks.archguard.module.domain.model.JMethodVO

interface DependencyRepository {
    fun getAll(): List<Dependency<JMethodVO>>
}