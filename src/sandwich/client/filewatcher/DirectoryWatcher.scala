package sandwich.client.filewatcher

import java.nio.file._
import java.nio.file.StandardWatchEventKinds._
import java.nio.file.LinkOption._
import scala.collection.mutable.HashMap
import java.nio.file.attribute.BasicFileAttributes
import scala.actors.Actor
import sandwich.client.fileindex.{FileItem, FileIndex}
import scala.collection.convert.Wrappers.JListWrapper
import sandwich.client.filewatcher.DirectoryWatcher.{FileHashRequest, FileIndexRequest}
import sandwich.controller

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
class DirectoryWatcher(val rootDirectory: Path) extends Actor {
  private val core = new DirectoryWatcherCore
  private var fileIndex = FileIndex(Set[FileItem]())

  override def act {
    core.start

    while(true) {
      receive {
        case FileIndexRequest => reply(fileIndex)
        case FileHashRequest => reply(fileIndex.IndexHash)
        case newFileIndex: FileIndex => fileIndex = newFileIndex
        case _ =>
      }
    }
  }

  private class DirectoryWatcherCore extends Actor {
    private val watcher = FileSystems.getDefault.newWatchService
    private val fileWatcherMap = new HashMap[WatchKey, Path]
    registerAll(rootDirectory)
    updateFileIndex

    private def register(dir: Path) {
      val key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
      fileWatcherMap(key) = dir
    }

    private def registerAll(root: Path) {
      Files.walkFileTree(root, new DirectoryWatcherVisitor)
    }

    private class DirectoryWatcherVisitor extends SimpleFileVisitor[Path] {
      override def preVisitDirectory(dir: Path, attributes: BasicFileAttributes) = {
        register(dir)
        FileVisitResult.CONTINUE
      }
    }

    private def updateFileIndex {
      // TODO: The checksum in fileItem is set to zero to match the canonical version; nevertheless, we should fix this.
      DirectoryWatcher.this ! FileIndex((for {(key, path) <- fileWatcherMap} yield FileItem(path.toString, path.toFile.length, 0)).toSet)
    }

    override def act {
      while(true) {
        try {
          val key = watcher.take
          val path = fileWatcherMap(key)
          for (raw_event <- JListWrapper[WatchEvent[_]](key.pollEvents())) {
            if(raw_event != OVERFLOW) {
              val event = raw_event.asInstanceOf[WatchEvent[Path]]
              val kind = event.kind
              val name = event.context
              val child = path.resolve(name)
              if (kind == ENTRY_CREATE && Files.isDirectory(child, NOFOLLOW_LINKS)) {
                registerAll(child)
              }
              if (!key.reset) {
                fileWatcherMap.remove(key)
              }
            }
          }
        } catch {
          case e: Exception => println("Error")
        }
        updateFileIndex
      }
    }
  }
}

object DirectoryWatcher {
  abstract class Request extends controller.Request
  object FileIndexRequest extends DirectoryWatcher.Request
  object FileHashRequest extends DirectoryWatcher.Request
}