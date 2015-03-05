package de.uniulm.iai.comma.measurement.ast

import com.buschmais.jqassistant.core.scanner.api.ScannerContext
import de.uniulm.iai.comma.lib.ast.javasource.EnhancedCommonTree
import de.uniulm.iai.comma.lib.ast.javasource.JavaParser._
import de.uniulm.iai.jqassistant.javasrc.plugin.model.{DependentDescriptor, IncompleteTypeDescriptor, JavaCompilationUnitDescriptor}
import de.uniulm.iai.jqassistant.javasrc.plugin.scanner.ScannerHelper

import scala.collection.JavaConversions._

/**
 * @author Steffen Kram
 */
class ImportVisitor(helper: ScannerHelper, compilationUnit: JavaCompilationUnitDescriptor)
    extends TreeVisitor with VisitorHelper {

  override def visit(node: EnhancedCommonTree): Unit = {
    node.getType match {
      case IMPORT =>
        val i = stringifyNodes(node.getChildren.toIndexedSeq, "").trim.replace(" ", ".")
        helper.resolveImport(i, compilationUnit)

      case _ => // ignore all other tokens
    }
  }
}
