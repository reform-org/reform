package webapp.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter;

object Date {
  def dateToEpochDay(s: String, format: String): Long = {
    val formatter = DateTimeFormatter.ofPattern(format)
    val date = LocalDate.parse(s, formatter)
    return date.toEpochDay();
  }

  def epochDayToDate(l: Long, format: String): String = {
    val formatter = DateTimeFormatter.ofPattern(format)
    val date = LocalDate.ofEpochDay(l)
    return date.format(formatter)
  }
}
