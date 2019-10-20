package it.agilelab.utils.scrupulous

import java.nio.file.Paths

import com.typesafe.config.ConfigValueFactory
import zio.test._
import zio.test.Assertion._

object ScrupulousConfigLogicImplSuite {
  type SimpleSpec = Spec[Any, Nothing, String, Either[TestFailure[Nothing], TestSuccess[Unit]]]

  val logic = new ScrupulousConfigLogicImpl()

  val jvmAndIJprops = List("awt", "file", "gopherProxySet", "java", "line", "os", "path", "sun", "user")

  val aEqualsB: SimpleSpec =
    test("a equals b")(
      assert(
        logic(
          templatePath = Paths.get("test-data/test1/template.conf"),
          confPath = Paths.get("test-data/test1/actual.conf"),
          defaultValues = Nil,
          pathsToIgnore = jvmAndIJprops,
          verbose = false
        ) match {
          case ScrupulousConfigOutput.NoDiffs(_) => true
          case _                                 => false
        },
        isTrue
      )
    )

  val includedConf: SimpleSpec =
    test("included conf")(
      assert(
        logic(
          templatePath = Paths.get("test-data/test2/template.conf"),
          confPath = Paths.get("test-data/test2/actual.conf"),
          defaultValues = Nil,
          pathsToIgnore = jvmAndIJprops,
          verbose = false
        ) match {
          case ScrupulousConfigOutput.NoDiffs(_) => true
          case _                                 => false
        },
        isTrue
      )
    )

  val defautValues: SimpleSpec =
    test("defautValues used in conf")(
      assert(
        logic(
          templatePath = Paths.get("test-data/test3/template.conf"),
          confPath = Paths.get("test-data/test3/actual.conf"),
          defaultValues = List("pippo" -> ConfigValueFactory.fromAnyRef("pappo")),
          pathsToIgnore = jvmAndIJprops,
          verbose = false
        ) match {
          case ScrupulousConfigOutput.NoDiffs(_) => true
          case _                                 => false
        },
        isTrue
      )
    )

  val mainSuite = suite("scrupulousConfigLogic")(aEqualsB, includedConf, defautValues)
}
