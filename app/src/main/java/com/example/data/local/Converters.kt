package com.example.data.local

import androidx.room.TypeConverter
import com.example.utils.toSafeBigDecimal
import java.math.BigDecimal

class Converters {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String {
        return value?.toPlainString() ?: "0.00"
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal {
        return value.toSafeBigDecimal(BigDecimal.ZERO)
    }
}
