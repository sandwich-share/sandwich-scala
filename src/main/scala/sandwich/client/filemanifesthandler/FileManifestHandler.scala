package sandwich.client.filemanifesthandler

import sandwich.client.peer.Peer
import sandwich.client.fileindex.FileIndex
import scala.collection.mutable.Map
import sandwich.client.clientcoms.getutilities._
import sandwich.client.filemanifesthandler.FileManifestHandler.FileManifestRequest
import sandwich.controller
import akka.actor._
import akka.agent.Agent
import scala.collection.{immutable, mutable}
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/19/13
 * Time: 10:51 AM
 * To change this template use File | Settings | File Templates.
 */
class FileManifestHandler extends Actor {
  import context._
  private var fileManifest = new FileManifest(immutable.Map[Peer, FileIndex]())
  private val isRunning = Agent[Boolean](true)
  private val mostRecentPeerSet = Agent[Set[Peer]](Set[Peer]())
  private val subscribers = mutable.Set[ActorRef]()
  context.actorSelection("/user/peerhandler") ! Identify()

  override def preStart {
    FileManifestHandlerCore.start
  }

  override def postStop {
    isRunning.send(false)
  }

  override def receive = {
    case newManifest: FileManifest => {
      fileManifest = newManifest
      subscribers.foreach(_ ! fileManifest)
    }
    case peerSet: Set[Peer] => mostRecentPeerSet.send(peerSet)
    case FileManifestRequest => sender ! fileManifest
    case ActorIdentity(_, ref) => for(actor <- ref) {
      context.watch(actor)
      subscribers += actor
    }
    case Terminated(actor) => {
      context.unwatch(actor)
      subscribers -= actor
    }
  }

  private object FileManifestHandlerCore extends Thread {
    val manifestMap = Map[Peer, FileIndex]()

    override def run {
      while(isRunning()) {
        for(peer <- mostRecentPeerSet()) {
          if(manifestMap.contains(peer)) {
            if(manifestMap(peer).IndexHash != peer.IndexHash) {
              getFileIndex(peer.IP) match {
                case Some(fileIndex) => manifestMap(peer) = fileIndex
                case None =>
              }
            }
          } else {
            getFileIndex(peer.IP) match {
              case Some(fileIndex) => manifestMap(peer) = fileIndex
              case None =>
            }
          }
        }
        self ! new FileManifest(manifestMap.toMap)
      }
    }
  }

}

object FileManifestHandler {
  abstract class Request extends controller.Request

  case object FileManifestRequest extends FileManifestHandler.Request

  case object SleepRequest extends FileManifestHandler.Request

  case object WakeRequest extends FileManifestHandler.Request

  def props = Props[FileManifestHandler]
}
