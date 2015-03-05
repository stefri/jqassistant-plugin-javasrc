package de.uniulm.iai.jqassistant.javasrc.plugin.scanner

import java.io.InputStreamReader

import com.buschmais.jqassistant.core.scanner.api.{Scanner, Scope}
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource
import de.uniulm.iai.comma.measurement.processor.JavaMeasurement
import de.uniulm.iai.jqassistant.javasrc.plugin.api.scanner.JavaSourceScope
import de.uniulm.iai.jqassistant.javasrc.plugin.model.JavaCompilationUnitDescriptor

/**
 * Implementation of the {@link AbstractScannerPlugin} for java source code.
 *
 * @author Steffen Kram
 */
class JavaSourceScannerPlugin extends AbstractScannerPlugin[FileResource, JavaCompilationUnitDescriptor] {

  override def accepts(item: FileResource, path: String, scope: Scope): Boolean = {
    JavaSourceScope.CLASSPATH.equals(scope) && path.toLowerCase.endsWith(".java")
  }

  override def scan(item: FileResource, path: String, scope: Scope, scanner: Scanner): JavaCompilationUnitDescriptor = {
    val helper = ScannerHelper(scanner.getContext)
    val reader = new InputStreamReader(item.createStream())
    JavaMeasurement(helper, item, path).run()
  }
}
