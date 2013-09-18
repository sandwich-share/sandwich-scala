package server

import java.net.{URI, InetSocketAddress}
import utils.Utils
import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import java.io._
import peerhandler.PeerHandler
import com.twitter.json.Json
import filewatcher.DirectoryWatcher
import java.nio.file.{Files, Path}
import peer.Peer
import java.util.Date

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 4:30 PM
 * To change this template use File | Settings | File Templates.
   */
class Server(private val peerHandler: PeerHandler, private val fileWatcher: DirectoryWatcher) {
  def startServer {
    val server = HttpServer.create(new InetSocketAddress(Utils.portHash(Utils.localIp)), 100)
    server.createContext("/ping", new PingHandler)
    server.createContext("/peerlist", new PeerListHandler)
    server.createContext("/fileindex", new FileIndexHandler)
    server.createContext("/files", new FileHandler(fileWatcher.rootDirectory))
    server.start
  }

  private class PingHandler extends HttpHandler {
    override def handle(exchange: HttpExchange) {
      val responseBody = new OutputStreamWriter(exchange.getResponseBody)
      responseBody.write("pong")
      responseBody.close
    }
  }

  private class PeerListHandler extends HttpHandler {
    override def handle(exchange: HttpExchange) {
      val responseBody = new OutputStreamWriter(exchange.getResponseBody)
      val peerList = peerHandler.peerSet + Peer(Utils.localIp, fileWatcher.fileIndex.fileHash, new Date)
      responseBody.write(Json.build(peerList).toString)
      responseBody.close
    }
  }

  private class FileIndexHandler extends HttpHandler {
    override def handle(exchange: HttpExchange) {
      val responseBody = new OutputStreamWriter(exchange.getResponseBody)
      responseBody.write(Json.build(fileWatcher.fileIndex).toString)
      responseBody.close
    }
  }

  private class FileHandler(root: Path) extends HttpHandler {
    def getFile(uri: URI): Path = {
      val path = uri.getPath.replaceFirst("/files/", "")
      root.resolve(path)
    }
    override def handle(exchange: HttpExchange) {
      val file = getFile(exchange.getRequestURI)
      val responseBody = exchange.getResponseBody
      val bodyWriter = new ByteArrayOutputStream()
      // TODO: We should not read an entire file into memory.
      bodyWriter.write(Files.readAllBytes(file))
      bodyWriter.writeTo(responseBody)
      responseBody.close
    }
  }
}