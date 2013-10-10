package sandwich.frontend

import scala.swing._
import sandwich.utils.Settings

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 9/24/13
 * Time: 3:55 AM
 */
class SettingsPane(controller: FrontEndController) extends SandwichPane {
  val sandwichPathField = new TextField(controller.settings.sandwichPath)
  val saveButton = new Button("Save")
  sandwichPathField.maximumSize = new Dimension(200, 30)
  sandwichPathField.preferredSize = new Dimension(200, 30)

  sandwichPathField.action = new Action("SandwichPath") {
    override def apply() {
      if(controller.settings.sandwichPath != sandwichPathField.text) {
        saveButton.enabled = true
      }
    }
  }
  saveButton.action = new Action("SaveButton") {
    override def apply() {
      val newSettings = new Settings
      newSettings.sandwichPath = sandwichPathField.text
      controller.settings = newSettings
      saveButton.enabled = false
    }
  }
  add("Sandwich Path", sandwichPathField)
  contents += saveButton
  saveButton.enabled = false
}
