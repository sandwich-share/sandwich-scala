package sandwich.client.blackwhitelist

import java.net.{InetAddress, Inet6Address, Inet4Address}
import sandwich.client.blackwhitelist.IPRange.toIPAddress

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 10/10/13
 * Time: 8:21 PM
 */
object WhiteBlackList {
  import sandwich.client.blackwhitelist.IPRange.toIPv4Address
  val whiteListIPv4: IPSet[Inet4Address] =
    IPRange("129.22.0.0", "129.22.255.255") union     // CWRUNET
    IPRange("173.241.224.0", "173.241.239.255") union // Hessler
    IPRange("127.0.0.0", "127.255.255.255") union     // IPv4 Subnet
    IPRange("192.5.109.0", "192.5.109.255") union     // CWRUNET-C0
    IPRange("192.5.110.0", "192.5.110.255") union     // CWRUNET-C1
    IPRange("192.5.111.0", "192.5.111.255") union     // CWRUNET-C2
    IPRange("192.5.112.0", "192.5.112.255") union     // CWRUNET-C3
    IPRange("192.5.113.0", "192.5.113.255")           // CWRUNET-C4
  val whiteListIPv6 = IPSet[Inet6Address](Set())
  val blackListIPv4 = IPSet[Inet4Address](Set())
  val blackListIPv6 = IPSet[Inet6Address](Set())

  def validate(address: InetAddress): Boolean = address match {
    case ipv4: Inet4Address => (ipv4 in whiteListIPv4) && (ipv4 notIn blackListIPv4)
    case ipv6: Inet6Address => (ipv6 in whiteListIPv6) && (ipv6 notIn blackListIPv6)
  }
}
