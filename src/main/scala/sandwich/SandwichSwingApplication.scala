package sandwich

import scala.swing.{Frame, SimpleSwingApplication}
import sandwich.controller.Controller
import sandwich.frontend.{FrontEndController, SandwichMainFrame}
import javax.swing.UIManager
import org.apache.log4j.BasicConfigurator

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/20/13
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */
object SandwichSwingApplication extends SimpleSwingApplication {
  val controller = new FrontEndController
  BasicConfigurator.configure()

  try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
  } catch {
    case _: Throwable =>
  }

  def top: Frame = new SandwichMainFrame(controller)
}
