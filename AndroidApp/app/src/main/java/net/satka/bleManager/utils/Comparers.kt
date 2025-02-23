package net.satka.bleManager.utils

import java.util.UUID

object Comparers {
    fun compareUUIDWithMask(uuid: UUID, mask: String?): Boolean {
        if (mask != null) {
            val regexPattern = mask.replace("#", ".")
            val regex = Regex(regexPattern, RegexOption.IGNORE_CASE)
            val uuidString = uuid.toString().replace("-", "")
            return regex.matches(uuidString)
        }

        return false
    }
}