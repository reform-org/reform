package webapp.entity

trait Entity[T <: Entity[T]] {
  def exists: Attribute[Boolean]

  def identifier: Attribute[String]

  def withExists(exists: Boolean): T
}
