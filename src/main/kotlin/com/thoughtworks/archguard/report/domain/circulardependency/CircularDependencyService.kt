package com.thoughtworks.archguard.report.domain.circulardependency

import com.thoughtworks.archguard.report.exception.WrongLimitException
import com.thoughtworks.archguard.report.exception.WrongOffsetException
import org.springframework.stereotype.Service

@Service
class CircularDependencyService(val circularDependencyRepository: CircularDependencyRepository) {
    private fun getCircularDependencyWithTotalCount(systemId: Long, limit: Long, offset: Long, type: CircularDependencyType): CircularDependencyStringListDto {
        validPagingParam(limit, offset)
        val circularDependencyCount = circularDependencyRepository.getCircularDependencyCount(systemId, type)
        val circularDependencyList = circularDependencyRepository.getCircularDependency(systemId, type, limit, offset)
        return CircularDependencyStringListDto(circularDependencyList,
                circularDependencyCount,
                offset / limit + 1)
    }

    fun getModuleCircularDependencyWithTotalCount(systemId: Long, limit: Long, offset: Long): CircularDependencyListDto<ModuleVO> {
        val circularDependencyWithTotalCount = getCircularDependencyWithTotalCount(systemId, limit, offset, CircularDependencyType.MODULE)
        val data = circularDependencyWithTotalCount.data.map { CircularDependency(it.split(";").map { ModuleVO(it) }) }
        return CircularDependencyListDto(data, circularDependencyWithTotalCount.count, circularDependencyWithTotalCount.currentPageNumber)
    }

    private fun validPagingParam(limit: Long, offset: Long) {
        if (limit <= 0) {
            throw WrongLimitException("limit $limit is smaller than 1")
        }
        if (offset < 0) {
            throw WrongOffsetException("offset $offset is smaller than 0")
        }
    }

    fun getPackageCircularDependencyWithTotalCount(systemId: Long, limit: Long, offset: Long): CircularDependencyListDto<PackageVO> {
        val circularDependencyWithTotalCount = getCircularDependencyWithTotalCount(systemId, limit, offset, CircularDependencyType.PACKAGE)
        val data = circularDependencyWithTotalCount.data.map { CircularDependency(it.split(";").map { PackageVO.create(it) }) }
        return CircularDependencyListDto(data, circularDependencyWithTotalCount.count, circularDependencyWithTotalCount.currentPageNumber)
    }

    fun getClassCircularDependencyWithTotalCount(systemId: Long, limit: Long, offset: Long): CircularDependencyListDto<ClassVO> {
        val circularDependencyWithTotalCount = getCircularDependencyWithTotalCount(systemId, limit, offset, CircularDependencyType.CLASS)
        val data = circularDependencyWithTotalCount.data.map { CircularDependency(it.split(";").map { ClassVO.create(it) }) }
        return CircularDependencyListDto(data, circularDependencyWithTotalCount.count, circularDependencyWithTotalCount.currentPageNumber)
    }

    fun getMethodCircularDependencyWithTotalCount(systemId: Long, limit: Long, offset: Long): CircularDependencyListDto<MethodVO> {
        val circularDependencyWithTotalCount = getCircularDependencyWithTotalCount(systemId, limit, offset, CircularDependencyType.METHOD)
        val data = circularDependencyWithTotalCount.data.map { CircularDependency(it.split(";").map { MethodVO.create(it) }) }
        return CircularDependencyListDto(data, circularDependencyWithTotalCount.count, circularDependencyWithTotalCount.currentPageNumber)
    }
}