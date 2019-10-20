package it.agilelab.utils.scrupulous

import java.nio.file.Path

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, ValidatedNel}
import cats.implicits._
import com.monovore.decline.{Argument, Command, Opts}
import com.typesafe.config.{ConfigValue, ConfigValueFactory}

object ScrupulousConfigArgs {
  implicit val stringConfValueArg: Argument[(String, ConfigValue)] = new Argument[(String, ConfigValue)] {
    override def read(s: String): ValidatedNel[String, (String, ConfigValue)] = {
      val splits = s.split("=")
      if (splits.isEmpty) {
        Invalid(NonEmptyList.of(s"String was empty")) // I don't think this is possible tho
      } else if (splits.length > 2) {
        Invalid(NonEmptyList.of(s"$s contains more than one = character, only one is accepted"))
      } else if (splits.head.isEmpty) {
        Invalid(NonEmptyList.of(s"$s does not have a key"))
      } else {
        Valid(splits.head -> ConfigValueFactory.fromAnyRef(splits.lift(1).getOrElse("")))
      }
    }

    override def defaultMetavar: String = "key=value"
  }

  private val templatePathOpt =
    Opts.option[Path](long = "template", short = "t", help = "The template conf to parse")

  private val confPathOpt =
    Opts.option[Path](long = "config", short = "c", help = "The conf to resolve")

  private val defaultValuesOpt =
    Opts
      .options[(String, ConfigValue)](
        long = "defaultValue",
        short = "d",
        help = "Default values to be injected in the resolved config. " +
          "The format must be key=value. Only unquoted strings are supported"
      )
      .orEmpty

  private val pathsToIgnoreOpt =
    Opts
      .options[String](
        long = "pathToIgnore",
        short = "i",
        help = "Config paths that have to be ignored during comparison"
      )
      .orEmpty

  private val verboseOpt = Opts.flag("verbose", short = "v", help = "Verbose mode").orFalse

  private val overwriteOpt =
    Opts.flag("overwrite", short = "o", help = "Overwrite the template with the resolved conf").orFalse

  private val opts =
    (templatePathOpt, confPathOpt, defaultValuesOpt, pathsToIgnoreOpt, verboseOpt, overwriteOpt).mapN(Arguments)

  case class Arguments(
      templatePath: Path,
      confPath: Path,
      defaultValues: List[(String, ConfigValue)],
      pathsToIgnore: List[String],
      verbose: Boolean,
      overwrite: Boolean
  )

  val command: Command[Arguments] = Command(
    name = "ScrupulousConfig",
    header = "Outputs diffs between a rendered (template) typesafe config and another config still to be " +
      "resolved. The template one does not get resolved, only parsed, while the second one is resolved (i.e. through " +
      "reference.conf files found in the classpath and so on, like in your regular final application)."
  )(opts)

}
