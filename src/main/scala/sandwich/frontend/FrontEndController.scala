package sandwich.frontend

import sandwich.client.clientcoms.getutilities.getFile
import sandwich.utils.{Settings, toPath}
import akka.agent.Agent
import akka.actor.ActorDSL._
import akka.actor.{Props, Actor, ActorSystem}
import sandwich.client.filemanifesthandler.FileManifest
import sandwich.client.fileindex.{FileItem, FileIndex}
import sandwich.client.peer.Peer
import scala.concurrent.ExecutionContext.Implicits.global
import sandwich.controller.Controller

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 10/4/13
 * Time: 3:19 PM
 */

// We wrap all communication to anything related to the server to make the transition to a separate process easier.
class FrontEndController {
  val system = ActorSystem("SandwichSystem")
  val controller = system.actorOf(Controller.props)
  val fileManifestAgent = Agent[FileManifest](new FileManifest(Map[Peer, FileIndex]()))
  system.actorOf(Props[FrontEndActor])

  def search(searchTerm: String): Set[FileItem] = {
    return fileManifestAgent().search(FileManifest.simpleSearch(searchTerm))
  }

  def download(fileItem: FileItem) {
    getFile(fileManifestAgent().filePeerMap(fileItem).IP, fileItem.FileName)
  }

  def getSettings(): Settings = {
    return Settings.getSettings
  }

  def setSettings(settings: Settings) {
    Settings.writeSettings(settings)
  }

  class FrontEndActor extends Actor {
    context.actorSelection("/user/peerhandler")

    override def receive = {
      case fileManifest: FileManifest => fileManifestAgent.send(fileManifest)
    }
  }
}
