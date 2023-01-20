package webapp.entity

import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.entity.Attribute
import webapp.given

case class UIAttribute[EntityType, AttributeType](
    attribute: Attribute[AttributeType],
    handler: AttributeHandler[EntityType, AttributeType],
    placeholder: String,
) {

  def update[T](setter: EntityType => EntityType, editingValue: Var[Option[EntityType]], x: String) = {
    editingValue.transform(value => {
      value.map(p => setter(p))
    })
  }

  def render(editingValue: Var[Option[EntityType]]) = {
    td(
      input(
        value := attribute.getAll.map(x => handler.readConverter(x)).mkString("/"),
        onInput.value --> {
          val evt = Evt[String]()
          evt.observe(x => update(l => handler.setter(l, handler.writeConverter(x)), editingValue, x))
          evt
        },
        VModifier.prop("placeholder") := placeholder,
      ),
    )
  }
}