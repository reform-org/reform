package webapp.utils

import java.nio.charset.StandardCharsets

object Base64 {

  private val encoder = java.util.Base64.getEncoder
  private val decoder = java.util.Base64.getDecoder

  private val utf8 = StandardCharsets.UTF_8

  def encode(s: String): String = encoder.encodeToString(s.getBytes(utf8))

  def decode(s: String): String = String(decoder.decode(s), utf8)

}
