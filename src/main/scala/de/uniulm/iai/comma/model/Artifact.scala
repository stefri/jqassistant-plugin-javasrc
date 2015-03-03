/*
 * comma, A Code Measurement and Analysis Tool
 * Copyright (C) 2010-2015 Steffen Kram
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.uniulm.iai.comma.model

import com.buschmais.jqassistant.core.store.api.model.Descriptor

sealed trait ArtifactType extends ArtifactType.Value {
  def name: String
}

object ArtifactType extends Enum[ArtifactType] {
  case object ARTIFACT_CLASS      extends ArtifactType { val name = "Artifact Class" }
  case object ARTIFACT_INTERFACE  extends ArtifactType { val name = "Artifact Interface" }
  case object ARTIFACT_ENUM       extends ArtifactType { val name = "Artifact Enumeration" }
  case object ARTIFACT_ANNOTATION extends ArtifactType { val name = "Artifact Annotation" }
  case object CLASS               extends ArtifactType { val name = "Class" }
  case object INNER_CLASS         extends ArtifactType { val name = "Inner Class" }
  case object ANON_INNER_CLASS    extends ArtifactType { val name = "Anonymous Inner Class" }
  case object INTERFACE           extends ArtifactType { val name = "Interface" }
  case object INNER_INTERFACE     extends ArtifactType { val name = "Inner Interface" }
  case object ENUM                extends ArtifactType { val name = "Enumeration" }
  case object INNER_ENUM          extends ArtifactType { val name = "Inner Enumeration" }
  case object ANNOTATION          extends ArtifactType { val name = "Annotation" }
  case object INNER_ANNOTATION    extends ArtifactType { val name = "Inner Annotation" }
  case object CONSTRUCTOR         extends ArtifactType { val name = "Constructor" }
  case object METHOD              extends ArtifactType { val name = "Method" }
  case object ENUM_CONST          extends ArtifactType { val name = "Enum Constant" }

  val values = Vector(
    ARTIFACT_CLASS,
    ARTIFACT_INTERFACE,
    ARTIFACT_ENUM,
    ARTIFACT_ANNOTATION,
    CLASS,
    INNER_CLASS,
    ANON_INNER_CLASS,
    INTERFACE,
    INNER_INTERFACE,
    ENUM,
    INNER_ENUM,
    ANNOTATION,
    INNER_ANNOTATION,
    CONSTRUCTOR,
    METHOD,
    ENUM_CONST)
}


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


case class Artifact(
    artifact: String,
    artifactFqn: String,
    descriptor: Descriptor)

