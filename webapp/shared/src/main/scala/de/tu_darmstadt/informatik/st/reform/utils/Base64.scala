package de.tu_darmstadt.informatik.st.reform.utils

import java.nio.charset.StandardCharsets

object Base64 {

  private val encoder = java.util.Base64.getEncoder.nn
  private val decoder = java.util.Base64.getDecoder.nn

  private val utf8 = StandardCharsets.UTF_8

  def encode(s: String): String = encoder.encodeToString(s.getBytes(utf8)).nn

  def decode(s: String): String = String(decoder.decode(s), utf8)

}
