package sandwich.frontend

import scala.swing._

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/24/13
 * Time: 3:55 AM
 * To change this template use File | Settings | File Templates.
 */
class SettingsPane extends SandwichPane {
  val sandwichPathField = new TextField
  sandwichPathField.maximumSize = new Dimension(200, 30)
  sandwichPathField.preferredSize = new Dimension(200, 30)
  add("Sandwich Path", new TextField)
}
