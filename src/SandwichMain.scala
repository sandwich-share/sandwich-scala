import filewatcher.DirectoryWatcher
import java.nio.file.{Paths, Files}
import peerhandler.PeerHandler
import scala.actors.Actor
import utils.{Settings, Utils}
import server.Server

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 9:43 PM
 * To change this template use File | Settings | File Templates.
 */
object SandwichMain {
  def main(args: Array[String]) {
    val settings = Settings.getSettings
    val peerSet = Utils.getCachedPeerIndex
    val peerHandler = new PeerHandler(peerSet)
    val fileWatcher = new DirectoryWatcher(Paths.get(settings.sandwichPath))
    val server = new Server(peerHandler, fileWatcher)
    fileWatcher.start
    peerHandler.start
    server.startServer
    // Here we want to block and hand over control to other actors.
    // TODO: Block more intelligently.
    Actor.receive({case _ => System.exit(0)})
  }
}
