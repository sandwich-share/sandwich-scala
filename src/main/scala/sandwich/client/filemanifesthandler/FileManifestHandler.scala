package sandwich.client.filemanifesthandler

import sandwich.client.peer.Peer
import sandwich.client.fileindex.FileIndex
import scala.collection.mutable.Map
import sandwich.client.clientcoms.getutilities._
import akka.actor._
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
class FileManifestHandler(private val peerHandler: ActorRef) extends Actor {
  private var fileManifest = new FileManifest(immutable.Map[Peer, FileIndex]())
  private var mostRecentPeerSet = Set[Peer]()
  private val subscribers = mutable.Set[ActorRef]()
  private val manifestMap = Map[Peer, FileIndex]()

  override def preStart() {
    peerHandler ! self
  }

  override def receive = {
    case peerSet: Set[Peer] => {
      mostRecentPeerSet = peerSet
      updateManifest()
      subscribers.foreach(_ ! fileManifest)
      println("Received peerset")
    }
    case actor: ActorRef => {
      context.watch(actor)
      subscribers += actor
      println(actor)
    }
    case Terminated(actor) => {
      context.unwatch(actor)
      subscribers -= actor
    }
  }

  def updateManifest() {
    for(peer <- mostRecentPeerSet) {
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
    fileManifest = new FileManifest(manifestMap.toMap)
  }
}

object FileManifestHandler {
  def props(peerHandler: ActorRef) = Props(classOf[FileManifestHandler], peerHandler)
}