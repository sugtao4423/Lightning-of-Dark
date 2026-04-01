package sugtao4423.twitterweb4j.challenge

import android.util.Base64
import java.security.MessageDigest
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class ClientTransaction @Throws(IllegalStateException::class) constructor(
    homePageHtml: String, ondemandFileContent: String
) {

    private val keyBytes: IntArray
    private val svgPaths: List<String>
    private val rowIndexKey: Int
    private val timeProductKeys: List<Int>

    companion object {
        private const val ADDITIONAL_RANDOM_NUMBER = 3
        private const val DEFAULT_KEYWORD = "obfiowerehiring"
        private const val EPOCH_OFFSET_MS = 1_682_924_400L * 1_000L
        private const val MAX_CURVE_TIME = 4096.0

        private val SITE_VERIFICATION_REGEX =
            Regex("""<meta\s+?name=["']twitter-site-verification["']\s+?content=["']([^"']+?)["']\s*?/?>""")

        private val SVG_PATH_REGEX =
            Regex("""<svg[^>]+?id=["']loading-x-anim-\d["'][^>]+?><g>(?:<path\s+?d=["']([^"']+?)["'][^>]*?></path>){2}""")

        private val INDICES_REGEX = Regex("""\(\w\[(\d{1,2})],\s*16\)""")
    }

    init {
        val siteVerificationMatch = SITE_VERIFICATION_REGEX.find(homePageHtml)
        val metaContent = siteVerificationMatch?.groupValues?.get(1) ?: throw IllegalStateException(
            "Could not find meta tag with name=\"twitter-site-verification\" in homepage HTML."
        )
        keyBytes = Base64.decode(metaContent, Base64.DEFAULT).map {
            it.toInt() and 0xFF
        }.toIntArray()

        val svgMatches = SVG_PATH_REGEX.findAll(homePageHtml).toList()
        if (svgMatches.isEmpty()) {
            throw IllegalStateException("Could not find any matching SVG paths in homepage HTML.")
        }
        svgPaths = svgMatches.map { it.groupValues[1] }
        svgPaths.forEach {
            if (it.length < 9) {
                throw IllegalStateException("SVG path 'd' attribute is unexpectedly short.")
            }
        }

        val matches = INDICES_REGEX.findAll(ondemandFileContent).toList()
        if (matches.isEmpty()) {
            throw IllegalStateException("Couldn't get KEY_BYTE indices from the ondemand.s file.")
        }
        val allIndices = matches.map { it.groupValues[1].toInt() }
        rowIndexKey = allIndices.first()
        timeProductKeys = allIndices.drop(1)
    }

    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    fun generateTransactionId(method: String, path: String): String {
        val animationKey = computeAnimationKey()

        val timeNow = ((System.currentTimeMillis() - EPOCH_OFFSET_MS) / 1_000L).toInt()
        val timeNowBytes = IntArray(4) { i -> (timeNow ushr (i * 8)) and 0xFF }

        val hashInput = "$method!$path!$timeNow$DEFAULT_KEYWORD$animationKey"
        val hashBytes = hashInput.toByteArray(Charsets.UTF_8).let {
            MessageDigest.getInstance("SHA-256").digest(it)
        }

        val plaintext = keyBytes.toList() + timeNowBytes.toList() + hashBytes.take(16).map {
            it.toInt() and 0xFF
        } + ADDITIONAL_RANDOM_NUMBER

        val xorKey = (0..255).random()
        val obfuscatedBytes =
            byteArrayOf(xorKey.toByte()) + plaintext.map { (it xor xorKey).toByte() }.toByteArray()

        return Base64.encodeToString(obfuscatedBytes, Base64.NO_WRAP or Base64.NO_PADDING)
    }

    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    private fun computeAnimationKey(): String {
        val rowIndex = keyBytes[rowIndexKey] % 16

        val rawTimeProduct = timeProductKeys.fold(1) { acc, idx ->
            acc * (keyBytes[idx] % 16)
        }
        val curveTime = jsRound(rawTimeProduct / 10.0) * 10

        val svgPathIndex = keyBytes[5] % 4
        val curveSegments = parseSvgCurveData(svgPathIndex)

        val curveParams = curveSegments.getOrNull(rowIndex)
            ?: throw IllegalStateException("rowIndex=$rowIndex out of bounds for curveSegments (size=${curveSegments.size}).")

        val targetTime = curveTime / MAX_CURVE_TIME
        return animateCurve(curveParams, targetTime)
    }

    @Throws(IllegalStateException::class)
    private fun parseSvgCurveData(pathIndex: Int): List<List<Int>> {
        val d = svgPaths.getOrNull(pathIndex)
            ?: throw IllegalStateException("No SVG path found for pathIndex=$pathIndex.")
        return d.substring(9).split("C").filter { it.isNotBlank() }.map { segment ->
            segment.replace(Regex("\\D+"), " ").trim().split(Regex("\\s+"))
                .filter { it.isNotEmpty() }.map { it.toInt() }
        }
    }

    private fun jsRound(num: Double): Int {
        val x = floor(num)
        return if (num - x >= 0.5) ceil(num).toInt() else x.toInt()
    }

    @Throws(IllegalArgumentException::class)
    private fun animateCurve(curveParams: List<Int>, targetTime: Double): String {
        if (curveParams.size < 7) {
            throw IllegalArgumentException("Expected at least 7 parameters for the curve, but got ${curveParams.size}.")
        }

        val fromColor = curveParams.take(3).map { it.toDouble() } + 1.0
        val toColor = curveParams.subList(3, 6).map { it.toDouble() } + 1.0

        val fromRotation = listOf(0.0)
        val toRotation = listOf(remapToRange(curveParams[6].toDouble(), 60.0, 360.0, true))

        val bezierCurves = curveParams.drop(7).mapIndexed { index, item ->
            remapToRange(item.toDouble(), if (index % 2 != 0) -1.0 else 0.0, 1.0, false)
        }

        val easedProgress = CubicBezier.getValue(bezierCurves, targetTime)

        val color = interpolate(fromColor, toColor, easedProgress).map { it.coerceIn(0.0, 255.0) }
        val rotation = interpolate(fromRotation, toRotation, easedProgress)
        val matrix = convertRotationToMatrix(rotation[0])

        val hexParts = buildList {
            color.take(3).forEach {
                add(bankersRound(it).toString(16))
            }
            matrix.forEach { value ->
                val absVal = abs(bankersRoundToTwo(value))
                val hex = floatToHex(absVal)
                val format = if (hex.startsWith(".")) "0$hex".lowercase() else hex.ifEmpty { "0" }
                add(format)
            }
            add("0")
            add("0")
        }

        return hexParts.joinToString("").replace(Regex("[.-]"), "")
    }

    private fun bankersRound(value: Double): Int {
        val floor = floor(value).toInt()
        return when {
            value - floor < 0.5 -> floor
            value - floor > 0.5 -> floor + 1
            else -> if (floor % 2 == 0) floor else floor + 1
        }
    }

    private fun bankersRoundToTwo(value: Double): Double = bankersRound(value * 100.0) / 100.0

    private fun remapToRange(
        value: Double, minVal: Double, maxVal: Double, rounding: Boolean
    ): Double {
        val result = value * (maxVal - minVal) / 255.0 + minVal
        return if (rounding) floor(result) else bankersRoundToTwo(result)
    }

    @Throws(IllegalArgumentException::class)
    private fun interpolate(from: List<Double>, to: List<Double>, f: Double): List<Double> {
        if (from.size != to.size) {
            throw IllegalArgumentException("interpolate: 'from' and 'to' lists must have the same size.")
        }
        return from.zip(to).map { (a, b) -> a * (1.0 - f) + b * f }
    }

    private fun convertRotationToMatrix(degrees: Double): List<Double> {
        val rad = degrees * PI / 180.0
        val c = cos(rad)
        val s = sin(rad)
        return listOf(c, -s, s, c)
    }

    private fun floatToHex(input: Double): String {
        val intPart = floor(input).toInt()
        val fraction = input - intPart

        val intHex = if (intPart > 0) intPart.toString(16).uppercase() else ""

        if (fraction == 0.0) return intHex

        return buildString {
            append(intHex)
            append('.')
            var frac = fraction
            while (frac > 0) {
                frac *= 16.0
                val digit = floor(frac).toInt()
                frac -= digit
                append(if (digit > 9) ('A' + (digit - 10)) else ('0' + digit))
            }
        }
    }

}
