package filemanifesthandler

import scala.actors.Actor
import peer.Peer
import fileindex.FileIndex
import scala.collection.mutable.Map
import clientcoms.getutilities._
import filemanifesthandler.FileManifestHandler.{WakeRequest, SleepRequest, FileManifestRequest}
import controller.Request
import peerhandler.PeerHandler

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/19/13
 * Time: 10:51 AM
 * To change this template use File | Settings | File Templates.
 */
class FileManifestHandler(private val peerHandler: PeerHandler) extends Actor {
  private val core = new FileManifestHandlerCore
  private var fileManifest = new FileManifest(Map[Peer, FileIndex]())

  override def act {
    peerHandler ! PeerHandler.SubscriptionRequest(core)
    while(true) {
      receive {
        case newManifest: FileManifest => fileManifest = newManifest
        case FileManifestRequest => reply(fileManifest)
        case SleepRequest => peerHandler ! PeerHandler.UnSubscriptionRequest(core)
        case WakeRequest => peerHandler ! PeerHandler.SubscriptionRequest(core)
        case _ =>
      }
    }
  }

  private class FileManifestHandlerCore extends Actor {
    val manifestMap = Map[Peer, FileIndex]()

    private def getMostRecentPeerSet: Set[Peer] = {
      while(mailboxSize > 1) {
        receive {
          case _ =>
        }
      }
      receive {
        case peerSet: Set[Peer] => peerSet
      }
    }

    override def act {
      while(true) {
        val peerSet = getMostRecentPeerSet
        for(peer <- peerSet) {
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
        FileManifestHandler.this ! new FileManifest(manifestMap)
      }
    }
  }

}

object FileManifestHandler {
  abstract class Request extends controller.Request
  case object FileManifestRequest extends FileManifestHandler.Request
  case object SleepRequest extends FileManifestHandler.Request
  case object WakeRequest extends FileManifestHandler.Request
}
