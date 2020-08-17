package com.thoughtworks.archguard.module.domain.model

/**
 * JClassVO is a Value Object, use for LogicModule aggregation
 */
class JClassVO(val name: String, val module: String) : LogicComponent() {
    var id: String? = null
    override fun containsOrEquals(logicComponent: LogicComponent): Boolean {
        return logicComponent.getType() == ModuleMemberType.CLASS && logicComponent.getFullName() == this.getFullName()
    }

    companion object {
        fun create(fullName: String): JClassVO {
            val split = fullName.split(".")
            return JClassVO(split.subList(1, split.size).joinToString(".").trim(), split[0].trim())
        }
    }

    override fun getFullName(): String {
        return "$module.$name"
    }

    override fun getType(): ModuleMemberType {
        return ModuleMemberType.CLASS
    }

    override fun toString(): String {
        return "JClassVO(name='$name', module='$module')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JClassVO

        if (name != other.name) return false
        if (module != other.module) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + module.hashCode()
        return result
    }


}
