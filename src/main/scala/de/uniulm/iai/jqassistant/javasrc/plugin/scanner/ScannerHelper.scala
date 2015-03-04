package de.uniulm.iai.jqassistant.javasrc.plugin.scanner

import com.buschmais.jqassistant.core.scanner.api.ScannerContext
import de.uniulm.iai.jqassistant.javasrc.plugin.api.scanner.{TypeResolver, TypeCache}
import de.uniulm.iai.jqassistant.javasrc.plugin.model._


object ScannerHelper {
  def apply(scannerContect: ScannerContext) = new ScannerHelper(scannerContect)
}


/**
 * Containing helper methods for the java source scanner.
 *
 * @author Steffen Kram
 */
class ScannerHelper(scannerContext: ScannerContext) {

  def compilationUnit(fullQualifiedName: String, name: String): JavaCompilationUnitDescriptor = {
    val compilationUnit = scannerContext.getStore.create(classOf[JavaCompilationUnitDescriptor])
    compilationUnit.setFullQualifiedName(fullQualifiedName)
    compilationUnit.setFileName(name)
    compilationUnit.setMayCompile(true)
    compilationUnit
  }

  /** Return the type descriptor for the given type name. */
  def resolveType(fullQualifiedName: String, dependentType: TypeCache.CachedType[_ <: TypeDescriptor]):
      TypeCache.CachedType[TypeDescriptor] = {
    val cachedType = typeResolver.resolve(fullQualifiedName, scannerContext)
    if (!dependentType.equals(cachedType) && dependentType.getDependency(fullQualifiedName) == null) {
      val dependency = cachedType.getTypeDescriptor()
      dependentType.addDependency(fullQualifiedName, dependency)
      dependentType.getTypeDescriptor.getDependencies.add(dependency)
    }
    cachedType
  }


  /** Return the type descriptor for the given type name. */
  def createType[T <: TypeDescriptor](fullQualifiedName: String, descriptorType: Class[T]): TypeCache.CachedType[T] = {
    typeResolver.create(fullQualifiedName, descriptorType, scannerContext)
  }


  /**
   * Return the type resolver.
   *
   * Looks up an instance in the scanner context. If none can be found the
   * default resolver is used.
   *
   * @return The type resolver.
   */
  private def typeResolver = {
    Option(scannerContext.peek(classOf[TypeResolver])).getOrElse {
      throw new IllegalStateException("Cannot get type resolver")
    }
  }


  /**
   * Return the method descriptor for the given type and method signature.
   *
   * @param cachedType
   *                  The containing type.
   * @param signature
   *                  The method signature.
   * @return The method descriptor.
   */
  def methodDescriptor(cachedType: TypeCache.CachedType[_ <: TypeDescriptor], signature: String):
      MethodDescriptor = {
    Option(cachedType.getMethod(signature)).getOrElse {
      val methodDescriptor = scannerContext.getStore.create(classOf[MethodDescriptor])
      methodDescriptor.setSignature(signature)
      cachedType.getTypeDescriptor.getDeclaredMethods.add(methodDescriptor)
      cachedType.addMember(signature, methodDescriptor)
      methodDescriptor
    }
  }


  /**
   * Return the constructor descriptor for the given type and constructor signature.
   *
   * @param cachedType
   *                  The containing type.
   * @param signature
   *                  The method signature.
   * @return The constructor descriptor.
   */
  def constructorDescriptor(cachedType: TypeCache.CachedType[_ <: TypeDescriptor], signature: String):
      ConstructorDescriptor = {
    Option(cachedType.getConstructor(signature)).getOrElse {
      val constructorDescriptor = scannerContext.getStore.create(classOf[ConstructorDescriptor])
      constructorDescriptor.setSignature(signature)
      cachedType.getTypeDescriptor.getDeclaredConstructors.add(constructorDescriptor)
      cachedType.addMember(signature, constructorDescriptor)
      constructorDescriptor
    }
  }


  /**
   * Return the field descriptor for the given type and field signature.
   *
   * @param cachedType
   *                  The containing type.
   * @param signature
   *                  The field signature.
   * @return The field descriptor.
   */
  def fieldDescriptor(cachedType: TypeCache.CachedType[_ <: TypeDescriptor], signature: String):
      FieldDescriptor = {
    Option(cachedType.getField(signature)).getOrElse {
      val fieldDescriptor = scannerContext.getStore.create(classOf[FieldDescriptor])
      fieldDescriptor.setSignature(signature)
      cachedType.getTypeDescriptor.getDeclaredFields.add(fieldDescriptor)
      cachedType.addMember(signature, fieldDescriptor)
      fieldDescriptor
    }
  }

}
