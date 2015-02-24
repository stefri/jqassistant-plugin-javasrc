package de.uniulm.iai.jqassistant.javasrc.plugin.scanner

import java.io.InputStreamReader

import com.buschmais.jqassistant.core.scanner.api.{Scanner, Scope}
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource
import de.uniulm.iai.comma.measurement.processor.JavaMeasurement
import de.uniulm.iai.jqassistant.javasrc.plugin.model.JavaCompilationUnitDescriptor
import org.apache.commons.logging.LogFactory

class JavaSourceScannerPlugin extends AbstractScannerPlugin[FileResource, JavaCompilationUnitDescriptor] {
  private val logger = LogFactory.getLog(getClass)

  override def accepts(item: FileResource, path: String, scope: Scope): Boolean = path.endsWith(".java")

  override def scan(item: FileResource, path: String, scope: Scope, scanner: Scanner): JavaCompilationUnitDescriptor = {
    val store = scanner.getContext.getStore

    // Create a node representing the file itself
    val compilationUnitDescriptor = store.create(classOf[JavaCompilationUnitDescriptor])
    compilationUnitDescriptor.setFileName(item.getFile.getName)

    // Setup and run java measurement processor
    val reader = new InputStreamReader(item.createStream())
    val proc = new JavaMeasurement(compilationUnitDescriptor, reader, path)
    val results = proc.run()

    // Process results
    results.map { r =>
      // FIXME
    }


    logger.info(results)

    compilationUnitDescriptor
  }
}
