package sandwich.frontend

import scala.swing._
import sandwich.controller.Controller
import java.awt.GraphicsConfiguration
import sandwich.client.filemanifesthandler.{FileManifestHandler, FileManifest}
import sandwich.client.fileindex.FileItem
import scala.swing.event.EditDone

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/20/13
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
class SandwichMainFrame(private val controller: Controller, gc: GraphicsConfiguration = null) extends MainFrame(gc) {

  lazy private val fileManifest = (controller !? FileManifestHandler.FileManifestRequest).asInstanceOf[FileManifest]
  private val sandwichSearchPane = new SandwichSearchPane

  title = "Sandwich"
  contents = sandwichSearchPane
  maximize

  override def closeOperation {
    super.closeOperation
    controller ! Controller.ShutdownRequest
  }

  private class SandwichSearchPane extends BoxPanel(Orientation.Vertical) {
    private val searchBox = new TextField
    private val scrollPane = new ScrollPane

    contents += new BoxPanel(Orientation.Horizontal) {
      contents += new Label("Search: ")
      contents += searchBox
    }
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += new Label("Results:")
      contents += scrollPane
    }

    searchBox.subscribe({
      case event: EditDone => {
        update(fileManifest.search(FileManifest.simpleSearch(searchBox.text)))
      }
      case _ =>
    })

    def update(fileItems: Set[FileItem]) {
      val columnNames = Array("File Name", "File Size")
      scrollPane.contents = new Table(fileItems.map(fileItem =>
        Array(fileItem.FileName.asInstanceOf[Any], fileItem.Size.toString.asInstanceOf[Any])).toArray[Array[Any]],
        columnNames.toSeq)
      scrollPane.repaint
    }
  }

}
