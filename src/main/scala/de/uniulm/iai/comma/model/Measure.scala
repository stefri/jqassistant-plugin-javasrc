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
package de.uniulm.iai.comma.model


/** This analyzer gathers multiple measures for each changed file. This object
  * represents a generic measure.
  */
sealed trait Measure extends Measure.Value {
  def name: String
  def unit: String
  def description: Option[String]
}

object Measure extends Enum[Measure] {

  case object LINES extends Measure {
    val name = "Lines"
    val unit = "# of lines"
    val description = Some("Lines in a source file")
  }

  case object LOC extends Measure {
    val name = "Lines of Code"
    val unit = "loc"
    val description = Some("Raw lines of code")
  }

  case object NCSCSS extends Measure {
    val name = "NCSCSS"
    val unit = "ncscss"
    val description = Some("Non-commenting, state changing source statements")
  }

  case object LINE_COMMENT_COUNT extends Measure {
    val name = "Line Comment Count"
    val unit = "# of line comments"
    val description = Some("Aggregated number of line comments")
  }

  case object LINE_COMMENT_LENGTH extends Measure {
    val name = "Line Comment Length"
    val unit = "# of chars"
    val description = Some("Aggregated number of chars for all line comments")
  }

  case object BLOCK_COMMENT_COUNT extends Measure {
    val name = "Block Comment Count"
    val unit = "# of block comments"
    val description = Some("Total number of block comment statements")
  }

  case object BLOCK_COMMENT_LINES extends Measure {
    val name = "Block Comment Lines"
    val unit = "# of lines"
    val description = Some("Aggregated number of lines for all block comments")
  }

  case object BLOCK_COMMENT_LENGTH extends Measure {
    val name = "Block Comment Length"
    val unit = "# of chars"
    val description = Some("Aggregated number of chars for all block comments")
  }

  case object JAVADOC_COUNT extends Measure {
    val name = "Javadoc Comment Count"
    val unit = "# of javadoc comments"
    val description = Some("Total number of javadoc comments")
  }

  case object JAVADOC_LINES extends Measure {
    val name = "Javadoc Comment Lines"
    val unit = "# of lines"
    val description = Some("Aggregated number of lines for all javadoc comments")
  }

  case object JAVADOC_LENGTH extends Measure {
    val name = "Javadoc Comment Length"
    val unit = "# of chars"
    val description = Some("Aggregated number of chars for all javadoc comments")
  }

  case object NODOC_API extends Measure {
    val name = "Non-Javadoc Public API"
    val unit = "methods"
    val description = Some("Total number of public methods that are not documented with javadoc")
  }

  case object DOC_LINES extends Measure {
    val name = "Number of comment lines"
    val unit = "lines"
    val description = Some("Total number of comment lines, includes line and block comments. No Javadoc!")
  }

  case object DOC_LENGTH extends Measure {
    val name = "Length of comment lines"
    val unit = "chars"
    val description = Some("Total numer of chars of comment lines, includes line and block comments. No Javadoc!")
  }

  case object NUM_CLASSES extends Measure {
    val name = "Number of classes"
    val unit = "# of classes"
    val description = Some("Measures the total number of classes")
  }

  case object API_CLASSES extends Measure {
    val name = "Number of api classes"
    val unit = "# of classes"
    val description = Some("Measures the total number of classes of the public API")
  }

  case object NUM_INTERFACES extends Measure {
    val name = "Number of interfaces"
    val unit = "# of interfaces"
    val description = Some("Measures the total number of interfaces")
  }

  case object API_INTERFACES extends Measure {
    val name = "Number of api interfaces"
    val unit = "# of interfaces"
    val description = Some("Measures the total number of interfaces of the public API")
  }

  case object NUM_ENUM extends Measure {
    val name = "Number of enums"
    val unit = "# of enums"
    val description = Some("Measures the total number of enums")
  }

  case object API_ENUM extends Measure {
    val name = "Number of api enums"
    val unit = "# of enums"
    val description = Some("Measures the total number of enums of the public API")
  }

  case object NUM_ANNOTATIONS extends Measure {
    val name = "Number of annotations"
    val unit = "# of annotations"
    val description = Some("Measures the total number of annotations")
  }

  case object API_ANNOTATIONS extends Measure {
    val name = "Number of api annotations"
    val unit = "# of annotations"
    val description = Some("Measures the total number of annotations of the public API")
  }

