package utils

import java.io.File
import java.nio.file.Files

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
class Settings {
  var sandwichPath = "~/sandwich"
}

object NullSettings extends Settings

object Settings {
  val settingsPath = Utils.configPath + File.pathSeparator + "settings.xml"

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
    _settings = readSettings
    return _settings
  }

  def readSettings = new Settings
}
