package sandwich.frontend

import scala.swing._

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/24/13
 * Time: 4:07 AM
 * To change this template use File | Settings | File Templates.
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
      maximumSize = new Dimension(300, 30)
      preferredSize = new Dimension(300, 30)
    }
  }
}
