package webapp.utils
import webapp.Repositories
import webapp.repo.Repository
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import scala.collection.mutable
import webapp.*
import scala.annotation.nowarn

def exportIndexedDBJson(using repositories: Repositories): String = {
  given repositoryCodec: JsonValueCodec[Repositories] =
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
  return writeToString(repositories)(using repositoryCodec)
}

def importIndexedDBJson(
    json: String,
)(using repositories: Repositories): Unit = {

  var repositoryCodecs: mutable.Map[String, Repository[?]] = mutable.Map()
  repositories.productIterator.foreach(value => {
    value match {
      case repository: Repository[?] => {
        repositoryCodecs += (repository.name -> repository)
      }
      case _ => {}
    }
  })

  given repositoryMapCodec: JsonValueCodec[mutable.Map[String, Repository[?]]] =
    new JsonValueCodec {
      def encodeValue(x: mutable.Map[String, Repository[?]], out: JsonWriter): Unit = ???

      def decodeValue(
          in: JsonReader,
          default: mutable.Map[String, Repository[?]],
      ): mutable.Map[String, Repository[?]] = {
        if (in.isNextToken('{')) {
          if (in.isNextToken('}')) default
          else {
            in.rollbackToken()
            val mb = mutable.Map.newBuilder[String, Repository[?]]
            while ({
              val key = in.readKeyAsString()
              val repository = repositoryCodecs.get(key).get
              mb += (key -> repository.magicCodec.decodeValue(in, repository)): @nowarn
              in.isNextToken(',')
            }) ()
            if (in.isCurrentToken('}')) mb.result()
            else in.arrayEndOrCommaError()
          }
        } else in.readNullOrTokenError(default, '{')
      }

      def nullValue: mutable.Map[String, Repository[?]] = {
        mutable.Map()
      }
    }

  readFromString(json)(using repositoryMapCodec): @nowarn
}
