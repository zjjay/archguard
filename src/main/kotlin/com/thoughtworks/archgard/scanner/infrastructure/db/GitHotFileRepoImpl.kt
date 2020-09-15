package com.thoughtworks.archgard.scanner.infrastructure.db

import com.thoughtworks.archgard.scanner.domain.scanner.git.GitHotFile
import com.thoughtworks.archgard.scanner.domain.scanner.git.GitHotFileRepo
import org.springframework.stereotype.Repository

@Repository
class GitHotFileRepoImpl(val gitHotFileDao: GitHotFileDao) : GitHotFileRepo {

    override fun save(gitHotFiles: List<GitHotFile>) {
        gitHotFileDao.deleteBySystemId(gitHotFiles[0].systemId)
        gitHotFileDao.saveAll(gitHotFiles)
    }

    override fun deleteBySystemId(systemId: Long) {
        TODO("Not yet implemented")
    }

    override fun findBySystemId(systemId: Long): List<GitHotFile> {
        return gitHotFileDao.findBySystemId(systemId)
    }
}
