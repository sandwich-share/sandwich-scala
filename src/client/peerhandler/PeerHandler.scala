package peerhandler

import scala.actors.{TIMEOUT, Actor}
import scala.collection.immutable.HashSet
import peer.Peer
import clientcoms.getutilities._
import scala.collection.mutable
import java.net.InetAddress
import scala.concurrent.Lock

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/17/13
 * Time: 12:34 AM
 * To change this template use File | Settings | File Templates.
 */
class PeerHandler(private var _peerSet: Set[Peer]) extends Actor {
  private val peerSetLock = new Lock

  private def update(newPeerSet: Set[Peer]) {
    var peerMap: mutable.Map[InetAddress, Peer]
    for(peer <- _peerSet) {
      peerMap(peer.ipAddress) = peer
    }
    for(peer <- newPeerSet) {
      val ipAddress = peer.ipAddress
      if(peerMap.contains(ipAddress)) {
        if(peer.lastSeen.after(peerMap(ipAddress).lastSeen)) {
          peerMap(ipAddress) = peer
        }
      } else {
        peerMap(ipAddress) = peer
      }
    }
    peerSetLock.acquire
    _peerSet = (for {(_, peer) <- peerMap} yield peer).toSet
    peerSetLock.release
  }

  def peerSet = {
    peerSetLock.acquire
    val peerSet = _peerSet
    peerSetLock.release
    peerSet
  }

  override def act {
    while(true) {
      val oldPeer = _peerSet.fold(_peerSet.head)((left, right) => if(left.lastSeen.before(right.lastSeen)) {left} else {right})
      update(getPeerList(oldPeer.ipAddress))
      reactWithin(1000) { case TIMEOUT => }
    }
  }
}
