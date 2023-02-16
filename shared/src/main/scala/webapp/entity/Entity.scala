package webapp.entity

trait Entity[T] {
  def default: T

  def exists: Attribute[Boolean]

  def identifier: Attribute[String]

  def withExists(exists: Boolean): T

}
