package com.liumeo.landlords;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;


public class IPHelper {
    private static int prefix;//前缀（子关掩码）长度
    private static InetAddress localhost;//局域网所在地址
    public static final int PORT_START = 12290;
    public static final int PORT_SPAN = 255;

    public static void initialize() {
        //初始化prefix和localhost
        try {
            List<NetworkInterface> ns = Collections.list(NetworkInterface.getNetworkInterfaces());
            outter:
            for (NetworkInterface net : ns) {
                for (InterfaceAddress interfaceAddress : net.getInterfaceAddresses()) {
                    InetAddress tmp = interfaceAddress.getAddress();
                    if (tmp instanceof Inet4Address &&
                            !tmp.isLoopbackAddress() &&
                            !tmp.isAnyLocalAddress()) {
                        prefix = interfaceAddress.getNetworkPrefixLength() - 1;
                        //获取子关掩码长度
                        localhost = tmp;
                        //得到ip地址
                        break outter;
                    }
                }
            }
        } catch (Exception ignored) {
            //忽略异常并重置信息
            prefix = -1;
            localhost = null;
        }
    }

    public static InetAddress getSocketAddress(ServerSocket socket) {
        //通过ServerSocket得到地址
        InetSocketAddress address = (InetSocketAddress) socket.getLocalSocketAddress();
        return address.getAddress();
    }

    public static InetAddress getLocalhost() {
        return localhost;
    }

    public static int getIP4fromBytes(byte[] ip) {
        //从byte型的ip地址得到IPV4地址
        return ByteBuffer.wrap(ip).getInt();
    }

    public static byte[] getIP4fromInt(int ip) {
        //从int型的ip地址得到IPV4地址
        return ByteBuffer.allocate(4).putInt(ip).array();
    }

    public static String getCode(int port) {
        //将端口转换成code（房间号）
        if (prefix >= 0) {
            int ip = getIP4fromBytes(localhost.getAddress());
            int mask = ~((1 << 31) >> prefix);
            port -= PORT_START;
            //暴力使用子关掩码得到code
            //如192.168.1.101:12290将变成0065
            return String.format("%02x%x", port, ip & mask);
        }
        return "Unknown";
    }

    public static InetSocketAddress getAddressFromCode(String code) {
        //将房间号转换成Socket地址
        if (code.length() > 2) {
            int mask = (1 << 31) >> prefix;
            int ip = (getIP4fromBytes(localhost.getAddress()) & mask) + Integer.parseInt(code.substring(2), 16);
            int port = Integer.parseInt(code.substring(0, 2), 16) + PORT_START;
            //与上一步完全相反
            try {
                return new InetSocketAddress(InetAddress.getByAddress(getIP4fromInt(ip)), port);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}