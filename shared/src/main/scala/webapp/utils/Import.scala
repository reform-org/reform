package webapp.utils
import org.scalajs.dom.console
import webapp.Repositories
import webapp.repo.Repository
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec

def exportIndexedDBJson(using repositories: Repositories): String = {

  given theEverythingCodec: JsonValueCodec[Repositories] =
    new JsonValueCodec {
      def encodeValue(x: Repositories, out: JsonWriter): Unit = {
        out.writeObjectStart()
        x.productIterator.foreach(f => {
          f match {
            case repository: Repository[?] => {
              out.writeKey(repository.name)
              repository.magicCodec.encodeValue(repository, out)
            }
            case _ => {}
          }
        })
        out.writeObjectEnd()
      }

      def decodeValue(in: JsonReader, default: Repositories): Repositories = ???

      def nullValue: Repositories = ???
    }

  return writeToString(repositories)(using theEverythingCodec)
}

def importIndexedDBJson(json: String): Unit = {
  console.log(json)
}

// repository.getOrCreate
