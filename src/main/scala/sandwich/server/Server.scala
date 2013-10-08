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
import akka.actor.Identify

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

  override def preStart {
    peerHandler ! Identify("Server")
    directoryWatcher ! Identify("Server")
    println(Utils.localIp.toString + ":" + Utils.portHash(Utils.localIp))
    server.createContext("/ping", new PingHandler)
    server.createContext("/peerlist", new PeerListHandler)
    server.createContext("/fileindex", new FileIndexHandler)
    server.createContext("/files", new FileHandler(Paths.get(Settings.getSettings.sandwichPath)))
    server.start()
  }

  override def postStop {
    server.stop(15) // If the server is not done after 15 sec, too bad...
  }

  override def receive = {
    case newPeerSet: Set[Peer] => {
      peerSet.send(newPeerSet)
      println(peerSet())
    }
    case newFileIndex: FileIndex => {
      fileIndex.send(newFileIndex)
      println(fileIndex())
    }
  }

  private def addPeer(exchange: HttpExchange) {
    peerHandler ! exchange.getRemoteAddress.getAddress
  }

  private class PingHandler extends HttpHandler {
    override def handle(exchange: HttpExchange) {
      addPeer(exchange)
      Source.fromInputStream(exchange.getRequestBody).mkString
      exchange.getRequestBody.close
      exchange.sendResponseHeaders(200, 0)
      val responseBody = new OutputStreamWriter(exchange.getResponseBody)
      responseBody.write("pong\n")
      responseBody.close
      exchange.close
    }
  }

  private class PeerListHandler extends HttpHandler {
    override def handle(exchange: HttpExchange) {
      addPeer(exchange)
      Source.fromInputStream(exchange.getRequestBody).mkString
      exchange.getRequestBody.close
      exchange.sendResponseHeaders(200, 0)
      val responseBody = new OutputStreamWriter(exchange.getResponseBody)
      val localPeer = Peer(Utils.getLocalIp, fileIndex().IndexHash, new Date)
      responseBody.write(Peer.gson.toJson((peerSet() + localPeer).toArray[Peer]))
      responseBody.close
      exchange.close
    }
  }

  private class FileIndexHandler extends HttpHandler {
    override def handle(exchange: HttpExchange) {
      addPeer(exchange)
      Source.fromInputStream(exchange.getRequestBody).mkString
      exchange.getRequestBody.close
      exchange.sendResponseHeaders(200, 0)
      val responseBody = new OutputStreamWriter(exchange.getResponseBody)
      val json: String = FileIndex.gson.toJson(fileIndex())
      responseBody.write(json)
      responseBody.close
      exchange.close
    }
  }

  private class FileHandler(root: Path) extends HttpHandler {
    def getFileReader(uri: URI): FileReader = {
      val path = uri.getPath.replaceFirst("/files/", "")
      return new FileReader(root.resolve(path).toFile())
    }

    override def handle(exchange: HttpExchange) {
      addPeer(exchange)
      Source.fromInputStream(exchange.getRequestBody).mkString
      exchange.getRequestBody.close()
      exchange.sendResponseHeaders(200, 0)
      val file = getFileReader(exchange.getRequestURI)
      val responseBody = new OutputStreamWriter(exchange.getResponseBody)
      val chunkyWriter = new ChunkyWriter(responseBody)
      chunkyWriter.write(file)
      file.close()
      responseBody.close()
      exchange.close()
    }
  }
}

object Server {
  def props(peerHandler: ActorRef, directoryWatcher: ActorRef) = Props(classOf[Server], peerHandler, directoryWatcher)
}