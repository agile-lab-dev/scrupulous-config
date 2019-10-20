package it.agilelab.utils.scrupulous

import java.nio.file.Paths

import cats.data.Validated.Valid
import com.typesafe.config.ConfigValueFactory
import it.agilelab.utils.scrupulous.ScrupulousConfigArgs.Arguments
import zio.test.Assertion._
import zio.test._

object ScrupulousConfigArgsSuite {

  type SimpleSpec = Spec[Any, Nothing, String, Either[TestFailure[Nothing], TestSuccess[Unit]]]

  val helpIsConsistent: SimpleSpec = test("help is consistent") {
    assert(
      ScrupulousConfigArgs.command.showHelp,
      equalTo(
        """Usage: ScrupulousConfig --template <path> --config <path> [--defaultValue <key=value>]... [--pathToIgnore <string>]... [--verbose] [--overwrite]
          |
          |Outputs diffs between a rendered (template) typesafe config and another config still to be resolved. The template one does not get resolved, only parsed, while the second one is resolved (i.e. through reference.conf files found in the classpath and so on, like in your regular final application).
          |
          |Options and flags:
          |    --help
          |        Display this help text.
          |    --template <path>, -t <path>
          |        The template conf to parse
          |    --config <path>, -c <path>
          |        The conf to resolve
          |    --defaultValue <key=value>, -d <key=value>
          |        Default values to be injected in the resolved config. The format must be key=value. Only unquoted strings are supported
          |    --pathToIgnore <string>, -i <string>
          |        Config paths that have to be ignored during comparison
          |    --verbose, -v
          |        Verbose mode
          |    --overwrite, -o
          |        Overwrite the template with the resolved conf""".stripMargin
      )
    )
  }

  val stringConfValueArgSimpleTest: SimpleSpec = test("stringConfValueArg simple") {
    assert(
      ScrupulousConfigArgs.stringConfValueArg.read("a=b"),
      equalTo(Valid("a" -> ConfigValueFactory.fromAnyRef("b")))
    )
  }

  val stringConfValueArgEmptyValueTest: SimpleSpec = test("stringConfValueArg empty value") {
    assert(
      ScrupulousConfigArgs.stringConfValueArg.read("a="),
      equalTo(Valid("a" -> ConfigValueFactory.fromAnyRef("")))
    )
  }

  val stringConfValueArgEmptyKeyTest: SimpleSpec = test("stringConfValueArg empty key") {
    assert(
      ScrupulousConfigArgs.stringConfValueArg.read("=b").isInvalid,
      isTrue
    )
  }

  val fixedArgsParseTest: SimpleSpec = test("parse fixed args") {
    val expected = Right(
      Arguments(
        templatePath = Paths.get("template.conf"),
        confPath = Paths.get("myConf"),
        defaultValues =
          List("default" -> ConfigValueFactory.fromAnyRef("3"), "other" -> ConfigValueFactory.fromAnyRef("444")),
        pathsToIgnore = List("pippo"),
        verbose = true,
        overwrite = true
      )
    )
    val input =
      List("-t", "template.conf", "-c", "myConf", "-d", "default=3", "-d", "other=444", "-o", "-v", "-i", "pippo")
    assert(
      ScrupulousConfigArgs.command.parse(input),
      equalTo(expected)
    )
  }

  val mainSuite: SimpleSpec = suite("args")(
    helpIsConsistent,
    stringConfValueArgSimpleTest,
    stringConfValueArgEmptyValueTest,
    stringConfValueArgEmptyKeyTest,
    fixedArgsParseTest
  )
}
