package sandwich.utils.actors

import akka.actor.{Actor, ActorRef}
import sandwich.client.peer.Peer
import sandwich.client.fileindex.FileIndex

/**
 * Declares that this actor is a subscriber to a special publisher (see Publisher[T]) providing the subscribe function
 * to subscribe to a given Publisher
 */
trait Subscribing extends Actor {

  /**
   * Subscribes to the specified publisher.
   * @param publisher to subscribe to.
   */
  final def subscribe(publisher: ActorRef): Unit = publisher ! self
}

trait SubscribingPeerHandler extends Subscribing {
  abstract override def receive = super.receive orElse {
    case subscriptionValue: Set[Peer] => respond(subscriptionValue)
  }

  def respond(subscriptionValue: Set[Peer]): Unit
}

trait SubscribingFileManifestHandler extends Subscribing {
  abstract override def receive = super.receive.orElse {
    case subscriptionValue: FileIndex => respond(subscriptionValue)
  }

  def respond(subscriptionValue: FileIndex): Unit
}