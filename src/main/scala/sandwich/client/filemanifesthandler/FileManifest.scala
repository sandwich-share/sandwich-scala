package sandwich.client.filemanifesthandler

import sandwich.client.fileindex.{FileItem, FileIndex}
import sandwich.client.peer.Peer

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/19/13
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */
class FileManifest(manifestMap: Map[Peer, FileIndex]) {

  lazy val filePeerMap: Map[FileItem, Peer] = manifestMap.flatMap{ case(peer, index) => index.List.map(item => (item, peer)).toMap }

  def search(filter: String => Boolean): Set[FileItem] = filePeerMap.filter{ case(item, _) => filter(item.FileName) }.keys.toSet[FileItem]
}

object FileManifest {
  def simpleSearch(string: String): String => Boolean = { (s: String) => s.contains(string) }
}
