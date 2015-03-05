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
package de.uniulm.iai.comma.measurement.processor

import java.io.InputStreamReader

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource
import de.uniulm.iai.comma.measurement.ast._
import de.uniulm.iai.comma.model._
import de.uniulm.iai.jqassistant.javasrc.plugin.scanner.ScannerHelper

object JavaMeasurement {
  def apply(helper: ScannerHelper, item: FileResource, path: String) = {
    new JavaMeasurement(helper, item, path)
  }
}

class JavaMeasurement(helper: ScannerHelper, item: FileResource, path: String) extends AstAnalyzer {

  // Create the java compilation unit descriptor
  val compilationUnitDescriptor = helper.createCompilationUnit(path, item.getFile.getName)


  /*
   * Structural analysis visitor with structure child visitors
   * Hint: Add additional sub-visitors to acquire metrics based on the internal file structure.
   */
  private val structureVisitor = new StructureVisitor(compilationUnitDescriptor, helper)
  addVisitor(structureVisitor)

  // Artifact Class visitors
  structureVisitor.addVisitorFactory(ArtifactType.ARTIFACT_CLASS, NcscssVisitor)

  // Artifact Interface visitors
  structureVisitor.addVisitorFactory(ArtifactType.ARTIFACT_INTERFACE, NcscssVisitor)

  // Artifact Enum visitors
  structureVisitor.addVisitorFactory(ArtifactType.ARTIFACT_ENUM, NcscssVisitor)

  // Artifact Annotation visitors
  structureVisitor.addVisitorFactory(ArtifactType.ARTIFACT_ANNOTATION, NcscssVisitor)

  // Class visitors
  structureVisitor.addVisitorFactory(ArtifactType.CLASS, NcscssVisitor)

  // Inner Class visitors
  structureVisitor.addVisitorFactory(ArtifactType.INNER_CLASS, NcscssVisitor)

  // Anonymous Inner Class visitors
  structureVisitor.addVisitorFactory(ArtifactType.ANON_INNER_CLASS, NcscssVisitor)

  // Interface visitors
  structureVisitor.addVisitorFactory(ArtifactType.INTERFACE, NcscssVisitor)

  // Enumeration visitors
  structureVisitor.addVisitorFactory(ArtifactType.ENUM, NcscssVisitor)

  // Inner enumeration visitors
  structureVisitor.addVisitorFactory(ArtifactType.INNER_ENUM, NcscssVisitor)

  // Annotation visitors
  structureVisitor.addVisitorFactory(ArtifactType.ANNOTATION, NcscssVisitor)

  // Constructor visitors
  structureVisitor.addVisitorFactory(ArtifactType.CONSTRUCTOR, NcscssVisitor)
  structureVisitor.addVisitorFactory(ArtifactType.CONSTRUCTOR, CyclomaticComplexityVisitor)
  structureVisitor.addVisitorFactory(ArtifactType.CONSTRUCTOR, NPathComplexityVisitor)

  // Method visitors
  structureVisitor.addVisitorFactory(ArtifactType.METHOD, NcscssVisitor)
  structureVisitor.addVisitorFactory(ArtifactType.METHOD, CyclomaticComplexityVisitor)
  structureVisitor.addVisitorFactory(ArtifactType.METHOD, NPathComplexityVisitor)

  // EnumConstant visitors
  structureVisitor.addVisitorFactory(ArtifactType.ENUM_CONST, NcscssVisitor)


  /*
   * Package visitor
   */
  val packageVisitor = new PackageVisitor(helper, compilationUnitDescriptor)
  addVisitor(packageVisitor)

  /*
   * Import visitor
   */
  val importVisitor = new ImportVisitor(helper, compilationUnitDescriptor)
  addVisitor(importVisitor)

  def run() = try {
    val reader = new InputStreamReader(item.createStream())
    runWith(reader)
    structureVisitor.evaluateSubstructureVisitors()
    compilationUnitDescriptor
  }

}
