package it.agilelab.utils.scrupulous

import zio.test.{DefaultRunnableSpec, suite}

object AllSuites
    extends DefaultRunnableSpec(
      suite("All tests")(ScrupulousConfigArgsSuite.mainSuite, ScrupulousConfigLogicImplSuite.mainSuite)
    )
