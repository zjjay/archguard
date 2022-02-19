package com.thoughtworks.archguard.report_bak.infrastructure

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class ClassStatisticRepo(@Autowired val jdbi: Jdbi) {

    fun getCodeLinesCount(): Int {
        return jdbi.withHandle<Int, Nothing> {
            it
                    .createQuery("select sum(`lines`) from ClassStatistic where 1 = 1")
                    .mapTo(Int::class.java)
                    .first()
        }
    }

    fun getModuleFanInFanOut(): List<FanInOutDBO> {
        return jdbi.withHandle<List<FanInOutDBO>, Nothing> {
            it.registerRowMapper(ConstructorMapper.factory(FanInOutDBO::class.java))
            it.createQuery(" select packageName, sum(fanin) as fanin,sum(fanout) as fanout " +
                    "from ClassStatistic " +
                    "group by packageName " +
                    "order by packageName")
                    .mapTo(FanInOutDBO::class.java)
                    .list()
        }
    }

}