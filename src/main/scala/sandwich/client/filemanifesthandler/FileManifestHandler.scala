package sandwich.client.filemanifesthandler

import sandwich.client.peer.Peer
import sandwich.client.fileindex.FileIndex
import sandwich.client.clientcoms.getutilities._
import akka.actor._
import scala.collection.{immutable, mutable}
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/19/13
 * Time: 10:51 AM
 * To change this template use File | Settings | File Templates.
 */
class FileManifestHandler(private val peerHandler: ActorRef) extends Actor {
  private var fileManifest = new FileManifest(Map[Peer, FileIndex]())
  private val subscribers = mutable.Set[ActorRef]()
  private var manifestMap = Map[Peer, FileIndex]()

  override def preStart() {
    peerHandler ! self
  }

  override def receive = {
    case peerSet: Set[Peer] => {
      updateManifest(peerSet)
      subscribers.foreach(_ ! fileManifest)
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

  def transformPairToOption(peer: Peer, indexFuture: Future[Option[FileIndex]]): Option[(Peer, FileIndex)] = {
    Await.ready(indexFuture, Duration.Inf).value.flatMap(_.toOption).flatten.flatMap(index => Some((peer, index)))
  }

  def updateManifest(peerSet: Set[Peer]) {
    val (inMap, notInMap) = peerSet.partition(manifestMap.contains(_))
    val (needsUpdate, notNeedUpdate) = inMap.partition(peer => manifestMap(peer).IndexHash != peer.IndexHash)
    val newFileIndices = getFileIndices(notInMap ++ needsUpdate)
    manifestMap = manifestMap.filter{case (peer, _) => notNeedUpdate.contains(peer)} ++
      newFileIndices.flatMap {
        case (peer, indexFuture) => transformPairToOption(peer, indexFuture)
      }.toMap
    fileManifest = new FileManifest(manifestMap.toMap)
  }
}

object FileManifestHandler {
  def props(peerHandler: ActorRef) = Props(classOf[FileManifestHandler], peerHandler)
}