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

import de.uniulm.iai.comma.lib.ast.javasource.JavaParser._
import de.uniulm.iai.comma.lib.ast.javasource.{EnhancedCommonTree, JavaLexer}
import de.uniulm.iai.comma.model._
import de.uniulm.iai.jqassistant.javasrc.plugin.model.JavaCompilationUnitDescriptor
import org.antlr.runtime.tree.Tree

import scala.annotation.tailrec
import scala.collection.JavaConversions._


/** The Structure visitor is a very sophisticated visitor implementation that is capable of dispatching
  * to numerous sub-visitors. A sub-visitor does implement the same `TreeVisitor` trait. It should compute
  * a measure that holds if applied in a sub-structural context.
  */
class StructureVisitor(changedEntity: Change, compilationUnitDescriptor: JavaCompilationUnitDescriptor) extends TreeVisitor {

  /** Internal storage for all registered artifact type visitor factories */
  private val visitorFactories =
    collection.mutable.Map.empty[ArtifactType, collection.mutable.Set[TreeVisitorFactory]]


  /** Add a visitor factory for a certain artifact type.
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
  private class Structure(
      val node: EnhancedCommonTree,
      val name: String,
      val artifactType: ArtifactType,
      val visibility: Visibility,
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
          val className = findIdentifier(node) match {
            case Some(n) => n.getText
            case None    => "[NO IDENTIFIER FOUND]"
          }
          val fullClassName = parent match {
            case Some(p) => p.name + "." + className
            case None    => packageName match {
              case Some(p) => p + "." + className
              case None    => className
            }
          }

          // If a parent is present, store as inner class otherwise as first order class or as main class
          val structure = parent match {
            case Some(p) => {
              // Determine inner artifact types
              val classType = node.getType match {
                case CLASS_DECLARATION  => ArtifactType.INNER_CLASS
                case INTERFACE          => ArtifactType.INNER_INTERFACE
                case ENUM               => ArtifactType.INNER_ENUM
                case ANNOTATION_DECL    => ArtifactType.INNER_ANNOTATION
                case _ => throw new IllegalStateException("Unexpected inner class type: " + node.getType)
              }

              val visitors = createStructureVisitors(classType, changedEntity, Some(fullClassName))
              new Structure(node, fullClassName, classType, detectVisibility(node), parent, visitors)
            }

            case None    =>
              // This is because there might be more than one java class definition on the outmost level
              if (changedEntity.path.endsWith("/" + className + ".java")) {

                // Determine artifact types
                val classType = node.getType match {
                  case CLASS_DECLARATION  => ArtifactType.ARTIFACT_CLASS
                  case INTERFACE          => ArtifactType.ARTIFACT_INTERFACE
                  case ENUM               => ArtifactType.ARTIFACT_ENUM
                  case ANNOTATION_DECL    => ArtifactType.ARTIFACT_ANNOTATION
                  case _ => throw new IllegalStateException("Unexpected main class type: " + node.getType)
                }

                val visitors = createStructureVisitors(classType, changedEntity, Some(fullClassName))
                new Structure(
                    node,
                    fullClassName,
                    classType,
                    detectVisibility(node),
                    parent,
                    visitors)

              } else {

                // Determine artifact types
                val classType = node.getType match {
                  case CLASS_DECLARATION  => ArtifactType.CLASS
                  case INTERFACE          => ArtifactType.INTERFACE
                  case ENUM               => ArtifactType.ENUM
                  case ANNOTATION_DECL    => ArtifactType.ANNOTATION
                  case _ => throw new IllegalStateException("Unexpected class type: " + node.getType)
                }

                val visitors = createStructureVisitors(classType, changedEntity, Some(fullClassName))
                new Structure(node, fullClassName, classType, detectVisibility(node), parent, visitors)
              }
          }

          structures(node) = structure
          structureStack.push(structure)
        }

        case ENUM_CLASS_BODY => {
          val enumDecl = node.getParent

          // Lookup parent
          val parent = structureStack.top

          // Lookup enum identifier
          val enumName = findIdentifier(enumDecl) match {
            case Some(i) => parent.name + "." + i.getText
            case None    => parent.name + ".[NO IDENTIFIER FOUND]"
          }

          // Create new structure
          val visitors = createStructureVisitors(ArtifactType.ENUM_CONST, changedEntity, Some(enumName))
          val structure =
            new Structure(
                enumDecl,
                enumName,
                ArtifactType.ENUM_CONST,
                detectVisibility(node),
                Some(parent),
                visitors)
          structures(enumDecl) = structure
          structureStack.push(structure)
        }

        case CONSTRUCTOR_DECL => {

          // Lookup parent
          val parent = structureStack.top

          // Create new structure
          val constructorSig =
            addToSignature(node.getChildren.toIndexedSeq, parent.name)
          val visitors = createStructureVisitors(ArtifactType.CONSTRUCTOR, changedEntity, Some(constructorSig))
          val structure =
            new Structure(
                node,
                constructorSig,
                ArtifactType.CONSTRUCTOR,
                detectVisibility(node),
                Some(parent),
                visitors)
          structures(node) = structure
          structureStack.push(structure)
        }

        case ANONYMOUS_CLASS_CONSTRUCTOR_CALL => {

          // Lookup parent
          val parent = structureStack.top

          // Create new structure
          val anonClassName = parent.name + ".ANON[" + parent.anonClassCount + "]"
          val visitors = createStructureVisitors(ArtifactType.ANON_INNER_CLASS, changedEntity, Some(anonClassName))
          val structure =
            new Structure(
                node,
                anonClassName,
                ArtifactType.ANON_INNER_CLASS,
                Visibility.ANONYMOUS,
                Some(parent),
                visitors)
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
          val visitors = createStructureVisitors(ArtifactType.METHOD, changedEntity, Some(methodName))
          val structure =
            new Structure(node, methodName, ArtifactType.METHOD, detectVisibility(node), Some(parent), visitors)
          structures(node) = structure
          structureStack.push(structure)
        }

        case _ => // Silently ignore all other tokens
    }

    // Add comment count and length to top most structure on stack, because this can not be covered by comment visitor
    if (structureStack.isEmpty) {
      (node.getPrecedingComments ++ node.getFollowingComments).foreach { t =>
        t.getType match {
          case JavaLexer.LINE_COMMENT => {
            _lineCommentCount += 1
            _lineCommentLength += t.getText.length
          }
          case JavaLexer.BLOCK_COMMENT => {
            _blockCommentCount += 1
            _blockCommentLines += t.getText.linesIterator.size
            _blockCommentLength += t.getText.length
          }
          case JavaLexer.JAVADOC_COMMENT => {
            _javadocCommentCount += 1
            _javadocCommentLines += t.getText.linesIterator.size
            _javadocCommentLength += t.getText.length
          }
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


  /** Does return the module name for the analyzed artifact.
    *
    * The module name is a computed property. It is constructed from the changed entity path which is the
    * module base name. The artifacts package name is converted back into a path structure and striped from
    * the path. The returned module name contains only the source independent parts of the artifact path.
    **/
  def getModule: Option[String] = {
    packageName match {
      case Some(name) => Some(determineModuleName(changedEntity, name))
      case None       => None
    }
  }


