package com.thoughtworks.archguard.dependence.infrastructure.logic_module

import com.thoughtworks.archguard.dependence.domain.logic_module.LogicModule
import com.thoughtworks.archguard.dependence.domain.logic_module.LogicModuleRepository
import com.thoughtworks.archguard.dependence.domain.logic_module.LogicModuleStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.web.WebAppConfiguration


@SpringBootTest
@WebAppConfiguration
internal class LogicModuleRepositoryImplTest {

    @Autowired
    lateinit var logicModuleRepository: LogicModuleRepository

    @Test
    internal fun `should only select normal data from logic module`() {
        val normalLogicModules = logicModuleRepository.getAll()
        assertThat(normalLogicModules.size).isEqualTo(1)
        assertThat(normalLogicModules[0]).isEqualTo(
                LogicModule("id1", "dubbo-provider", listOf("dubbo-provider"), LogicModuleStatus.NORMAL))
    }
}