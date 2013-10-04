package sandwich.frontend

import scala.swing._

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 9/24/13
 * Time: 3:55 AM
 */
class SettingsPane extends SandwichPane {
  val sandwichPathField = new TextField
  sandwichPathField.maximumSize = new Dimension(200, 30)
  sandwichPathField.preferredSize = new Dimension(200, 30)
  add("Sandwich Path", new TextField)
}
