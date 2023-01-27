/*
Copyright 2022 https://github.com/phisn/ratable, The reform-org/reform contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package webapp.webrtc

import kofre.datatypes.PosNegCounter
import loci.registry.*
import webapp.*
import webapp.Codecs.*
import rescala.default.*
import loci.transmitter.RemoteRef
import loci.communicator.webrtc.WebRTC

class ConnectionInformation (val session: WebRTC.CompleteSession, val alias: String){}

object WebRTCService {
  val registry: Registry = new Registry

  private val removeConnection = Evt[RemoteRef]()
  private val addConnection = Evt[RemoteRef]()
  private val addConnectionB = addConnection act (current[Seq[RemoteRef]] :+ _)
  private val removeConnectionB = removeConnection act (a => current[Seq[RemoteRef]].filter(b => !b.equals(a)))

  val connections = Fold(Seq.empty: Seq[RemoteRef])(addConnectionB, removeConnectionB)
  
  registry.remoteJoined.monitor(addConnection.fire)
  registry.remoteLeft.monitor(removeConnection.fire)

}
