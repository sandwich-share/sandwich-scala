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
class SearchPane(private val controller: FrontEndController) extends BoxPanel(Orientation.Vertical) {
  private val searchBox = new TextField
  searchBox.minimumSize = new Dimension(200, 30)
  searchBox.preferredSize = new Dimension(200, 30)
  searchBox.maximumSize = new Dimension(200, 30)
  private val scrollPane = new ScrollPane
  private val searchButton = new Button("Search: ")
  searchButton.action = Search

  contents += Swing.VStrut(20)
  contents += new BoxPanel(Orientation.Horizontal) {
    contents += Swing.HGlue
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += searchButton
      contents += searchBox
      minimumSize = new Dimension(300, 30)
    }
    contents += Swing.HGlue
  }
  contents += Swing.VStrut(20)
  contents += scrollPane

  private object Search extends Action("Search") {
    override def apply() {
      val results = controller.search(searchBox.text)
      scrollPane.contents = new Table(results.map(_.toArray()).toArray, Seq("File Path", "Size"))
    }
  }
}
