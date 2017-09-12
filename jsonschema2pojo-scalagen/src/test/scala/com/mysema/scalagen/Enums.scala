package com.mysema.scalagen

object Gender extends Enumeration {
  val MALE = new Gender(0)
  val FEMALE = new Gender(1)
}

class Gender(val id: Int) extends Gender.Value {
  
}