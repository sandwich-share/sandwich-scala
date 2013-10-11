package sandwich.client.blackwhitelist

import java.net.{Inet6Address, Inet4Address, InetAddress}

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 10/10/13
 * Time: 4:14 AM
 */
case class IPSet[Address <: InetAddress](ipSet: Set[IPRange[Address]]) {
  def union(other: IPRange[Address]): IPSet[Address] = {
    val (merged, notMerged) = ipSet.map(_.merge(other)).toList.partition(_.isLeft)
    val toAddIn = merged.map(_.left.get) match {
      case Nil => List(other)
      case head :: Nil => List(head)
      case head :: tail => tail.map(_.merge(head).right.get)
    }
    return IPSet[Address]((notMerged.map(_.right.get) ++ toAddIn).toSet)
  }

  def union(other: IPSet[Address]): IPSet[Address] = other.ipSet.foldLeft(this) {
    case (set, range) => set.union(range)
  }
}
case class IPRange[Address <: InetAddress](min: IPAddress[Address], max: IPAddress[Address]) {

  implicit object IPAddressOrdering extends Ordering[IPAddress[Address]] {
    override def compare(x: IPAddress[Address], y: IPAddress[Address]): Int = if (x < y) -1 else if (x == y) 0 else 1
  }

  override def equals(any: Any): Boolean = any.isInstanceOf[IPRange[Address]] &&
    any.asInstanceOf[IPRange[Address]].min == min && any.asInstanceOf[IPRange[Address]].max == max

  def <(other: IPRange[Address]): Boolean = max < other.min

  def intersects(other: IPRange[Address]): Boolean = {
    val left = if (this < other) this else other
    val right = if (!(this < other)) this else other
    return left.max < right.min
  }

  def intersectsOrAdjacent(other: IPRange[Address]): Boolean = if (this < other) {
    this intersects IPRange[Address](other.min++, other.max)
  } else {
    other intersects IPRange[Address](min++, max)
  }

  def merge(other: IPRange[Address]): Either[IPRange[Address], IPRange[Address]] = if (this intersectsOrAdjacent other) {
    val addresses = Seq(min, max, other.min, other.max)
    Left(IPRange[Address](addresses.min, addresses.max))
  } else {
    Right(this)
  }

  def union(other: IPSet[Address]): IPSet[Address] = other union this

  def union(other: IPRange[Address]): IPSet[Address] = this union IPSet[Address](Set(other))
}

object IPRange {
  implicit def toIPAddress(address: Inet4Address): IPAddress[Inet4Address] = IPAddress[Inet4Address](address)
  implicit def toIPAddress(address: Inet6Address): IPAddress[Inet6Address] = IPAddress[Inet6Address](address)
  implicit def toIPv4Address(string: String): IPAddress[Inet4Address] = InetAddress.getByName(string).asInstanceOf[Inet4Address]
  implicit def toIPv6Address(string: String): IPAddress[Inet6Address] = InetAddress.getByName(string).asInstanceOf[Inet6Address]
}

case class IPAddress[Address <: InetAddress](address: Address) {

  override def equals(any: Any) = address == any

  def <(other: IPAddress[Address]): Boolean = {
    for ((left, right) <- address.getAddress.zip(other.address.getAddress)) {
      if (left < right) {
        return true
      } else if (left > right) {
        return false
      }
    }
    return false
  }

  def <=(other: IPAddress[Address]): Boolean = this < other || this == other

  def >(other: IPAddress[Address]): Boolean = !(this <= other)

  def >=(other: IPAddress[Address]): Boolean = !(this < other)

  def in(ipRange: IPRange[Address]): Boolean = this >= ipRange.min && this <= ipRange.max

  def in(ipSet: IPSet[Address]): Boolean = ipSet.ipSet.foldLeft(false) {
    case (inSet: Boolean, range: IPRange[Address]) => inSet || (this in range)
  }

  def notIn(ipSet: IPSet[Address]): Boolean = !(this in ipSet)

  def ++ : IPAddress[Address] = {
    val newAddress = address.getAddress
    var index = -1
    do {
      index += 1
      newAddress(index) = (newAddress(index) + 1).toByte
    } while (newAddress(index) == 0)
    return InetAddress.getByAddress(newAddress) match {
      case ipv4: Inet4Address => IPRange.toIPAddress(ipv4).asInstanceOf[IPAddress[Address]]
      case ipv6: Inet6Address => IPRange.toIPAddress(ipv6).asInstanceOf[IPAddress[Address]]
    }
  }
}