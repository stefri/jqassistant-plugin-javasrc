package de.uniulm.iai.comma.measurement.ast

import com.buschmais.jqassistant.core.scanner.api.ScannerContext
import de.uniulm.iai.comma.lib.ast.javasource.EnhancedCommonTree
import de.uniulm.iai.comma.lib.ast.javasource.JavaParser._
import de.uniulm.iai.jqassistant.javasrc.plugin.model.JavaSourceDescriptor
import de.uniulm.iai.jqassistant.javasrc.plugin.scanner.ScannerHelper

import scala.collection.JavaConversions._

/**
 * @author Steffen Kram
 */

class PackageVisitor(helper: ScannerHelper, descriptor: JavaSourceDescriptor)
    extends TreeVisitor with VisitorHelper {

  /** Store the package identifier of this file if one is present */
  private var packageName: Option[String] = None

  override def visit(node: EnhancedCommonTree): Unit = {
    node.getType match {

      case PACKAGE => {
        packageName = Some(stringifyNodes(node.getChildren.toIndexedSeq, "").trim.replace(" ", "."))
      }

      case _ => // Ignore all other tokens
    }

  }

}
