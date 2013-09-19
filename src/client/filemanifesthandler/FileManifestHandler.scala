package filemanifesthandler

import scala.actors.Actor
import peer.Peer
import fileindex.FileIndex
import scala.collection.mutable.Map
import clientcoms.getutilities._
import filemanifesthandler.FileManifestHandler.FileManifestRequest

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/19/13
 * Time: 10:51 AM
 * To change this template use File | Settings | File Templates.
 */
class FileManifestHandler extends Actor {
  private var fileManifest = new FileManifest(Map[Peer, FileIndex]())

  override def act {
    while(true) {
      receive {
        case newManifest: FileManifest => fileManifest = newManifest
        case FileManifestRequest => reply(fileManifest)
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
            if(manifestMap(peer).fileHash != peer.IndexHash) {
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
  object FileManifestRequest
}
