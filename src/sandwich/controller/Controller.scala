package sandwich.controller

import sandwich.client.peerhandler.PeerHandler
import sandwich.utils.{Settings, Utils}
import sandwich.client.filewatcher.DirectoryWatcher
import java.nio.file.Paths
import sandwich.client.filemanifesthandler.FileManifestHandler
import sandwich.server.Server
import scala.actors.Actor
import sandwich.controller.Controller.ShutdownRequest
import sandwich.client.peer.Peer
import sandwich.controller

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