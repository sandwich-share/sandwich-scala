package sandwich.controller

import peerhandler.PeerHandler
import utils.{Settings, Utils}
import filewatcher.DirectoryWatcher
import java.nio.file.Paths
import filemanifesthandler.FileManifestHandler
import server.Server
import scala.actors.Actor
import controller.Controller.ShutdownRequest
import peer.Peer

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/19/13
 * Time: 6:57 PM
 * To change this template use File | Settings | File Templates.
 */
class Controller extends Actor {
  val settings = Settings.getSettings
  val peerHandler = new PeerHandler(Utils.getCachedPeerIndex)
  val directoryWatcher = new DirectoryWatcher(Paths.get(settings.sandwichPath))
  val fileManifestHandler = new FileManifestHandler(peerHandler)
  val server = new Server(peerHandler, directoryWatcher)

  override def act {
    peerHandler.start
    directoryWatcher.start
    fileManifestHandler.start
    server.startServer

    while(true) {
      receive {
        case _: PeerHandler.Request => peerHandler ! _
        case _: DirectoryWatcher.Request => directoryWatcher ! _
        case _: FileManifestHandler.Request => fileManifestHandler ! _
        case ShutdownRequest =>
      }
    }
  }

  private def shutdown {
    val peerSet = (peerHandler !? PeerHandler.PeerSetRequest).asInstanceOf[Set[Peer]]
    Utils.cachePeerIndex(peerSet)
    Settings.writeSettings(settings)
  }
}

object Controller {
  abstract class Request extends controller.Request
  case object ShutdownRequest extends Controller.Request
}

abstract class Request