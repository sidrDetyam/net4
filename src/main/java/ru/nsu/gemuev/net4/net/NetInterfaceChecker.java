package ru.nsu.gemuev.net4.net;

import lombok.NonNull;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Optional;

public class NetInterfaceChecker {

    private NetInterfaceChecker() {
    }

    public static Optional<InetAddress> getInetIpaddress(@NonNull NetworkInterface networkInterface, boolean isIpv6) {
        var it = networkInterface.getInetAddresses();
        while (it.hasMoreElements()) {
            var address = it.nextElement();
            if (isIpv6 && address instanceof Inet6Address || !isIpv6 && address instanceof Inet4Address) {
                return Optional.of(address);
            }
        }

        return Optional.empty();
    }

    public static Optional<NetworkInterface> findAnyUpNetworkInterface(boolean isIpv6) throws IOException {
        var interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            var iface = interfaces.nextElement();
            if (iface.isUp() && !iface.isLoopback() && iface.supportsMulticast()) {
                if (isIpv6 && getInetIpaddress(iface, true).isPresent() ||
                        !isIpv6 && getInetIpaddress(iface, false).isPresent()) {

                    return Optional.of(iface);
                }
            }
        }

        return Optional.empty();
    }
}