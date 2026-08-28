package com.allenrogers6.luzie.data

import java.security.MessageDigest

object Hasher {

    fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")

        return digest
            .digest(pin.toByteArray(Charsets.UTF_8))
            .joinToString("") {
                "%02x".format(it)
            }
    }

    fun verify(
        pin: String,
        expectedHash: String,
    ): Boolean {
        return hash(pin) == expectedHash
    }
}
