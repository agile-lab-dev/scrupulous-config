package it.agilelab.utils.scrupulous

import java.nio.file.Path

import com.typesafe.config.ConfigValue

trait ScrupulousConfigLogic {
  def apply(
      templatePath: Path,
      confPath: Path,
      defaultValues: List[(String, ConfigValue)],
      pathsToIgnore: List[String],
      verbose: Boolean
  ): ScrupulousConfigOutput
}