  /** Does return the package name for the analyzed artifact if a package was found. */
  def getPackage: Option[String] = packageName


  /** Does return the unique name of the main artifact (usually a class or interface)
    * or an empty string if none was found.
    */
  def getMainArtifact: Option[Artifact] = structures.values.collectFirst {
    case s if (s.artifactType == ArtifactType.ARTIFACT_CLASS
          || s.artifactType == ArtifactType.ARTIFACT_INTERFACE
          || s.artifactType == ArtifactType.ARTIFACT_ENUM
          || s.artifactType == ArtifactType.ARTIFACT_ANNOTATION) => {
      Artifact(
          s.name,
          s.artifactType,
          s.visibility,
          s.node.getLine,
          s.node.getLastLine,
          packageName.getOrElse(""),
          changedEntity.path + "$" + s.name)
    }
  }


  /** Does return a map with all found artifacts and their measured values.
    *
    * Note: The values do depend on the additional visitors that were registered with this structural
    * visitor.
    */
  def getArtifacts: Map[Artifact, Set[Value]] = structures.values.map { s =>
    val cea =
      Artifact(s.name,
        s.artifactType,
        s.visibility,
        s.node.getLine,
        s.node.getLastLine,
        packageName.getOrElse(""),
        changedEntity.path + "$" + s.name)
    (cea, s.visitors flatMap { _.measuredValues() })
  }.toMap


  /** Once a package declaration was found use this method to determine the module name for this artifact */
  private def determineModuleName(entity: Change, pkgName: String): String = {
    val packageWithEntity = (if (!pkgName.isEmpty) pkgName + "." else "").replace('.', '/') + entity.name
    if (entity.path.endsWith(packageWithEntity)) {
      val packageStart = entity.path.lastIndexOf(packageWithEntity)
      entity.path.substring(0, packageStart)
    } else {
      // FIXME This does not work for test classes with incorrect package path
      /*throw new StructureException(
        entity,
        "Package '%s' is not encoded in path '%s'".format(pkgName, entity.path))*/
      ""
    }
  }


