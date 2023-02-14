package webapp.utils

import java.text.*
import java.util.Locale

object Money {

  private val symbols = {
    val symbols = DecimalFormatSymbols(Locale.GERMAN)
    symbols.setDecimalSeparator(',')
    symbols.setGroupingSeparator('.')
    symbols
  }

  private val df = new DecimalFormat("#,###.00", symbols)

  implicit class BigDecimalOps(self: BigDecimal) {

    def toMoneyString: String = df.format(self) + " €"
  }
}
