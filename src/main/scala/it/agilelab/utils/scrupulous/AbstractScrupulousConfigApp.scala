package it.agilelab.utils.scrupulous

import java.io.{PrintStream, PrintWriter}
import java.nio.file.Path

import cats.data.EitherT
import cats.effect.{ExitCode, IO, IOApp, Resource}
import it.agilelab.utils.scrupulous.ScrupulousConfigArgs.Arguments
import it.agilelab.utils.scrupulous.ScrupulousConfigOutput.{CannotParse, HasDiffs, NoDiffs}

abstract class AbstractScrupulousConfigApp extends IOApp {
  protected val logic: ScrupulousConfigLogic

  protected val HAS_DIFFS_CODE: ExitCode = ExitCode(1)
  protected val CANNOT_PARSE_CODE: ExitCode = ExitCode(2)
  protected val FILE_NOT_FOUND_CODE: ExitCode = ExitCode(3)
  protected val WRONG_ARGS_CODE: ExitCode = ExitCode(4) // scalastyle:ignore

  protected val NO_DIFF_CODE: ExitCode = ExitCode.Success
  protected val out: PrintStream = System.out
  protected val err: PrintStream = System.err

  protected def parseArgs(
      args: List[String],
      err: PrintStream
  ): IO[Either[ExitCode, ScrupulousConfigArgs.Arguments]] = {
    IO {
      ScrupulousConfigArgs.command.parse(args).left.map { h =>
        err.println(h.toString())
        WRONG_ARGS_CODE
      }
    }
  }

  protected def handleLogicOutput(
      outcome: ScrupulousConfigOutput,
      parsedArgs: Arguments,
      out: PrintStream,
      err: PrintStream
  ): IO[ExitCode] = {
    val overwrite = parsedArgs.overwrite
    val templatePath = parsedArgs.templatePath
    outcome match {
      case CannotParse(errors) =>
        IO {
          err.println("Cannot parse one or more input files")
          errors.foreach {
            case (p, t) =>
              err.println(s"Cannot parse $p")
              t.printStackTrace(err)
          }
          CANNOT_PARSE_CODE
        }
      case HasDiffs(rendered, diffs) =>
        IO {
          err.println("Found differences between template and resolved config")
          err.println(diffs)
        }.flatMap { _ =>
            handleOverwrite(overwrite, templatePath, rendered)(out, err)
          }
          .map { _ =>
            HAS_DIFFS_CODE
          }

      case NoDiffs(rendered) =>
        handleOverwrite(overwrite, templatePath, rendered)(out, err).map { _ =>
          NO_DIFF_CODE
        }

    }
  }

  protected def fileExists(confPath: Path): IO[Either[ExitCode, Unit]] = IO {
    if (!confPath.toFile.exists()) {
      err.println(s"confPath: $confPath must exists")
      Left(FILE_NOT_FOUND_CODE)
    } else {
      Right(())
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      parsedArgs <- EitherT(parseArgs(args, err))
      _ <- EitherT.right(printArgs(err, parsedArgs))
      _ <- EitherT(fileExists(parsedArgs.confPath))
      outcome <- EitherT.right[ExitCode](callLogic(parsedArgs))
      exitCode <- EitherT.right[ExitCode](handleLogicOutput(outcome, parsedArgs, out, err))
    } yield {
      exitCode
    }
  }.merge

  protected def callLogic(args: Arguments): IO[ScrupulousConfigOutput] = IO {
    logic.apply(args.templatePath, args.confPath, args.defaultValues, args.pathsToIgnore, args.verbose)
  }

  protected def printArgs(
      printStream: PrintStream,
      parsedArgs: Arguments
  ): IO[Unit] = IO {
    val Arguments(templatePath, confPath, defaultValues, pathsToIgnore, verbose, overwrite) = parsedArgs
    printStream.println(s"""
                           |templatePath : $templatePath
                           |confPath     : $confPath
                           |defaultValues: $defaultValues
                           |pathsToIgnore: $pathsToIgnore
                           |verbose      : $verbose
                           |overwrite    : $overwrite
                           |""".stripMargin)
  }

  protected def handleOverwrite(overwrite: Boolean, path: Path, renderedConf: String)(
      out: PrintStream,
      err: PrintStream
  ): IO[Unit] = {
    if (overwrite) {
      Resource
        .fromAutoCloseable(IO(new PrintWriter(path.toFile)))
        .use(writer => IO(writer.println(renderedConf)))
    } else {
      IO.unit
    }
  }
}