  /** This method provides a save way to get a set with newly created visitors for a detected structure */
  private def createStructureVisitors(
      artifactType: ArtifactType,
      entity: Change,
      artifactName: Option[String]): Set[TreeVisitor] = {

    // Check if artifact type is already present in visitor factories list - if not create it!
    if (!visitorFactories.contains(artifactType)) {
      visitorFactories(artifactType) = collection.mutable.Set.empty[TreeVisitorFactory]
    }

    visitorFactories(artifactType).map { v =>
      v.createVisitor(changedEntity, artifactName)
    }.toSet
  }

  @tailrec
  private def addToSignature(nodes: IndexedSeq[EnhancedCommonTree], signature: String): String = {

    // Special abort case for annotation, since they do not have a formal parameter list!
    if (nodes.isEmpty) return signature + "()"

    // Find signature
    nodes.get(0).getType match {
      case IDENT =>
        val sig = signature + nodes.get(0).getText
        addToSignature(nodes.tail, sig)
      case FORMAL_PARAM_LIST =>
        val types =
          if (nodes.get(0).getChildren != null)
            addToParamList(nodes.get(0).getChildren.toIndexedSeq, "")
          else ""
        signature + "(" + types + ")"
      case _ => addToSignature(nodes.tail, signature)
    }
  }


  @tailrec
  private def addToParamList(params: IndexedSeq[EnhancedCommonTree], paramList: String): String = {
    params.get(0).getType match {
      case FORMAL_PARAM_VARARG_DECL =>
        val reversed = stringifyNodes(params.get(0).getChildren.toIndexedSeq, "").split(' ').reverse
        val param = reversed.updated(1, reversed(1) + "...").reverse.foldLeft("")((cur, elem) => cur + " " + elem)
        val p = paramList + param
        if (params.length == 1) p.trim
        else addToParamList(params.tail, p.trim + ", ")
      case FORMAL_PARAM_STD_DECL =>
        val p = paramList + stringifyNodes(params.get(0).getChildren.toIndexedSeq, "")
        if (params.length == 1) p.trim
        else addToParamList(params.tail, p.trim + ", ")
      case _ =>
        addToParamList(params.tail, paramList.trim)
    }
  }


  @tailrec
  private def stringifyNodes(nodes: IndexedSeq[EnhancedCommonTree], text: String): String = {
    var newText = text

    val node = nodes.get(0)
    val leftNodes =
      if (node.getChildren != null) {
        node.getChildren.toIndexedSeq ++ nodes.tail
      } else {
        newText =
          node.getType match {
            case LOCAL_MODIFIER_LIST | SEMI => text
            case SUPER | EXTENDS => text + " " + node.getText
            case IDENT => {
              text + {
                if (!text.isEmpty && text.last != '.' && text.last != '<') " "
                else ""
              } + node.getText
            }
            case _ => text + node.getText
          }
        nodes.tail
      }

    if (leftNodes.size == 0) newText
    else stringifyNodes(leftNodes, newText)
  }


  /** Use this method to detect the identifier of a certain node among its children. */
  private def findIdentifier(node: EnhancedCommonTree): Option[EnhancedCommonTree] = {
    node.getChildren find { _.getType == IDENT }
  }


  /** Run this method to detect the visibility of a structural node. A structural node might be a
    * class, interface, enum or annotation or a method call.
    *
    * This method does look for a modifier list, if there is none the structure is package-protected
    * - no keyword is defined at all. If there is a modifier list, it still might contain no children
    * which results in package-protected visibility, too. Otherwise we look for the actual visibility
    * modifier and return an appropriate visibility or if none of the standard visibility modifiers
    * are found we end up with package-protected visibility, again.
    */
  private def detectVisibility(node: EnhancedCommonTree): Visibility = {
    node.getChildren find { _.getType == MODIFIER_LIST } match {
      case Some(list) => {

        // In case there are no modifiers at all
        if (list.getChildren == null) return Visibility.PACKAGE

        // Otherwise pick a visibility modifier or return package visibility if there is none specified
        list.getChildren find {
          _.getType match {
            case PUBLIC | PROTECTED | PRIVATE => true
            case _                            => false
          }
        } match {
          case Some(n) => n.getType match {
            case PUBLIC    => Visibility.PUBLIC
            case PROTECTED => Visibility.PROTECTED
            case PRIVATE   => Visibility.PRIVATE
            case _         => Visibility.PACKAGE
          }
          case None    => Visibility.PACKAGE
        }
      }
      case None       => Visibility.PACKAGE
    }

  }
}
