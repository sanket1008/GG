package com.unc.gearupvr.model

data class RangeLimit(val min: Float, val max: Float) {
    override fun toString(): String {
        return StringBuilder().append(min.toInt()).append(",")
            .append(max.toInt())
            .toString()
    }

    fun toHashMap(): HashMap<String, Int> {
        return hashMapOf("max" to max.toInt(), "min" to min.toInt())

    }
}