package sugtao4423.twitterweb4j.challenge

import kotlin.math.abs

object CubicBezier {

    private fun calculate(a: Double, b: Double, m: Double): Double =
        3.0 * a * (1 - m) * (1 - m) * m + 3.0 * b * (1 - m) * m * m + m * m * m

    fun getValue(curves: List<Double>, time: Double): Double {
        if (time <= 0.0) {
            val startGradient = when {
                curves[0] > 0.0 -> curves[1] / curves[0]
                curves[1] == 0.0 && curves[2] > 0.0 -> curves[3] / curves[2]
                else -> 0.0
            }
            return startGradient * time
        }

        if (time >= 1.0) {
            val endGradient = when {
                curves[2] < 1.0 -> (curves[3] - 1.0) / (curves[2] - 1.0)
                curves[2] == 1.0 && curves[0] < 1.0 -> (curves[1] - 1.0) / (curves[0] - 1.0)
                else -> 0.0
            }
            return 1.0 + endGradient * (time - 1.0)
        }

        var start = 0.0
        var end = 1.0
        var mid = 0.0

        while (start < end) {
            mid = (start + end) / 2.0
            val xEst = calculate(curves[0], curves[2], mid)
            if (abs(time - xEst) < 1e-5) {
                return calculate(curves[1], curves[3], mid)
            }
            if (xEst < time) {
                start = mid
            } else {
                end = mid
            }
        }

        return calculate(curves[1], curves[3], mid)
    }

}
