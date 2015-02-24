package de.uniulm.iai.comma

package object model {

  trait Enum[A] {
    trait Value { self: A => }
    def values: IndexedSeq[A]
  }

}

