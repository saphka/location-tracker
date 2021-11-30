package org.saphka.location.tracker.user.model

data class User(val id: Int, val alias: String, val publicKey: ByteArray, val passwordHash: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (alias != other.alias) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + alias.hashCode()
        return result
    }
}