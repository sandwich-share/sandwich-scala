package sandwich.frontend

import scala.swing._
import java.awt.Dimension
import scala.swing.event.EditDone
import sandwich.client.filemanifesthandler.FileManifest
import sandwich.client.fileindex.FileItem
import scala.swing.event.EditDone
import scala.Array

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 9/24/13
 * Time: 4:09 AM
 */
class SearchPane extends BoxPanel(Orientation.Vertical) {
  private val searchBox = new TextField
  searchBox.minimumSize = new Dimension(200, 30)
  searchBox.preferredSize = new Dimension(200, 30)
  searchBox.maximumSize = new Dimension(200, 30)
  private val scrollPane = new ScrollPane

  contents += Swing.VStrut(20)
  contents += new BoxPanel(Orientation.Horizontal) {
    contents += Swing.HGlue
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += new Button("Search: ")
      contents += searchBox
      minimumSize = new Dimension(300, 30)
    }
    contents += Swing.HGlue
  }
  contents += Swing.VStrut(20)
  contents += scrollPane
}
