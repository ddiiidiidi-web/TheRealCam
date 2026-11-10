package com.therealcam.sensors

/**
 * "Phone is fixed" with hysteresis, so wind or a light tripod wobbling around the threshold does not
 * flip the status several times a second. Becomes steady after [ENTER_HOLD_NS] below [SensorHub.STEADY_RAD_S];
 * stops being steady at once above [EXIT_RAD_S] (picked up, bumped), or after [EXIT_HOLD_NS] of continuous
 * motion between the two thresholds.
 */
class SteadyFilter {
    var steady = false
        private set
    private var sinceNs: Long? = null

    fun update(rms: Double, nowNs: Long): Boolean {
        if (!rms.isFinite()) {
            steady = false
            sinceNs = null
            return steady
        }
        if (steady) {
            when {
                rms >= EXIT_RAD_S -> { steady = false; sinceNs = null }
                rms >= SensorHub.STEADY_RAD_S -> {
                    val t = sinceNs ?: nowNs.also { sinceNs = it }
                    if (nowNs - t >= EXIT_HOLD_NS) { steady = false; sinceNs = null }
                }
                else -> sinceNs = null
            }
        } else {
            if (rms < SensorHub.STEADY_RAD_S) {
                val t = sinceNs ?: nowNs.also { sinceNs = it }
                if (nowNs - t >= ENTER_HOLD_NS) { steady = true; sinceNs = null }
            } else {
                sinceNs = null
            }
        }
        return steady
    }

    companion object {
        /** Well above tripod wind shake, below a hand holding the phone. */
        const val EXIT_RAD_S = 0.03
        const val ENTER_HOLD_NS = 700_000_000L
        const val EXIT_HOLD_NS = 2_000_000_000L
    }
}
