package it.agilelab.utils.scrupulous

import java.nio.file.Path

import gnieh.diffson.sprayJson._

sealed trait ScrupulousConfigOutput

object ScrupulousConfigOutput {

  case class CannotParse(errors: List[(Path, Throwable)]) extends ScrupulousConfigOutput

  case class HasDiffs(rendered: String, diffs: JsonPatch) extends ScrupulousConfigOutput

  case class NoDiffs(rendered: String) extends ScrupulousConfigOutput

}
