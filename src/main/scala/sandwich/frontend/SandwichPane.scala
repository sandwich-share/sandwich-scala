package sandwich.frontend

import scala.swing._

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 9/24/13
 * Time: 4:07 AM
 */
class SandwichPane extends BoxPanel(Orientation.Vertical) {
  contents += Swing.VStrut(20)

  def add(label: String, component: Component) {
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += new Label(label)
      contents += Swing.HGlue
      contents += component
      contents += Swing.HGlue
      maximumSize = new Dimension(400, 30)
      preferredSize = new Dimension(400, 30)
    }
  }
}
