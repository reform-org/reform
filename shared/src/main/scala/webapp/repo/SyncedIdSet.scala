package webapp.repo

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import concurrent.ExecutionContext.Implicits.global
import webapp.*
import concurrent.ExecutionContext.Implicits.global
import rescala.default.*

case class SyncedIdSet(name: String) {

  implicit val codecDeltaForGrowOnlySetString: JsonValueCodec[DeltaFor[GrowOnlySet[String]]] = JsonCodecMaker.make

  private val syncer = Syncer[GrowOnlySet[String]](name + "-ids")

  private val synced = syncer.sync("ids", GrowOnlySet.empty)

  def add(id: String): Unit = {
    synced.update(_.add(id))
  }

  val ids: Signal[Set[String]] =
    synced.signal.map(_.set)

  def syncWithRepo(repo: IdSetRepository): Unit = {
    repo.get.map(ids => {
      synced.update(_.union(ids))
    })
    synced.signal.observe(repo.set)
  }
}
