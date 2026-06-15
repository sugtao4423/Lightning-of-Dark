package sugtao4423.twitterweb4j.parser.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TWITTER_DATE_PATTERN = "EEE MMM dd HH:mm:ss Z yyyy"

fun parseTwitterDate(date: String): Date =
    SimpleDateFormat(TWITTER_DATE_PATTERN, Locale.US).parse(date)!!
