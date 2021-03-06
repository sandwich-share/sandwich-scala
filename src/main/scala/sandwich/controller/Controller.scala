package sandwich.controller

import sandwich.client.peerhandler.PeerHandler
import sandwich.client.filewatcher.DirectoryWatcher
import java.nio.file.Paths
import sandwich.client.filemanifesthandler.FileManifestHandler
import sandwich.server.Server
import sandwich.client.peer.Peer
import sandwich.controller
import akka.actor.{Props, Actor}
import akka.pattern.ask
import sandwich.utils._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/19/13
 * Time: 6:57 PM
 * To change this template use File | Settings | File Templates.
 */
class Controller extends Actor {
  private val settings = Settings.getSettings
  val peerHandler = context.actorOf(PeerHandler.props, "peerhandler")
  val directoryWatcher = context.actorOf(DirectoryWatcher.props(Paths.get(settings.sandwichPath)), "directorywatcher")
  val fileManifestHandler = context.actorOf(FileManifestHandler.props(peerHandler), "filemanifesthandler")
  val server = context.actorOf(Server.props(peerHandler, directoryWatcher), "server")

  override def postStop {
    for(peerSet <- peerHandler ? PeerHandler.PeerSetRequest) {
      Utils.cachePeerIndex(peerSet.asInstanceOf[Set[Peer]])
    }
    Settings.writeSettings(settings)
  }

  override def receive = {
    case request: PeerHandler.Request => peerHandler forward request
    case request: DirectoryWatcher.Request => directoryWatcher forward request
  }
}

object Controller {
  abstract class Request extends controller.Request
  case object ShutdownRequest extends Controller.Request

  def props: Props = Props[Controller]
}

abstract class Request