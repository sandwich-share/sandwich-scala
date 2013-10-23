package sandwich.utils.actors

import akka.actor.{ActorSelection, Actor, ActorRef}
import sandwich.client.peer.Peer
import sandwich.client.fileindex.FileIndex
import sandwich.client.filemanifesthandler.{FileManifestHandler, FileManifest}
import sandwich.utils._
import sandwich.controller.Controller
import sandwich.client.peerhandler.PeerHandler
import sandwich.client.filewatcher.DirectoryWatcher
import java.net.InetAddress
import sandwich.server.Server

/**
 * Declares that this actor is a subscriber to a special publisher (see Publisher[T]) providing the subscribe function
 * to subscribe to a given Publisher
 */
trait Subscribing extends Actor {
  import context._

  /**
   * Subscribes to the Publisher with the specified name.
   * @param actorName name of the Publisher to subscribe to.
   */
  final def subscribe(actorName: String) {
    println(actorPath(Controller.name, actorName))
    context.actorSelection(actorPath(Controller.name, actorName)) ! self
  }
}

trait SubscribingPeerHandler extends Subscribing {
  abstract override def receive = super.receive orElse {
    case subscriptionValue: Set[Peer] => respond(subscriptionValue)
  }

  abstract override def preStart() {
    super.preStart()
    subscribe(PeerHandler.name)
  }

  def respond(subscriptionValue: Set[Peer]): Unit
}

trait SubscribingDirectoryWatcher extends Subscribing {
  abstract override def receive = super.receive orElse {
    case subscriptionValue: FileIndex => respond(subscriptionValue)
  }

  abstract override def preStart() {
    super.preStart()
    subscribe(DirectoryWatcher.name)
  }

  def respond(subscriptionValue: FileIndex): Unit
}

trait SubscribingFileManifestHandler extends Subscribing {
  abstract override def receive = super.receive orElse {
    case subscriptionValue: FileManifest => respond(subscriptionValue)
  }

  abstract override def preStart() {
    super.preStart()
    subscribe(FileManifestHandler.name)
  }

  def respond(subscriptionValue: FileManifest): Unit
}

trait SubscribingServer extends Subscribing {
  abstract override def receive = super.receive orElse {
    case subscriptionValue: InetAddress => respond(subscriptionValue)
  }

  abstract override def preStart() {
    super.preStart()
    subscribe(Server.name)
  }

  def respond(subscriptionValue: InetAddress): Unit
}