package com.erikschouten.roothash

import com.erikschouten.roothash.leaf.SecureData

class RootHash private constructor(
    val leaves: List<Hashable>
) : Hashable {

    override fun sha3() = leaves.map { it.sha3() }.join()!!

    fun contains(hashable: Hashable) = contains(hashable.sha3())
    fun contains(sha3: ByteArray) = leaves.map { it.sha3() }.contains(sha3)

    fun findData(): List<Any> = findSecureData().map { it.get() }
    fun findSecureData(): List<SecureData> = leaves.filterIsInstance(SecureData::class.java)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RootHash

        if (leaves != other.leaves) return false

        return true
    }

    companion object {
        fun build(leaves: List<Hashable>) = RootHash(leaves.sorted())

    }
}

fun List<ByteArray>.join(): ByteArray? {
    if (this.isEmpty()) return null
    var byteArray = this[0]
    for (i in 1 until this.size) {
        byteArray += this[i]
    }
    return byteArray
}
