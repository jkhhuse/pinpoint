package com.chinamobile.cmss.pinpoint.hbase;

import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class HBaseUtil {
    public static String getHostPort(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            String hostName = SocketAddressUtils.getHostNameFirst(inetSocketAddress);
            if (hostName == null) {
                return HBaseConstants.UNKNOWN_ADDRESS;
            }
            return HostAndPort.toHostAndPortString(hostName, inetSocketAddress.getPort());
        }
        return HBaseConstants.UNKNOWN_ADDRESS;
    }
}
