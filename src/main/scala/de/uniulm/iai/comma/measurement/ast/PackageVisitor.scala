package de.uniulm.iai.comma.measurement.ast

import com.buschmais.jqassistant.core.store.api.model.Descriptor
import de.uniulm.iai.comma.lib.ast.javasource.EnhancedCommonTree
import de.uniulm.iai.comma.lib.ast.javasource.JavaParser._
import de.uniulm.iai.comma.model.{Measure, Change}
import de.uniulm.iai.jqassistant.javasrc.plugin.model.JavaSourceDescriptor

import scala.collection.JavaConversions._

/**
 * @author Steffen Kram
 */

object PackageVisitor extends TreeVisitorFactory {

  override def measures(): Iterable[Measure] = Vector.empty[Measure]

  override def createVisitor(entity: Change, descriptor: Descriptor, artifact: Option[String]): TreeVisitor = {
    new PackageVisitor(entity, descriptor.asInstanceOf[JavaSourceDescriptor], artifact)
  }

}

class PackageVisitor(entity: Change, descriptor: JavaSourceDescriptor, artifact: Option[String])
    extends TreeVisitor with VisitorHelper {

  /** Store the package identifier of this file if one is present */
  private var packageName: Option[String] = None

  override def visit(node: EnhancedCommonTree): Unit = {
    node.getType match {

      case PACKAGE => {
        packageName = Some(
          stringifyNodes(node.getChildren.toIndexedSeq, "").trim.replace(" ", "."))
      }
    }

  }

}
