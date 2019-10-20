package it.agilelab.utils.scrupulous

object ScrupulousConfig extends AbstractScrupulousConfigApp {
  override protected val logic: ScrupulousConfigLogic = new ScrupulousConfigLogicImpl()
}
