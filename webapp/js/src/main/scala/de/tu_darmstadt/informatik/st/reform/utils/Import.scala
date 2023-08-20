package de.tu_darmstadt.informatik.st.reform.utils
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.core.*
import de.tu_darmstadt.informatik.st.reform.JSImplicits
import de.tu_darmstadt.informatik.st.reform.Repositories
import de.tu_darmstadt.informatik.st.reform.*
import de.tu_darmstadt.informatik.st.reform.given_ExecutionContext
import de.tu_darmstadt.informatik.st.reform.repo.RepoAndValues
import de.tu_darmstadt.informatik.st.reform.repo.Repository

import scala.collection.mutable
import scala.concurrent.Future

def exportIndexedDBJson(using jsImplicits: JSImplicits): String = {

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
  writeToString(jsImplicits.repositories)(using repositoryCodec)
}

def importIndexedDBJson(
    json: String,
)(using jsImplicits: JSImplicits): Future[scala.collection.mutable.Iterable[Repository[?]]] = {
  Future {
    var repositoryCodecs: mutable.Map[String, Repository[?]] = mutable.Map()
    jsImplicits.repositories.productIterator.foreach {
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
                mb += (key -> repository.decodeRepository(in))
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
