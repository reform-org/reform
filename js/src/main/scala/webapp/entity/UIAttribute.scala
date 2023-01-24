package webapp.entity

import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.entity.Attribute
import webapp.given

case class UIAttribute[EntityType, AttributeType](
    getter: EntityType => Attribute[AttributeType],
    setter: (EntityType, Attribute[AttributeType]) => EntityType,
    readConverter: AttributeType => String,
    writeConverter: String => AttributeType,
    placeholder: String,
) {

  private def setFromString(entityVar: Var[Option[EntityType]], x: String): Unit = {
    entityVar.transform(
      _.map(e => {
        val attr = getter(e)
        setter(e, attr.set(writeConverter(x)))
      }),
    )
  }

  def render(entity: EntityType) = {
    td("TODO")
  }

  def renderEdit(entityVar: Var[Option[EntityType]]) = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          input(
            value := attr.getAll.map(x => readConverter(x)).mkString("/"),
            onInput.value --> {
              val evt = Evt[String]()
              evt.observe(x => setFromString(entityVar, x))
              evt
            },
            VModifier.prop("placeholder") := placeholder,
          ),
        )
      })
    }
  }
}
