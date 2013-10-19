package sandwich.server

import java.net.{URI, InetSocketAddress}
import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import java.io._
import java.nio.file.{Paths, Path}
import sandwich.client.peer.Peer
import java.util.Date
import sandwich.client.fileindex.{FileItem, FileIndex}
import scala.io.Source
import sandwich.utils._
import akka.actor._
import akka.agent.Agent
import sandwich.utils.using

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 4:30 PM
 * To change this template use File | Settings | File Templates.
   */
class Server(private val peerHandler: ActorRef, private val directoryWatcher: ActorRef) extends Actor {
  import context._
  private val server = HttpServer.create(new InetSocketAddress(Utils.portHash(Utils.localIp)), 100)
  private val peerSet = Agent[Set[Peer]](Set[Peer]())
  private val fileIndex = Agent[FileIndex](FileIndex(Set[FileItem]()))

  override def preStart() {
    peerHandler ! self
    directoryWatcher ! self
    println(Utils.localIp.toString + ":" + Utils.portHash(Utils.localIp))
    server.createContext("/ping", new PingHandler)
    server.createContext("/peerlist", new PeerListHandler)
    server.createContext("/fileindex", new FileIndexHandler)
    server.createContext("/files", new FileHandler(Paths.get(Settings.getSettings.sandwichPath)))
    server.start()
  }

  override def postStop() {
    server.stop(15) // If the server is not done after 15 sec, too bad...
  }

  override def receive = {
    case newPeerSet: Set[Peer] => peerSet.send(newPeerSet)
    case newFileIndex: FileIndex => fileIndex.send(newFileIndex)
  }

  private def addPeer(exchange: HttpExchange) {
    peerHandler ! exchange.getRemoteAddress.getAddress
  }
  
  abstract private class AbstractHandler extends HttpHandler {
    def handleRequest(exchange: HttpExchange)
    
    override final def handle(exchange: HttpExchange) = using[Unit, HttpExchange](exchange) { exchange =>
      addPeer(exchange)
      using(exchange.getRequestBody) { inputStream => Source.fromInputStream(inputStream).mkString }
      exchange.sendResponseHeaders(200, 0)
      handleRequest(exchange)
    }
  }
  
  abstract private class NonFileRequestHandler extends AbstractHandler {
    def respond(outputStreamWriter: OutputStreamWriter)
    
    override final def handleRequest(exchange: HttpExchange) {
      using(new OutputStreamWriter(exchange.getResponseBody)) { inputStream => respond(inputStream) }
    }
  }

  private class PingHandler extends NonFileRequestHandler {
    override def respond(outputStreamWriter: OutputStreamWriter) {
      outputStreamWriter.write("pong\n")
    }
  }

  private class PeerListHandler extends NonFileRequestHandler {
    override def respond(outputStreamWriter: OutputStreamWriter) {
      val localPeer = Peer(Utils.getLocalIp, fileIndex().IndexHash, new Date)
      outputStreamWriter.write(Peer.gson.toJson((peerSet() + localPeer).toArray[Peer]))
    }
  }

  private class FileIndexHandler extends NonFileRequestHandler {
    override def respond(outputStreamWriter: OutputStreamWriter) {
      outputStreamWriter.write(FileIndex.gson.toJson(fileIndex()))
    }
  }

  private class FileHandler(root: Path) extends AbstractHandler {
    def getFile(uri: URI): File = {
      val path = uri.getPath.replaceFirst(File.separator +  "files" + File.separator, "")
      root.resolve(path).toFile
    }

    override def handleRequest(exchange: HttpExchange) {
      val file = getFile(exchange.getRequestURI)
      using(new FileInputStream(file), exchange.getResponseBody) { (fileReader, responseBody) =>
        new ChunkyWriter(responseBody).write(fileReader, file.length)
      }
    }
  }
}

object Server {
  def props(peerHandler: ActorRef, directoryWatcher: ActorRef) = Props(classOf[Server], peerHandler, directoryWatcher)
}