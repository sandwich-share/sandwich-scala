package filemanifesthandler

import scala.collection.{immutable, mutable}
import fileindex.{FileItem, FileIndex}
import peer.Peer

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/19/13
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */
class FileManifest(manifestMap: mutable.Map[Peer, FileIndex]) {

  lazy val filePeerMap: immutable.Map[FileItem, Peer] = {
    manifestMap.map{ case(peer, index) => {
      for { item <- index.fileList } yield (item, peer) }.toMap[FileItem, Peer]
    }.fold(Map[FileItem, Peer]())((left, right) => left ++ right)
  }

  def search(filter: String => Boolean): Set[FileItem] = filePeerMap.filter{ case(item, _) => filter(item.FileName) }.values.toSet[FileItem]
}
