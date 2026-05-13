package sugtao4423.twitterweb4j.parser.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val twitterDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US)

fun parseTwitterDate(date: String): Date = twitterDateFormat.parse(date)!!
