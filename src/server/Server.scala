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
import fileindex.FileIndex
import scala.io.Source

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

  private def addPeer(exchange: HttpExchange) {
    peerHandler.core ! exchange.getRemoteAddress.getAddress
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
      val fileHash = (fileWatcher !? DirectoryWatcher.FileHashRequest).asInstanceOf[Int]
      val peerSet = (peerHandler !? PeerHandler.PeerSetRequest).asInstanceOf[Set[Peer]] + Peer(Utils.localIp, fileHash, new Date)
      responseBody.write(Peer.gson.toJson(peerSet.toArray[Peer]))
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
      val fileIndex = (fileWatcher !? DirectoryWatcher.FileIndexRequest).asInstanceOf[FileIndex]
      responseBody.write(FileIndex.gson.toJson(fileIndex))
      responseBody.close
      exchange.close
    }
  }

  private class FileHandler(root: Path) extends HttpHandler {
    def getFile(uri: URI): Path = {
      val path = uri.getPath.replaceFirst("/files/", "")
      root.resolve(path)
    }
    override def handle(exchange: HttpExchange) {
      addPeer(exchange)
      Source.fromInputStream(exchange.getRequestBody).mkString
      exchange.getRequestBody.close
      exchange.sendResponseHeaders(200, 0)
      val file = getFile(exchange.getRequestURI)
      val responseBody = exchange.getResponseBody
      val bodyWriter = new ByteArrayOutputStream()
      // TODO: We should not read an entire file into memory.
      bodyWriter.write(Files.readAllBytes(file))
      bodyWriter.writeTo(responseBody)
      bodyWriter.close
      exchange.close
    }
  }
}