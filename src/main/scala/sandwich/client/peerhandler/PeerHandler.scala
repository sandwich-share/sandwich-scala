package sandwich.client.peerhandler

import sandwich.client.clientcoms.getutilities._
import sandwich.client.peer.Peer
import sandwich.controller
import scala.collection.mutable
import java.net.InetAddress
import java.util.{Calendar, Date}
import sandwich.client.peerhandler.PeerHandler._
import akka.actor._
import akka.agent.Agent
import sandwich.utils._
import sandwich.utils.SandwichInitializationException
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Terminated
import sandwich.utils.logging.Logging

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/17/13
 * Time: 12:34 AM
 * To change this template use File | Settings | File Templates.
 */
class PeerHandler extends Actor with Logging {
  import context._
  private val subscribers = mutable.Set[ActorRef]()
  private val peerSetAgent = Agent[Set[Peer]](Set[Peer]())
  private val unvisitedPeers = Agent[mutable.Set[InetAddress]](mutable.Set[InetAddress]())

  override def preStart() {
    val peerIndexFuture = Utils.getCachedPeerIndex()
    peerIndexFuture onSuccess {
      case startingPeers: Set[Peer] => peerSetAgent.send(startingPeers)
    }
    peerIndexFuture onFailure {
      case error =>
        log.error("failed to load cached peer index")
        throw error
    }
    PeerHandlerCore.populatePeerMap(peerSetAgent())
    new Thread(PeerHandlerCore).start()
  }

  override def postStop() {
    log.info("Shutting down")
    PeerHandlerCore.shutdown()
    PingHandler.shutdown()
    if(!peerSetAgent().isEmpty) {
      Utils.cachePeerIndex(peerSetAgent())
    }
  }

  override def receive = {
    case unvisitedIp: InetAddress => unvisitedPeers.send(_ += unvisitedIp)

    case newPeerSet: Set[Peer] => {
      peerSetAgent.send(newPeerSet)
      subscribers.foreach(_ ! newPeerSet)
    }

    case EmptyPeerSetNotification => {
      new Thread(PingHandler).start()
    }

    case PingRespondedNotification => {
      PeerHandlerCore.populatePeerMap(peerSetAgent())
      new Thread(PeerHandlerCore).start()
    }

    case ref: ActorRef => {
      context.watch(ref)
      subscribers += ref
      log.info("Now watching: " + ref)
    }

    case Terminated(ref) => {
      context.unwatch(ref)
      subscribers -= ref
      log.info("No longer watching: " + ref)
    }
  }

  private object PeerHandlerCore extends Thread {
    private val running = Agent[Boolean](true)
    private var peerMap = mutable.Map[InetAddress, Peer]()
    private var deadPeers = mutable.Map[InetAddress, Date]()

    def shutdown() {
      running.send(false) // Ends thread execution at next iteration.
    }

    def populatePeerMap(peerSet: Set[Peer]) {
      peerMap ++= peerSet.map(peer => (peer.IP, peer)).toMap[InetAddress, Peer]
    }

    override def run() {
      log.info("Starting PeerHandlerCore")
      while(running()) {
        if(!update()) {
          return
        }
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, -1)
        val currentTime = calendar.getTime()
        deadPeers = deadPeers.filter{ case (_, date) => currentTime.after(date) }
        Thread.sleep(1000)
      }
      running.send(true) // Reset for next time.
    }

    private def update(): Boolean = {
      while(true) {
        if(peerMap.isEmpty) {
          log.info("peerMap is empty, killing PeerHandlerCore")
          self ! EmptyPeerSetNotification
          return false
        } else {
          val peer = selectUpdatePeer()
          getPeerList(peer) match {
            case Some(peerList) => {
              update(peerList)
              return true
            }
            case None => {
              peerMap -= peer
              deadPeers(peer) = new Date
            }
          }
        }
      }
      return false // Never actually gets here.
    }

    private def selectUpdatePeer(): InetAddress = {
      if(!unvisitedPeers().isEmpty) {
        val unvisitedPeer = unvisitedPeers().head
        unvisitedPeers().remove(unvisitedPeer)
        return unvisitedPeer
      } else if(peerMap.size > 1) {
        return peerMap.values.reduce((left, right) => if(left.LastSeen.before(right.LastSeen)) left else right).IP
      } else {
        return peerMap.head._1
      }
    }

    private def update(newPeerSet: Set[Peer]) {
      val (inPeerMap, notInPeerMap) = newPeerSet.partition(peer => peerMap.contains(peer.IP))
      val (inDeadPeers, notInDeadPeers) = notInPeerMap.partition(peer => deadPeers.contains(peer.IP))
      val notDead = inDeadPeers.filter(peer => peer.LastSeen.after(deadPeers(peer.IP)))

      deadPeers --= notDead.map(peer => peer.IP)
      peerMap ++= toPeerMap(notDead)
      peerMap ++= toPeerMap(notInDeadPeers)
      peerMap ++= toPeerMap(inPeerMap.filter(peer => peer.LastSeen.after(peerMap(peer.IP).LastSeen)))

      self ! peerMap.values.toSet[Peer]
    }

    private def toPeerMap(peerSet: Set[Peer]): Map[InetAddress, Peer] = peerSet.map(peer => (peer.IP, peer)).toMap[InetAddress, Peer]
  }

  private object PingHandler extends Runnable {
    private val running = Agent[Boolean](true)

    def shutdown() {
      running.send(false)
    }

    override def run() {
      log.info("Starting PingHandler")
      while (running()) {
        for (peer <- peerSetAgent()) {
          if(ping(peer.IP)) {
            log.info("Found active peer, killing PingHandler")
            self ! PingRespondedNotification
            return
          }
          Thread.sleep(3000)
        }
      }
      running.send(true)
    }
  }

  case object EmptyPeerSetNotification
  case object PingRespondedNotification
}

object PeerHandler {
  abstract class Request extends controller.Request
  case object PeerSetRequest extends PeerHandler.Request
  def props = Props[PeerHandler]
}