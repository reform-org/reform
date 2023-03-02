package webapp.utils
import webapp.Repositories
import webapp.repo.{RepoAndValues, Repository}
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import scala.collection.mutable
import webapp.*
import scala.annotation.nowarn
import scala.concurrent.Future
import webapp.given_ExecutionContext

def exportIndexedDBJson(using repositories: Repositories): String = {

  given repositoryCodec: JsonValueCodec[Repositories] =
    new JsonValueCodec {
      def encodeValue(x: Repositories, out: JsonWriter): Unit = {
        out.writeObjectStart()
        x.productIterator.foreach {
          case repository: Repository[?] => {
            out.writeKey(repository.name)
            repository.encodeRepository(out)
          }
          case _ => {}
        }
        out.writeObjectEnd()
      }

      def decodeValue(in: JsonReader, default: Repositories): Repositories = ???

      def nullValue: Repositories = ???
    }
  writeToString(repositories)(using repositoryCodec)
}

def importIndexedDBJson(
    json: String,
)(using repositories: Repositories): Future[scala.collection.mutable.Iterable[Repository[?]]] = {
  Future {
    var repositoryCodecs: mutable.Map[String, Repository[?]] = mutable.Map()
    repositories.productIterator.foreach {
      case repository: Repository[?] => {
        repositoryCodecs += (repository.name -> repository)
      }
      case _ => {}
    }

    given repositoryMapCodec: JsonValueCodec[mutable.Map[String, (Repository[?], mutable.Map[String, ?])]] =
      new JsonValueCodec {
        def encodeValue(x: mutable.Map[String, (Repository[?], mutable.Map[String, ?])], out: JsonWriter): Unit = ???

        def decodeValue(
            in: JsonReader,
            default: mutable.Map[String, (Repository[?], mutable.Map[String, ?])],
        ): mutable.Map[String, (Repository[?], mutable.Map[String, ?])] = {
          if (in.isNextToken('{')) {
            if (in.isNextToken('}')) default
            else {
              in.rollbackToken()
              val mb = mutable.Map.newBuilder[String, (Repository[?], mutable.Map[String, ?])]
              while ({
                val key = in.readKeyAsString()
                val repository = repositoryCodecs(key)
                mb += (key -> repository.decodeRepository(in)): @nowarn
                in.isNextToken(',')
              }) ()
              if (in.isCurrentToken('}')) mb.result()
              else in.arrayEndOrCommaError()
            }
          } else in.readNullOrTokenError(default, '{')
        }

        def nullValue: mutable.Map[String, (Repository[?], mutable.Map[String, ?])] = {
          mutable.Map()
        }
      }

    readFromString(json)(using repositoryMapCodec)
  }.flatMap(result => {
    val res = Future
      .sequence(
        result.map((_, repoAndValues) => {
          repoAndValues match {
            case repoAndValues: RepoAndValues[?] => {
              Future
                .sequence(repoAndValues._2.map((k, v) => {
                  repoAndValues._1
                    .getOrCreate(k)
                    .flatMap(
                      _.update(old =>
                        repoAndValues._1
                          .latticeMerge(old.getOrElse(repoAndValues._1.bottomEmpty), v),
                      ),
                    )
                }))
                .map(_ => repoAndValues._1)
            }
          }
        }),
      )
    res
  })
}
