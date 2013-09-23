package sandwich

import scala.swing.{Frame, SimpleSwingApplication}
import sandwich.controller.Controller
import sandwich.frontend.SandwichMainFrame

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/20/13
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */
object SandwichSwingApplication extends SimpleSwingApplication {
  private val controller = new Controller
  controller.start

  def top: Frame = new SandwichMainFrame(controller)
}