  case object NUM_METHODS extends Measure {
    val name = "Number of methods"
    val unit = "# of methods"
    val description = Some("Measures the total number of methods")
  }

  case object API_METHODS extends Measure {
    val name = "Number of api methods"
    val unit = "# of methods"
    val description = Some("Measures the total number of methods of the public API")
  }

  case object MODULE_COUNT extends Measure {
    val name = "Module Count"
    val unit = "count"
    val description = Some("Total number of different source modules")
  }

  case object PACKAGE_COUNT extends Measure {
    val name = "Package Count"
    val unit = "count"
    val description = Some("Total number of different package names, only packages containing at least one source file are counted.")
  }

  case object NUM_FILES extends Measure {
    val name = "Number of files"
    val unit = "# of files"
    val description = Some("Measures the total number of files")
  }

  case object FILE_COUNT extends Measure {
    val name = "File count"
    val unit = "count"
    val description = Some("Total number of files, presumably of a given file-type.")
  }

  case object FANOUT extends Measure {
    val name = "Fanout Complexity Number"
    val unit = "# of used classes"
    val description = Some("Total number of used classes")
  }

  case object CCN extends Measure {
    val name = "Cyclomatic Complexity Number"
    val unit = "ccn"
    val description = None
  }

  case object AMW extends Measure {
    val name = "Average Method Weight"
    val unit = "ccn"
    val description = Some("Measures the average static complexity of methods based on McCabe cyclomatic complexity")
  }

  case object WMC extends Measure {
    val name = "Weighted Method Count"
    val unit = "ccn"
    val description = Some("Sum of the static complexity of methods based on McCabe cyclomatic complexity")
  }

  case object CCN_LOW extends Measure {
    val name = "Low Cyclomatic Complexity Class"
    val unit = "count"
    val description = Some("Total number of methods with a low ccn between 1 and 5.")
  }

  case object CCN_NORMAL extends Measure {
    val name = "Normal Cyclomatic Complexity Class"
    val unit = "count"
    val description = Some("Total number of methods with a normal ccn between 6 and 10.")
  }

  case object CCN_HIGH extends Measure {
    val name = "High Cyclomatic Complexity Class"
    val unit = "count"
    val description = Some("Total number of methods with a high ccn between 11 and 20.")
  }

  case object CCN_PROBLEMS extends Measure {
    val name = "Problematic Cyclomatic Complexity Class"
    val unit = "count"
    val description = Some("Total number of methods with a problematic ccn between 21 and 30.")
  }

  case object CCN_ALERTING extends Measure {
    val name = "Alerting Cyclomatic Complexity Class"
    val unit = "count"
    val description = Some("Total number of methods with a alerting ccn above 30.")
  }

  case object NPATH extends Measure {
    val name = "NPath Complexity Number"
    val unit = "npath"
    val description = Some("NPath, measures the execution path complexity")
  }

  case object AVERAGE_NPATH extends Measure {
    val name = "Average NPath"
    val unit = "npath"
    val description = Some("Average NPath of methods")
  }

  case object SUM_NPATH extends Measure {
    val name = "Sum NPath"
    val unit = "npath"
    val description = Some("Sum NPath of methods. Analogous to the WMC of the CCN measure.")
  }

  case object NPATH_MAX_EXCEEDED extends Measure {
    val name = "Exceeded NPath Threshold"
    val unit = "Boolean"
    val description = Some("NPath might produce ridiculus high values. If the given threshold is exceeded this measure is true.")
  }

  case object NPATH_EXCEEDED_COUNT extends Measure {
    val name = "Number of exceeded npath measures"
    val unit = "exceeded methods"
    val description = Some("Sum of methods with exceeded npath measures")
  }

  case object NPATH_LOW extends Measure {
    val name = "Low NPath Class"
    val unit = "count"
    val description = Some("Total number of methods with a low npath value between 1 and 100.")
  }

  case object NPATH_NORMAL extends Measure {
    val name = "Normal NPath Class"
    val unit = "count"
    val description = Some("Total number of methods with a normal npath value between 101 and 200.")
  }

  case object NPATH_HIGH extends Measure {
    val name = "High NPath Class"
    val unit = "count"
    val description = Some("Total number of methods with a high npath value between 201 and 300.")
  }

