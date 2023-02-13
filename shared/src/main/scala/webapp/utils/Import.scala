package webapp.utils
import org.scalajs.dom.console
import webapp.Repositories
import webapp.repo.Repository
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import scala.collection.mutable
import webapp.entity.Entity
import webapp.repo.Synced

def exportIndexedDBJson(using repositories: Repositories): String = {
  implicit val codec: JsonValueCodec[mutable.Map[String, String]] = JsonCodecMaker.make
  var exp: mutable.Map[String, String] = mutable.Map()

  repositories.productIterator.foreach(f => {
    f match {
      case repository: Repository[?] => {
        val name: String = repository.name
        var values: mutable.Map[String, String] = mutable.Map()

        // repository.all.now.foreach(f => values += (f.id -> writeToString(f.signal.now)))
        exp += (name -> writeToString(repository)(using repository.magicCodec))
      }
      case _ => {}
    }
  })

  return writeToString(exp)
}

def importIndexedDBJson(json: String): Unit = {
  console.log(json)
}

// repository.getOrCreate
