package it.agilelab.utils.scrupulous

import java.nio.file.Path

import cats.Applicative
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.syntax.validated._
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions, ConfigValue}
import gnieh.diffson.sprayJson._
import it.agilelab.utils.scrupulous.ScrupulousConfigOutput._

import scala.util.{Failure, Success, Try}

class ScrupulousConfigLogicImpl() extends ScrupulousConfigLogic {

  def apply(
      templatePath: Path,
      confPath: Path,
      defaultValues: List[(String, ConfigValue)],
      pathsToIgnore: List[String],
      verbose: Boolean
  ): ScrupulousConfigOutput = {

    val initialConf = withValues(ConfigFactory.empty(), defaultValues)

    val resolvedConf = resolveConf(
      confPath,
      c => withouthPaths(ConfigFactory.load().withFallback(initialConf).withFallback(c), pathsToIgnore)
    )

    val resolvedTemplate =
      resolveConf(templatePath, c => withouthPaths(ConfigFactory.load().withFallback(c), pathsToIgnore))

    type Error = (Path, Throwable)
    type ErrorOr[A] = ValidatedNel[Error, A]

    Applicative[ErrorOr]
      .map2(resolvedConf, resolvedTemplate) { (parsedConf, parsedTemplate) =>
        val renderedFinalConf = render(parsedConf)
        val renderedOldConf = render(parsedTemplate)
        renderedFinalConf -> JsonDiff.diff(renderedFinalConf, renderedOldConf, remember = false)
      }
      .fold(
        errors => CannotParse.apply(errors.toList), { ok =>
          if (ok._2.ops.isEmpty) {
            NoDiffs(ok._1)
          } else {
            HasDiffs(ok._1, ok._2)
          }
        }
      )
  }

  private def withValues(conf: Config, values: List[(String, ConfigValue)]): Config = {
    values.foldLeft(conf) {
      case (c, (p, v)) =>
        c.withValue(p, v)
    }
  }

  private def withouthPaths(conf: Config, paths: List[String]): Config = {
    paths.foldLeft(conf) {
      case (c, p) =>
        c.withoutPath(p)
    }
  }

  private def render(conf: Config): String = {
    conf.root().render(ConfigRenderOptions.defaults().setComments(false).setOriginComments(false))
  }

  private def resolveConf(p: Path, transformF: Config => Config): Validated[NonEmptyList[(Path, Throwable)], Config] =
    Try(transformF(ConfigFactory.parseFile(p.toFile)).resolve()) match {
      case Failure(e)     => (p, e).invalidNel
      case Success(value) => value.valid
    }
}
