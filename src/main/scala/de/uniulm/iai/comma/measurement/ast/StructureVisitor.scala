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
import de.uniulm.iai.comma.lib.ast.javasource.JavaParser._
import de.uniulm.iai.comma.lib.ast.javasource.{EnhancedCommonTree, JavaLexer}
import de.uniulm.iai.comma.model._
import de.uniulm.iai.jqassistant.javasrc.plugin.api.scanner.TypeCache
import de.uniulm.iai.jqassistant.javasrc.plugin.model._
import de.uniulm.iai.jqassistant.javasrc.plugin.scanner.ScannerHelper
import org.antlr.runtime.tree.Tree

import scala.collection.JavaConversions._


/**
 * The Structure visitor is a very sophisticated visitor implementation that is capable of dispatching
 * to numerous sub-visitors. A sub-visitor does implement the same `TreeVisitor` trait. It should compute
 * a measure that holds if applied in a sub-structural context.
 */
class StructureVisitor(compilationUnit: JavaCompilationUnitDescriptor, helper: ScannerHelper)
    extends TreeVisitor with VisitorHelper {

  /** Internal storage for all registered artifact type visitor factories */
  private val visitorFactories =
    collection.mutable.Map.empty[ArtifactType, collection.mutable.Set[TreeVisitorFactory]]

  /**
   * Add a visitor factory for a certain artifact type.
   *
   * When the structure visitor does detect an artifact of the given type it uses the
   * factories `createVisitor` to create a new visitor for the structure that measures
   * the desired value.
   *
   * Note, that this limits the visitors which are applicable to be used with the structure
   * visitors dispatching mode to those who have a companion object which implements the
   * `TreeVisitorFactory` trait.
   */
  def addVisitorFactory(artifactType: ArtifactType, factory: TreeVisitorFactory) = {
    if (!visitorFactories.contains(artifactType)) {
      visitorFactories(artifactType) = collection.mutable.Set.empty[TreeVisitorFactory]
    }
    visitorFactories(artifactType) += factory
  }

  /** This is an internal class to store information about the source structure while parsing the source file. */
  private class Structure(val node: EnhancedCommonTree,
                           val name: String,
                           val cachedType: Option[TypeCache.CachedType[_ <: TypeDescriptor]],
                           val parent: Option[Structure],
                           val visitors: Set[TreeVisitor]) {

    /** Check if this structure is the compilation unit. */
    def isCompilationUnit: Boolean = { node.getType == JAVA_SOURCE }

    /** Anonymous inner classes are numbered based on this counter which is reset for each structure */
    private var anonCount = 0
    def anonClassCount: Int = {
      anonCount += 1
      anonCount
    }

    /** Check if the given node is a sub-node of this structure. Used to reduce the structure stack. */
    def containsNode(node: EnhancedCommonTree): Boolean = {
      this.node.getLastLine >= node.getLine
    }

    def canEqual(other: Any): Boolean = other.isInstanceOf[Structure]

    override def equals(other: Any): Boolean = {
      other match {
        case that: Structure => (that canEqual this) && this.node == that.node
        case _ => false
      }
    }

    override def hashCode: Int = node.hashCode
  }

  /** Store the package identifier of this file if one is present */
  private var packageName: Option[String] = None

  /** Maps each structure to its first tree node. Use the tree node to parse the structure again */
  private val structures = collection.mutable.HashMap.empty[Tree, Structure]

  /** This stack holds all "open" structures to be able to correctly name and identify nested structures */
  private var structureStack: collection.mutable.ArrayStack[Structure] = new collection.mutable.ArrayStack[Structure]


  /** This visitor method uses pattern-matching to split the source file into its sub-structures */
  override def visit(node: EnhancedCommonTree) = {

    // Remove closed elements from stack
    structureStack = structureStack.filter(_.containsNode(node))

    // Evaluate node type to build source structure
    node.getType match {
      case JAVA_SOURCE => {
        val visitors = createStructureVisitors(ArtifactType.COMPILATION_UNIT, compilationUnit, None)
        val structure = new Structure(node, node.getText, None, None, visitors)
        structureStack.push(structure)
      }

      case PACKAGE => {
        packageName = Some(
          stringifyNodes(node.getChildren.toIndexedSeq, "").trim.replace(" ", "."))
      }

      case CLASS_DECLARATION
           | INTERFACE
           | ENUM
           | ANNOTATION_DECL => {

        // Lookup parent
        val parent = if (structureStack.isEmpty) None else Some(structureStack.top)

        // Create new structure
        val className = findIdentifier(node).map(_.getText).getOrElse("[NO IDENTIFIER FOUND]")

        val fullClassName = parent.map(_.name + "." + className).getOrElse {
          packageName.map(_ + "." + className).getOrElse(className)
        }

        // If a parent is present, store as inner class otherwise as first order class or as main class
        val structure = parent match {
          case Some(p) if p.isCompilationUnit => {
            // Determine inner artifact types
            val classType = node.getType match {
              case CLASS_DECLARATION  => (ArtifactType.INNER_CLASS, classOf[ClassDescriptor])
              case INTERFACE          => (ArtifactType.INNER_INTERFACE, classOf[InterfaceDescriptor])
              case ENUM               => (ArtifactType.INNER_ENUM, classOf[EnumDescriptor])
              case ANNOTATION_DECL    => (ArtifactType.INNER_ANNOTATION, classOf[AnnotationDescriptor])
              case _ => throw new IllegalStateException("Unexpected inner class type: " + node.getType)
            }

            val visibility = detectVisibility(node)

            // Create new descriptor
            val cachedType = helper.createType(fullClassName, classType._2)
            val descr = cachedType.getTypeDescriptor
            descr.setName(className)
            descr.setStartLineNumber(node.getLine)
            descr.setEndLineNumber(node.getLastLine)
            descr.setVisibility(visibility.name)
            descr.setFinal(detectFinal(node))
            descr.setStatic(detectStatic(node))
            descr.setAbstract(detectAbstract(node))

            // Add descriptor as inner type definition
            p.cachedType.get.getTypeDescriptor.getDeclaredInnerTypes.add(descr)

            val visitors = createStructureVisitors(classType._1, descr, Some(fullClassName))
            new Structure(node, fullClassName, Some(cachedType), parent, visitors)
          }

          case None    =>
            // This is because there might be more than one java class definition on the outmost level
            if (compilationUnit.getFullQualifiedName.endsWith("/" + className + ".java")) {

              // Determine artifact types
              val classType = node.getType match {
                case CLASS_DECLARATION  => (ArtifactType.ARTIFACT_CLASS, classOf[ClassDescriptor])
                case INTERFACE          => (ArtifactType.ARTIFACT_INTERFACE, classOf[InterfaceDescriptor])
                case ENUM               => (ArtifactType.ARTIFACT_ENUM, classOf[EnumDescriptor])
                case ANNOTATION_DECL    => (ArtifactType.ARTIFACT_ANNOTATION, classOf[AnnotationDescriptor])
                case _ => throw new IllegalStateException("Unexpected main class type: " + node.getType)
              }

              val cachedType = helper.createType(fullClassName, classType._2)
              val descr = cachedType.getTypeDescriptor
              descr.setDeclarationUnit(compilationUnit)
              descr.setName(className)
              descr.setStartLineNumber(node.getLine)
              descr.setEndLineNumber(node.getLastLine)
              descr.setVisibility(detectVisibility(node).name)
              descr.setFinal(detectFinal(node))
              descr.setStatic(detectStatic(node))
              descr.setAbstract(detectAbstract(node))
              compilationUnit.setMainType(descr)

              val visitors = createStructureVisitors(classType._1, descr, Some(fullClassName))
              new Structure(node, fullClassName, Some(cachedType), None, visitors)

            } else {

              // Determine artifact types
              val classType = node.getType match {
                case CLASS_DECLARATION  => (ArtifactType.CLASS, classOf[ClassDescriptor])
                case INTERFACE          => (ArtifactType.INTERFACE, classOf[InterfaceDescriptor])
                case ENUM               => (ArtifactType.ENUM, classOf[EnumDescriptor])
                case ANNOTATION_DECL    => (ArtifactType.ANNOTATION, classOf[AnnotationDescriptor])
                case _ => throw new IllegalStateException("Unexpected class type: " + node.getType)
              }

              val cachedType = helper.createType(fullClassName, classType._2)
              val descr = cachedType.getTypeDescriptor
              descr.setDeclarationUnit(compilationUnit)
              descr.setName(className)
              descr.setStartLineNumber(node.getLine)
              descr.setEndLineNumber(node.getLastLine)
              descr.setVisibility(detectVisibility(node).name)
              descr.setFinal(detectFinal(node))
              descr.setStatic(detectStatic(node))
              descr.setAbstract(detectAbstract(node))

              val visitors = createStructureVisitors(classType._1, descr, Some(fullClassName))
              new Structure(node, fullClassName, Some(cachedType), None, visitors)
            }
        }

        structures(node) = structure
        structureStack.push(structure)
      }

      /* FIXME case ENUM_CLASS_BODY => {
        val enumDecl = node.getParent

        // Lookup parent
        val parent = structureStack.top

        // Lookup enum identifier
        val enumName = findIdentifier(enumDecl).map { i =>
          parent.name + "." + i.getText
        } getOrElse {
          parent.name + ".[NO IDENTIFIER FOUND]"
        }

        // Create new structure
        val cachedType = helper.createType(enumName, classOf[EnumConstantDescriptor])
        val descr = cachedType.getTypeDescriptor
        //descr.setType(parent.descriptor.asInstanceOf[TypeDescriptor])
        descr.setName(enumName)
        descr.setSignature(enumName)
        descr.setStartLineNumber(node.getLine)
        descr.setEndLineNumber(node.getLastLine)

        // Add descriptor as inner type definition
        // FIXME Should we add an enum constant as field?
        // parent.cachedType.get.getTypeDescriptor.getDeclaredInnerTypes.add(descr)

        val visitors = createStructureVisitors(ArtifactType.ENUM_CONST, changedEntity, descr, Some(enumName))
        val structure = new Structure(enumDecl, enumName, None, Some(parent), visitors)
        structures(enumDecl) = structure
        structureStack.push(structure)
      }*/

      case CONSTRUCTOR_DECL => {

        // Lookup parent
        val parent = structureStack.top

        // Create new structure
        val constructorSig =
          addToSignature(node.getChildren.toIndexedSeq, parent.name)

        val descr = helper.constructorDescriptor(parent.cachedType.get, constructorSig)
        descr.setName(parent.cachedType.get.getTypeDescriptor.getName)
        descr.setSignature(constructorSig)
        descr.setAbstract(detectAbstract(node))
        descr.setFinal(detectFinal(node))
        descr.setVisibility(detectVisibility(node).name)
        descr.setStartLineNumber(node.getLine)
        descr.setEndLineNumber(node.getLastLine)


        val visitors = createStructureVisitors(ArtifactType.CONSTRUCTOR, descr, Some(constructorSig))
        val structure = new Structure(node, constructorSig, None, Some(parent), visitors)
        structures(node) = structure
        structureStack.push(structure)
      }

      case ANONYMOUS_CLASS_CONSTRUCTOR_CALL => {

        // Lookup parent
        val parent = structureStack.top

        // Create new structure
        val parentSimpleClassName = parent.cachedType.get.getTypeDescriptor.getName
        val index = parent.anonClassCount

        val cachedType = helper.createType(s"${parent.name}.ANON[$index]", classOf[AnonymousClassDescriptor])
        val descr = cachedType.getTypeDescriptor
        descr.setIndex(index)
        descr.setName(s"$parentSimpleClassName.ANON[${descr.getIndex}]")
        descr.setStartLineNumber(node.getLine)
        descr.setEndLineNumber(node.getLastLine)

        // Add descriptor as inner type definition
        parent.cachedType.get.getTypeDescriptor.getDeclaredInnerTypes.add(descr)

        val visitors =
          createStructureVisitors(ArtifactType.ANON_INNER_CLASS, descr, Some(descr.getFullQualifiedName))
        val structure = new Structure(node, descr.getFullQualifiedName, Some(cachedType), Some(parent), visitors)
        structures(node) = structure
        structureStack.push(structure)
      }

      case VOID_METHOD_DECL
           | FUNCTION_METHOD_DECL
           | ANNOTATION_METHOD_DECL => {

        // Lookup parent
        val parent = structureStack.top

        // Create new structure
        val methodName =
          addToSignature(node.getChildren.toIndexedSeq, parent.name + ".")

        val descr = helper.methodDescriptor(parent.cachedType.get, methodName)
        descr.setName(getFunctionIdentifier(node.getChildren.toIndexedSeq))
        descr.setAbstract(detectAbstract(node))
        descr.setFinal(detectFinal(node))
        descr.setVisibility(detectVisibility(node).name)
        descr.setStatic(detectStatic(node))
        descr.setStartLineNumber(node.getLine)
        descr.setEndLineNumber(node.getLastLine)

        val visitors = createStructureVisitors(ArtifactType.METHOD, descr, Some(methodName))
        val structure = new Structure(node, methodName, None, Some(parent), visitors)
        structures(node) = structure
        structureStack.push(structure)
      }

      case VAR_DECLARATION => {
        val parent = structureStack.top

        // Only include fields and no variable declarations inside of methods!
        if (parent.cachedType.isDefined) {
          val visibility = detectVisibility(node).name
          val isFinal = detectFinal(node)
          val isStatic = detectStatic(node)
          val isTransient = detectTransient(node)
          val isVolatile = detectVolatile(node)
          val fieldType = detectType(node)
          val fieldNameNodes = detectVariableDeclarators(node)

          fieldNameNodes.foreach { n =>
            val signature = n.getChild(0).getText
            val descr = helper.fieldDescriptor(parent.cachedType.get, signature)
            descr.setName(signature)
            descr.setVisibility(visibility)
            descr.setFinal(isFinal)
            descr.setStatic(isStatic)
            descr.setTransient(isTransient)
            descr.setVolatile(isVolatile)
            descr.setStartLineNumber(node.getLine)

            val typeDescr = helper.resolveType(fieldType.get.getText, parent.cachedType.get).getTypeDescriptor
            descr.setType(typeDescr)
          }
        }
      }

      case _ => // Silently ignore all other tokens
    }

    // Dispatch to child visitors for each active structure on our stack, some visitors are only called
    // if it is the topmost structure. The latter decision is up to the visitor implementation.
    structureStack.foreach { s =>
      s.visitors.foreach(_.visit(node, s.equals(structureStack.top)))
    }
  }

  /** Return all measured values of all visitors for each detected structure. */
  override def measuredValues() = {
    val res = structures flatMap { _._2.visitors flatMap { _.measuredValues() }}
    res.toIndexedSeq
  }

  /** Does return the package name for the analyzed artifact if a package was found. */
  def getPackage: Option[String] = packageName


  /**
   * Calls the measuredValues method for each visitor registered with a substructure.
   *
   * It is necessary to run this evaluation method after the structural analysis is complete to
   * force all substructure visitors to create their nodes in the underlying database.
   */
  def evaluateSubstructureVisitors(): Unit = {
    structures.values.foreach(s => s.visitors.foreach(_.measuredValues()))
  }

  /** This method provides a save way to get a set with newly created visitors for a detected structure */
  private def createStructureVisitors(artifactType: ArtifactType,
                                      descriptor: Descriptor,
                                      artifactName: Option[String]): Set[TreeVisitor] = {

    // Check if artifact type is already present in visitor factories list - if not create it!
    if (!visitorFactories.contains(artifactType)) {
      visitorFactories(artifactType) = collection.mutable.Set.empty[TreeVisitorFactory]
    }

    visitorFactories(artifactType).map { v =>
      v.createVisitor(descriptor, artifactName)
    }.toSet
  }
}
