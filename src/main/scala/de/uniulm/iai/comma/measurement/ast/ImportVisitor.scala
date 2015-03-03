package de.uniulm.iai.comma.measurement.ast

import com.buschmais.jqassistant.core.scanner.api.ScannerContext
import de.uniulm.iai.comma.lib.ast.javasource.EnhancedCommonTree
import de.uniulm.iai.comma.lib.ast.javasource.JavaParser._
import de.uniulm.iai.jqassistant.javasrc.plugin.model.{DependentDescriptor, IncompleteTypeDescriptor, JavaCompilationUnitDescriptor}

import scala.collection.JavaConversions._

/**
 * @author Steffen Kram
 */
class ImportVisitor(context: ScannerContext, compilationUnit: JavaCompilationUnitDescriptor)
    extends TreeVisitor with VisitorHelper {

  override def visit(node: EnhancedCommonTree): Unit = {
    node.getType match {
      case IMPORT => {
        val i = stringifyNodes(node.getChildren.toIndexedSeq, "").trim.replace(" ", ".")
        val dependency = context.getStore.create(classOf[IncompleteTypeDescriptor])
        dependency.setName(i.split('.').reverse.head)
        dependency.setFullQualifiedName(i)
        dependency.setRequiredBy(compilationUnit)
      }

      case _ => // ignore all other tokens
    }
  }
}
