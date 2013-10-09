package sandwich.client.peer

import java.net.InetAddress
import java.util.Date
import com.google.gson._
import sandwich.utils.{DateDeserializer, DateSerializer}

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 4:31 AM
 * To change this template use File | Settings | File Templates.
 */
case class Peer(var IP: InetAddress, var IndexHash: Long, var LastSeen: Date) {

  override def equals(value: Any) = if(value.isInstanceOf[Peer]) {
    value.asInstanceOf[Peer].IP == IP
  } else {
    false
  }
}

object Peer {
  def gson: Gson = {
    val gson = new GsonBuilder
    gson.registerTypeAdapter(classOf[Date], new DateSerializer)
    gson.registerTypeAdapter(classOf[Date], new DateDeserializer)
    gson.create()
  }
}