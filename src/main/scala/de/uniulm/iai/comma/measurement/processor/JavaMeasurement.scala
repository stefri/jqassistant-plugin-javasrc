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

import java.io.Reader

import de.uniulm.iai.comma.measurement.ast._
import de.uniulm.iai.comma.model._
import de.uniulm.iai.jqassistant.javasrc.plugin.model.JavaCompilationUnitDescriptor
import org.apache.commons.logging.LogFactory

class JavaMeasurement(artifactDescriptor: JavaCompilationUnitDescriptor, code: Reader, path: String)
    extends AstAnalyzer {

  private val logger = LogFactory.getLog(getClass)

  // Create a dummy change
  private val change = Change(artifactDescriptor.getFileName, path)

  /*
   * Structural analysis visitor with structure child visitors
   * Hint: Add additional sub-visitors to acquire metrics based on the internal file structure.
   */
  private val structureVisitor = new StructureVisitor(change)
  addVisitor(structureVisitor)


  def run() = runWith(change, code) map { result =>

  }
}
