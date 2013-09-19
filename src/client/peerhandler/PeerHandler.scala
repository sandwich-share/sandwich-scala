package peerhandler

import scala.actors.{TIMEOUT, Actor}
import scala.actors.Actor._
import peer.Peer
import clientcoms.getutilities._
import scala.collection.mutable
import java.net.InetAddress
import scala.concurrent.Lock
import java.util.{TimeZone, Calendar, Date}
import peerhandler.PeerHandler.{SubscriptionRequest, PeerSetRequest}

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/17/13
 * Time: 12:34 AM
 * To change this template use File | Settings | File Templates.
 */
class PeerHandler(private var _peerSet: Set[Peer]) extends Actor {
  private val _core = new PeerHandlerCore
  private val subscribers = mutable.Set[Actor]()

  override def act {
    _core.start

    while(true) {
      receive {
        case PeerSetRequest => reply(_peerSet)
        case newPeerSet: Set[Peer] => {
          _peerSet = newPeerSet
          for(subscriber <- subscribers) {
            subscriber ! _peerSet
          }
        }
        case SubscriptionRequest(subscriber) => subscribers.add(subscriber)
        case _ =>
      }
    }
  }

  def core: Actor = _core

  private class PeerHandlerCore extends Actor {
    private val unvisitedPeers = mutable.Set[InetAddress]()
    private val peerMap = mutable.Map[InetAddress, Peer]()
    private val deadPeers = mutable.Map[InetAddress, Date]()
    for(peer <- _peerSet) { peerMap(peer.IP) = peer }

    private def update(newPeerSet: Set[Peer]) {
      for(peer <- newPeerSet) {
        val ipAddress = peer.IP
        if(peerMap.contains(ipAddress)) {
          if(peer.LastSeen.after(peerMap(ipAddress).LastSeen)) {
            peerMap(ipAddress) = peer
          }
        } else if(deadPeers.contains(ipAddress)) {
          if(deadPeers(ipAddress).before(peer.LastSeen)) {
            peerMap(ipAddress) = peer
          }
        } else {
          peerMap(ipAddress) = peer
        }
      }
      PeerHandler.this ! (for {(_, peer) <- peerMap} yield peer).toSet
    }

    private def selectUpdatePeer: InetAddress = {
      if(!unvisitedPeers.isEmpty) {
        val unvisitedPeer = unvisitedPeers.head
        unvisitedPeers.remove(unvisitedPeer)
        return unvisitedPeer
      } else {
        peerMap.values.fold(_peerSet.head)((left, right) => if(left.LastSeen.before(right.LastSeen)) {left} else {right}).IP
      }
    }

    override def act {
      while(true) {
        while(mailboxSize > 0) {
          receive {
            case address: InetAddress => {
              if(!peerMap.contains(address) && !deadPeers.contains(address)) {
                unvisitedPeers.add(address)
              }
            }
            case _ =>
          }
        }
        val oldPeer = selectUpdatePeer
        getPeerList(oldPeer) match {
          case Some(peerList) => update(peerList)
          case None => {
            peerMap.remove(oldPeer)
            deadPeers(oldPeer) = new Date
          }
        }
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, -1)
        val currentTime = calendar.getTime()
        deadPeers.filter{ case (_, date) => currentTime.after(date) }
        reactWithin(1000) { case TIMEOUT => }
      }
    }
  }
}

object PeerHandler {
  case object PeerSetRequest
  case class SubscriptionRequest(val actor: Actor)
}
