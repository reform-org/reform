package de.tu_darmstadt.informatik.st.reform.entity

enum DocumentsForWhom(val id: String, val display: String, val done: String, val lastSent: Contract => Long)
    extends Enum[DocumentsForWhom] {

  case ForNobody
      extends DocumentsForWhom(
        id = "nobody",
        display = "nobody",
        done = "",
        lastSent = _ => -1,
      )

  case ForHiwi
      extends DocumentsForWhom(
        id = "hiwi",
        display = "the hiwi",
        done = "The contract has been signed",
        lastSent = _.contractSentDate.getOrElse(0L),
      )

  case ForDekanat
      extends DocumentsForWhom(
        id = "dekanat",
        display = "the dekanat",
        done = "The letter has been submitted",
        lastSent = _.letterSentDate.getOrElse(0L),
      )

}
