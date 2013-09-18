import filewatcher.DirectoryWatcher
import java.nio.file.{Paths, Files}
import peerhandler.PeerHandler
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
    fileWatcher.act
    peerHandler.act
    server.startServer
  }
}
