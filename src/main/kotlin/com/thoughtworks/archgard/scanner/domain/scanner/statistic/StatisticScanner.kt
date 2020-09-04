package com.thoughtworks.archgard.scanner.domain.scanner.statistic

import com.thoughtworks.archgard.scanner.domain.ScanContext
import com.thoughtworks.archgard.scanner.domain.config.model.ToolConfigure
import com.thoughtworks.archgard.scanner.domain.scanner.Scanner
import com.thoughtworks.archgard.scanner.domain.tools.DesigniteJavaReportType
import com.thoughtworks.archgard.scanner.domain.tools.DesigniteJavaTool
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class StatisticScanner(@Autowired val classClassStatisticRepo: ClassStatisticRepo,
                       @Autowired val methodClassStatisticRepo: MethodStatisticRepo) : Scanner {

    private val log = LoggerFactory.getLogger(StatisticScanner::class.java)
    override fun getScannerName(): String {
        return "Statistic"
    }

    override fun scan(context: ScanContext) {
        log.info("start scan statistic report, workspcase: {}", context.workspace)
        val (classStatistics, methodStatistic) = generateStatistic(context)
        classClassStatisticRepo.save(classStatistics)
        methodClassStatisticRepo.save(methodStatistic)
        log.info("finished scan statistic report")
    }

    private fun generateStatistic(context: ScanContext): Pair<List<ClassStatistic>, List<MethodStatistic>> {
        val designiteJavaTool = DesigniteJavaTool(context.workspace)
        val currentDirectionName = context.workspace.path.substring(context.workspace.path.lastIndexOf("/") + 1)
        val classStatistics = designiteJavaTool.readReport(DesigniteJavaReportType.TYPE_METRICS).map { toClassStatistic(it, currentDirectionName, context.systemId) }
        val methodStatistic = designiteJavaTool.readReport(DesigniteJavaReportType.METHOD_METRICS).map { toMethodStatistic(it, currentDirectionName, context.systemId) }
        return Pair(classStatistics, methodStatistic)
    }

    private fun toClassStatistic(line: String, currentDirectionName: String, systemId: Long): ClassStatistic {
        val elements = line.split(",")
        val moduleName = if (currentDirectionName == elements[0]) null else elements[0]
        return ClassStatistic(UUID.randomUUID().toString(), systemId, moduleName, elements[1],
                elements[2], elements[7].toInt(), elements[12].toInt(), elements[13].toInt())
    }

    private fun toMethodStatistic(line: String, currentDirectionName: String, systemId: Long): MethodStatistic {
        val elements = line.split(",")
        val moduleName = if (currentDirectionName == elements[0]) null else elements[0]
        return MethodStatistic(UUID.randomUUID().toString(), systemId, moduleName, elements[1],
                elements[2], elements[3], elements[4].toInt())
    }
}