package de.tu_darmstadt.informatik.st.reform.entity

trait Entity[T] {
  def default: T

  def exists: Boolean

  def identifier: Attribute[String]

  def withExists(exists: Boolean): T
}
