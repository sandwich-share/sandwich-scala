package sandwich.frontend

import sandwich.client.clientcoms.getutilities.getFile
import sandwich.utils._
import akka.agent.Agent
import akka.actor.ActorDSL._
import akka.actor._
import sandwich.client.filemanifesthandler.{FileManifestHandler, FileManifest}
import sandwich.client.fileindex.{FileItem, FileIndex}
import sandwich.client.peer.Peer
import scala.concurrent.ExecutionContext.Implicits.global
import sandwich.controller.Controller
import akka.actor.Identify

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 10/4/13
 * Time: 3:19 PM
 */

// We wrap all communication to anything related to the server to make the transition to a separate process easier.
class FrontEndController {
  val system = ActorSystem("SandwichSystem")
  val controller = system.actorOf(Controller.props, Controller.name)
  val fileManifestAgent = Agent[FileManifest](new FileManifest(Map[Peer, FileIndex]()))
  system.actorOf(Props(classOf[FrontEndActor], fileManifestAgent))

  def search(searchTerm: String): Set[FileItem] = {
    return fileManifestAgent().search(FileManifest.simpleSearch(searchTerm))
  }

  def download(fileItem: FileItem) {
    getFile(fileManifestAgent().filePeerMap(fileItem).IP, fileItem.FileName)
  }

  def settings: Settings = {
    return Settings.getSettings
  }

  def settings_=(settings: Settings) {
    Settings.writeSettings(settings)
  }

  def shutdown() {
    system.shutdown()
  }
}

private class FrontEndActor(private val fileManifestAgent: Agent[FileManifest]) extends Actor {
  override def preStart() {
    context.actorSelection(actorPath(Controller.name, FileManifestHandler.name)) ! self
  }

  override def receive = {
    case fileManifest: FileManifest => {
      fileManifestAgent.send(fileManifest)
    }
  }
}