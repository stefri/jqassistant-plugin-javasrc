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
package de.uniulm.iai.comma.measurement.ast

import com.buschmais.jqassistant.core.store.api.model.Descriptor
import de.uniulm.iai.comma.model.{Change, Measure}


trait TreeVisitorFactory {

  def measures(): Iterable[Measure]

  def createVisitor(entity: Change, descriptor: Descriptor): TreeVisitor = createVisitor(entity, descriptor, None)

  def createVisitor(entity: Change, descriptor: Descriptor, artifact: Option[String]): TreeVisitor

}
