package com.mysema.scalagen

case class ConversionSettings(splitLongLines: Boolean = true)

object ConversionSettings {
  def defaultSettings = ConversionSettings()
}