  case object NPATH_PROBLEMS extends Measure {
    val name = "Problematic NPath Class"
    val unit = "count"
    val description = Some("Total number of methods with a problematic npath value between 301 and 500.")
  }

  case object NPATH_ALERTING extends Measure {
    val name = "Alerting NPath Class"
    val unit = "count"
    val description = Some("Total number of methods with a alerting npath value between 1 and 100. Includes NPath exceeded measure count.")
  }

  case object RULE_VIOLATION_COUNT extends Measure {
    val name = "Rule Violation Count"
    val unit = "count"
    val description = Some("Total number of rule violations of a certain kind.")
  }

  case object RULE_VIOLATION_INC extends Measure {
    val name = "Rule Violation Increased"
    val unit = "count"
    val description = Some("Increased rule violations in entity compared to previous commit")
  }

  case object RULE_VIOLATION_DEC extends Measure {
    val name = "Rule Violation Decreased"
    val unit = "count"
    val description = Some("Decreased rule violations in entity compared to previous commit")
  }

  case object RULE_VIOLATION_DEL extends Measure {
    val name = "Rule Violations Deleted"
    val unit = "count"
    val description = Some("Rule violations removed in entity compared to previous commit due to deletion of files")
  }

  case object RULE_VIOLATION_INTR extends Measure {
    val name = "Rule Violation Types Introduced"
    val unit = "count"
    val description = Some("New rule violation types introduced in entity compared to previous commit (also part of INC")
  }

  case object RULE_VIOLATION_RM extends Measure {
    val name = "Rule Violations Types Removed"
    val unit = "count"
    val description = Some("Exisiting rule violations types removed in entity compared to previous commit (also part of DEC)")
  }

  case object RULE_PRIORITY_COUNT extends Measure {
    val name = "Violation Priority Count"
    val unit = "count"
    val description = Some("Total number of rule violations of a certain priority")
  }

  val values =
    Vector(
      LINES,                  //  0 ca web
      LOC,                    //  1 ca web
      NCSCSS,                 //  2 ca web

      LINE_COMMENT_COUNT,     //  3
      LINE_COMMENT_LENGTH,    //  4
      BLOCK_COMMENT_COUNT,    //  5
      BLOCK_COMMENT_LINES,    //  6
      BLOCK_COMMENT_LENGTH,   //  7
      JAVADOC_COUNT,          //  8
      JAVADOC_LINES,          //  9 ca web
      JAVADOC_LENGTH,         // 10 ca web
      NODOC_API,              // 11
      DOC_LINES,              // 12 ca web
      DOC_LENGTH,             // 13 ca web

      NUM_CLASSES,            // 14 ca web
      NUM_INTERFACES,         // 15 ca web
      NUM_ENUM,               // 16 ca web
      NUM_ANNOTATIONS,        // 17 ca web
      NUM_METHODS,            // 18 ca web
      API_CLASSES,            // 19 ca web
      API_INTERFACES,         // 20 ca web
      API_ENUM,               // 21 ca web
      API_ANNOTATIONS,        // 22 ca web
      API_METHODS,            // 23

      MODULE_COUNT,           // 24 ca web
      PACKAGE_COUNT,          // 25 ca web

      NUM_FILES,              // 26
      FILE_COUNT,             // 27 ca

      FANOUT,                 // 28

      CCN,                    // 29
      AMW,                    // 30
      WMC,                    // 31
      CCN_LOW,                // 32
      CCN_NORMAL,             // 33
      CCN_HIGH,               // 34
      CCN_PROBLEMS,           // 35
      CCN_ALERTING,           // 36

      NPATH,                  // 37
      AVERAGE_NPATH,          // 38
      SUM_NPATH,              // 39
      NPATH_MAX_EXCEEDED,     // 40
      NPATH_EXCEEDED_COUNT,   // 41 ca
      NPATH_LOW,              // 42
      NPATH_NORMAL,           // 43
      NPATH_HIGH,             // 44
      NPATH_PROBLEMS,         // 45
      NPATH_ALERTING,         // 46


      RULE_VIOLATION_COUNT,   // 47 ca
      RULE_PRIORITY_COUNT,    // 48 ca
      RULE_VIOLATION_INC,     // 49 ca
      RULE_VIOLATION_DEC,     // 50 ca
      RULE_VIOLATION_DEL,     // 51 ca
      RULE_VIOLATION_INTR,    // 52 ca
      RULE_VIOLATION_RM       // 53 ca
    )
}
