package utils

import java.io.{FileReader, FileWriter, StringWriter, File}
import java.nio.file.{Paths, Files}
import com.google.gson.Gson
import java.nio.charset.Charset
import java.nio.ByteBuffer

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
class Settings {
  var sandwichPath = "/home/brendan/sandwich"
}

object NullSettings extends Settings

object Settings {
  val settingsPath = Utils.configPath + File.separator + "settings.json"

  private var _settings: Settings = NullSettings

  def getSettings: Settings = {
    if(_settings != NullSettings) {
      return _settings
    }
    val file = new File(Utils.configPath)
    if(!file.isDirectory()) {
      if(!file.mkdir) {
        return NullSettings
      }
    }
    val settingsFile = new File(settingsPath)
    if(!settingsFile.exists) {
      if(!settingsFile.createNewFile) {
        return NullSettings
      }
      _settings = new Settings
    }
    _settings = readSettings match {
      case Some(settings) => settings
      case None => new Settings
    }
    return _settings
  }

  def readSettings: Option[Settings] = {
    try {
      val encoded = Files.readAllBytes(Paths.get(settingsPath))
      val encoding = Charset.defaultCharset()
      (new Gson).fromJson(encoding.decode(ByteBuffer.wrap(encoded)).toString, classOf[Settings]) match {
        case settings: Settings => Option(settings)
        case _ => Option.empty
      }
    } catch {
      case _ => Option.empty
    }
  }

  def writeSettings(settings: Settings) {
    val writer = new FileWriter(Settings.settingsPath)
    writer.write((new Gson).toJson(settings, classOf[Settings]))
    writer.close
  }
}
