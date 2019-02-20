package com.erikschouten.roothash.leaf

import com.erikschouten.roothash.Hashable
import org.bouncycastle.util.encoders.Base64

data class Hash(
    val value: String
) : Hashable {

    constructor(bytes: ByteArray) : this(Base64.toBase64String(bytes))
    constructor(hashable: Hashable) : this(hashable.sha3())

    override fun sha3() = Base64.decode(value)!!
}
