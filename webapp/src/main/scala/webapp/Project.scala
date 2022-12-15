package webapp

import kofre.datatypes.PosNegCounter
import kofre.base.{Bottom, DecomposeLattice, Defs}
import kofre.base.Defs.Id
import kofre.dotted.DottedDecompose
import kofre.syntax.PermIdMutate.withID
import kofre.syntax.{ArdtOpsContains, DottedName, OpsSyntaxHelper, PermId, PermIdMutate, PermQuery}
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegister
import kofre.decompose.interfaces.LWWRegisterInterface
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegisterSyntax
import kofre.dotted.Dotted
import kofre.datatypes.PosNegCounter.PNCounterSyntax
import org.scalajs.dom.svg.Defs
import kofre.decompose.containers.DeltaBufferRDT
import kofre.datatypes.TimedVal
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import webapp.Codecs.myReplicaID

given [A]: Bottom[Option[TimedVal[A]]] = new Bottom[Option[TimedVal[A]]] {
  def empty = None
}

case class Project(
    _name: Option[TimedVal[String]],
    _maxHours: PosNegCounter,
    _accountName: Option[TimedVal[Option[String]]],
) derives DecomposeLattice,
      Bottom {

  def withName(name: String) = {
    val diffSetName = Project.empty.copy(_name = Some(TimedVal(name, myReplicaID)))

    this.merge(diffSetName)
  }

  def withAccountName(accountName: Option[String]) = {
    val diffSetAccountName = Project.empty.copy(_accountName = Some(TimedVal(accountName, myReplicaID)))

    this.merge(diffSetAccountName)
  }

  def withAddedMaxHours(addedMaxHours: Int) = {
    val diffSetMaxHours =
      Project.empty.copy(_maxHours = _maxHours.add(addedMaxHours)(using PermIdMutate.withID(myReplicaID)))

    this.merge(diffSetMaxHours) // TODO FIXME probably also use this mutator thing - also to ship deltas to remotes
  }

  def name = {
    _name.map(_.value).getOrElse("not initialized")
  }

  def maxHours = {
    _maxHours.value
  }

  def accountName = {
    _accountName.map(_.value.getOrElse("no account")).getOrElse("not initialized")
  }
}

object Project {
  val empty: Project = Project(None, PosNegCounter.zero, None)

  implicit val codec: JsonValueCodec[Project] = JsonCodecMaker.make
}
