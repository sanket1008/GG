package com.unc.gearupvr.utils

import com.unc.gearupvr.model.StudentCapacity

class FilterConstants {
    companion object {
        const val inState = "in_state"
        const val outState = "out_state"
        const val satMin = 400f
        const val satMax = 1600f
        const val actMin = 1f
        const val actMax = 36f
        const val costMIN = 500f
        const val costMAX = 70000f

        enum class StudentBody {
            OptionLt5000,
            Option5000Bw10000,
            Option10000Bw15000,
            OptionGt15000;

            internal var displayName: String
                get() {
                    return when (this) {
                        OptionLt5000 -> "Below 5,000"
                        Option5000Bw10000 -> "5,000-10,000"
                        Option10000Bw15000 -> "10,001-15,000"
                        OptionGt15000 -> "Above 15,000"
                    }
                }
                set(_) {}

            internal var value: StudentCapacity
                get() {
                    return when (this) {
                        OptionLt5000 -> StudentCapacity("lt", 5000, 0)
                        Option5000Bw10000 -> StudentCapacity("bt", 5000, 10000)
                        Option10000Bw15000 -> StudentCapacity("bt", 10001, 15000)
                        OptionGt15000 -> StudentCapacity("gt", 15001, 0)
                    }
                }
                set(_) {}
        }

    }
}
