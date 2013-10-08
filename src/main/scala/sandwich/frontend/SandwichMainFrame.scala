package sandwich.frontend

import scala.swing._
import sandwich.controller.Controller
import java.awt.GraphicsConfiguration
import sandwich.client.filemanifesthandler.{FileManifestHandler, FileManifest}
import sandwich.client.fileindex.FileItem
import scala.swing.event.EditDone
import scala.swing.TabbedPane.Page

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/20/13
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
class SandwichMainFrame(private val controller: FrontEndController, gc: GraphicsConfiguration = null) extends MainFrame(gc) {
  private val tabs = new TabbedPane

  title = "Sandwich"
  contents = tabs
  maximize

  tabs.pages += new Page("Search", new SearchPane(controller))
  tabs.pages += new Page("Settings", new SettingsPane)

  override def closeOperation() {
    super.closeOperation()
    controller.shutdown()
  }

}
