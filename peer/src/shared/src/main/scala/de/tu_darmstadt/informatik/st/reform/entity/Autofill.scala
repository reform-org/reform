package de.tu_darmstadt.informatik.st.reform.entity

enum Autofill(val display: String) extends Enum[Autofill] {
  case NoFill extends Autofill("don't fill")
  case FillContract extends Autofill("fill the contract")
  case FillLetter extends Autofill("fill the letter")
}
