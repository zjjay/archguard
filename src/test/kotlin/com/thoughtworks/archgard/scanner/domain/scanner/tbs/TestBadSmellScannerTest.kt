package com.thoughtworks.archgard.scanner.domain.scanner.tbs

import com.thoughtworks.archgard.scanner.domain.ScanContext
import com.thoughtworks.archgard.scanner.domain.system.BuildTool
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File

@SpringBootTest
@ActiveProfiles("test")
internal class TestBadSmellScannerTest(@Autowired val testBadSmellScanner: TestBadSmellScanner, @Autowired val jdbi: Jdbi) {

    @Test
    fun should_get_test_bad_smell_report() {
        val scanContext = ScanContext(1, "repo", BuildTool.GRADLE, File(javaClass.classLoader.getResource("TestBadSmell").toURI()), "",ArrayList())
        testBadSmellScanner.scan(scanContext)

        val testBadSmells = jdbi.withHandle<List<TestBadSmell>, RuntimeException> { handle: Handle ->
            handle.createQuery("select * from testBadSmell")
                    .mapTo(TestBadSmell::class.java).list()
        }
        assertEquals(testBadSmells.size, 12)
        assertEquals(testBadSmells[0].systemId, 1)

        val testCount = jdbi.withHandle<Int, RuntimeException> { handle: Handle ->
            handle.createQuery("select overview_value from overview where overview_type='test'")
                    .mapTo(Int::class.java).one()
        }

        assertEquals(testCount, 1)

    }

}
