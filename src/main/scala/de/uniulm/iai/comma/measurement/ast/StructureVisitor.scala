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

    /** Anonymous inner classes are numbered based on this counter which is reset for each structure */
    private var anonCount = 0
    def anonClassCount: Int = {
      anonCount += 1
      anonCount
    }

    private var _lineCommentCount = 0
    def increaseLineCommentCount() = _lineCommentCount += 1
    def lineCommentCount = _lineCommentCount

    private var _lineCommentLength = 0l
    def increaseLineCommentLength(size: Int) = _lineCommentLength += size
    def lineCommentLength = _lineCommentLength

    private var _blockCommentCount = 0
    def increaseBlockCommentCount() = _blockCommentCount += 1
    def blockCommentCount = _blockCommentCount

    private var _blockCommentLines = 0
    def increaseBlockCommentLines(lines: Int) = _blockCommentLines += lines
    def blockCommentLines = _blockCommentLines

    private var _blockCommentLength = 0l
    def increaseBlockCommentLength(size: Int) = _blockCommentLength += size
    def blockCommentLength = _blockCommentLength

    private var _javadocCommentCount = 0
    def increaseJavadocCommentCount() = _javadocCommentCount += 1
    def javadocCommentCount = _javadocCommentCount

    private var _javadocCommentLength = 0
    def increaseJavadocCommentLength(size: Int) = _javadocCommentLength += size
    def javadocCommentLength = _javadocCommentLength

    private var _javadocCommentLines = 0
    def increaseJavadocCommentLines(lines: Int) = _javadocCommentLines += lines
    def javadocCommentLines = _javadocCommentLines


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

  /** Store number of source level line comments. */
  private var _lineCommentCount = 0
  def lineCommentCount = _lineCommentCount

  /** Store total length of source level line comments. */
  private var _lineCommentLength = 0l
  def lineCommentLength = _lineCommentLength

  /** Store total number of block comments. */
  private var _blockCommentCount = 0
  def blockCommentCount = _blockCommentCount

  /** Store total number of source level block comment lines. */
  private var _blockCommentLines = 0
  def blockCommentLines = _blockCommentLines

  /** Store total length of source level block comments. */
  private var _blockCommentLength = 0l
  def blockCommentLength = _blockCommentLength

  /** Store total number of javadoc comments. */
  private var _javadocCommentCount = 0
  def javadocCommentCount = _javadocCommentCount

  /** Store total number of source level javadoc comment lines. */
  private var _javadocCommentLines = 0
  def javadocCommentLines = _javadocCommentLines

  /** Store total length of source level javadoc comments. */
  private var _javadocCommentLength = 0l
  def javadocCommentLength = _javadocCommentLength

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
          case Some(p) => {
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

    // Add comment count and length to top most structure on stack, because this can not be covered by comment visitor
    if (structureStack.isEmpty) {
      (node.getPrecedingComments ++ node.getFollowingComments).foreach { t =>
        t.getType match {
          case JavaLexer.LINE_COMMENT =>
            _lineCommentCount += 1
            _lineCommentLength += t.getText.length

          case JavaLexer.BLOCK_COMMENT =>
            _blockCommentCount += 1
            _blockCommentLines += t.getText.linesIterator.size
            _blockCommentLength += t.getText.length

          case JavaLexer.JAVADOC_COMMENT =>
            _javadocCommentCount += 1
            _javadocCommentLines += t.getText.linesIterator.size
            _javadocCommentLength += t.getText.length

          case _ => // ignore, ... should be never reached anyway.
        }
      }
    }

    // Dispatch to child visitors for each active structure on our stack, some visitors are only called
    // if it is the topmost structure. The latter decision is up to the visitor implementation.
    structureStack.foreach { s =>
      s.visitors.foreach(_.visit(node, s.equals(structureStack.top)))
    }
  }

  /** Return all measured values of all visitors for each detected structure. */
  override def measuredValues() = {

    // Add comments not associated with any artifact as global values.
    val comments = Vector.newBuilder[Value]
    if (lineCommentCount > 0) {
      Value(None, Measure.LINE_COMMENT_COUNT, lineCommentCount)
      Value(None, Measure.LINE_COMMENT_LENGTH, lineCommentLength)
    }
    if (blockCommentCount > 0) {
      Value(None, Measure.BLOCK_COMMENT_COUNT, blockCommentCount)
      Value(None, Measure.BLOCK_COMMENT_LINES, blockCommentLines)
      Value(None, Measure.BLOCK_COMMENT_LENGTH, blockCommentLength)
    }
    if (javadocCommentCount > 0) {
      Value(None, Measure.JAVADOC_COUNT, javadocCommentCount)
      Value(None, Measure.JAVADOC_LINES, javadocCommentLines)
      Value(None, Measure.JAVADOC_LENGTH, javadocCommentLength)
    }

    // And also include all values for all sub-visitors
    val res = structures flatMap { _._2.visitors flatMap { _.measuredValues() }}

    comments.result ++ res.toIndexedSeq
  }


  /**
   * Does return the module name for the analyzed artifact.
   *
   * The module name is a computed property. It is constructed from the changed entity path which is the
   * module base name. The artifacts package name is converted back into a path structure and striped from
   * the path. The returned module name contains only the source independent parts of the artifact path.
   **/
  def getModule: Option[String] = {
    for {
      name <- packageName
      module <- determineModuleName(compilationUnit.getFileName, compilationUnit.getFullQualifiedName, name)
    } yield module
  }


  /** Does return the package name for the analyzed artifact if a package was found. */
  def getPackage: Option[String] = packageName


  /**
   * Does return a map with all found artifacts and their measured values.
   *
   * Note: The values do depend on the additional visitors that were registered with this structural
   * visitor.
   */
  def getArtifacts: Map[Artifact, Set[Value]] = structures.values.map { s =>
    val cea = Artifact(s.name.stripPrefix(packageName.getOrElse("") + "."), s.name)
    (cea, s.visitors flatMap { _.measuredValues() })
  }.toMap


  /** Once a package declaration was found use this method to determine the module name for this artifact */
  private def determineModuleName(fileName: String, path: String, pkgName: String): Option[String] = {
    val packageWithEntity = (if (!pkgName.isEmpty) pkgName + "." else "").replace('.', '/') + fileName
    if (path.endsWith(packageWithEntity)) {
      val packageStart = path.lastIndexOf(packageWithEntity)
      Some(path.substring(0, packageStart))
    } else None
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
