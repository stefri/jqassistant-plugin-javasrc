package de.uniulm.iai.comma.model

sealed trait Visibility extends Visibility.Value {
  def name: String
}

object Visibility extends Enum[Visibility] {
  case object PUBLIC    extends Visibility { val name = "public" }
  case object DEFAULT   extends Visibility { val name = "default" }
  case object PROTECTED extends Visibility { val name = "protected" }
  case object PRIVATE   extends Visibility { val name = "private" }
  case object ANONYMOUS extends Visibility { val name = "anonymous" }

  val values = Vector(PUBLIC, DEFAULT, PROTECTED, PRIVATE, ANONYMOUS)
}
