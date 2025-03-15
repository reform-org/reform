package de.tu_darmstadt.informatik.st.reform

import de.tu_darmstadt.informatik.st.reform.entity.Contract
import rescala.default.*

class ContractDocuments(using jsImplicits: JSImplicits) {

  def documentsFromSchema(contract: Signal[Contract]): Signal[Seq[String]] = Signal.dynamic {
    contract.value.contractSchema.option
      .flatMap({ schema =>
        jsImplicits.repositories.contractSchemas
          .find(schema)
          .value
          .flatMap(_.signal.value.files.option)
      })
      .getOrElse(Seq.empty)
  }

}
