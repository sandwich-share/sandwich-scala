package sandwich.controller

import sandwich.client.peerhandler.PeerHandler
import sandwich.utils.{Settings, Utils}
import sandwich.client.filewatcher.DirectoryWatcher
import java.nio.file.Paths
import sandwich.client.filemanifesthandler.FileManifestHandler
import sandwich.server.Server
import scala.actors.{Scheduler, Actor}
import sandwich.controller.Controller.ShutdownRequest
import sandwich.client.peer.Peer
import sandwich.controller
import scala.actors.scheduler.ResizableThreadPoolScheduler

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

  Scheduler.impl = {
    val scheduler = new ResizableThreadPoolScheduler(false)
    scheduler.start
    scheduler
  }

  override def act {
    peerHandler.start
    directoryWatcher.start
    fileManifestHandler.start
    server.startServer

    while(true) {
      receive {
        case request: PeerHandler.Request => if(request.expectsResponse) { reply(peerHandler !? request) } else { peerHandler ! request }
        case request: DirectoryWatcher.Request => if(request.expectsResponse) { reply(peerHandler !? request) } else { directoryWatcher ! request }
        case request: FileManifestHandler.Request => if(request.expectsResponse) { reply(peerHandler !? request) } else {fileManifestHandler ! request }
        case ShutdownRequest => shutdown
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

abstract class Request {
  def expectsResponse:Boolean = false
}