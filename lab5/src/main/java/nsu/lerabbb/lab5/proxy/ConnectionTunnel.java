package nsu.lerabbb.lab5.proxy;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class ConnectionTunnel {
    private final static Logger logger = LogManager.getLogger(ConnectionTunnel.class);

    private final int BufferSize = 4096;

    @Getter @Setter
    private SocketChannel client;
    @Getter
    private SocketChannel destServer;

    private Selector serverSelector;
    private DnsResolver dnsResolver;
    private ProxyServer proxyServer;

    @Getter
    private int dnsRequestId;
    @Getter
    private boolean isConfigured;
    private boolean isWaitingDnsResponse;
    private int stepOfAuthentication;

    private int requestMode;
    private String destResource;
    private int destPort;

    public ConnectionTunnel(ProxyServer proxyServer, DnsResolver dnsResolver) {
        serverSelector = proxyServer.getSelector();
        this.dnsResolver = dnsResolver;
        this.proxyServer = proxyServer;
        dnsRequestId = -1;
        isConfigured = false;
        isWaitingDnsResponse = false;
        stepOfAuthentication = 0;
    }

    public ConnectionTunnel(ProxyServer proxyServer, DnsResolver dnsResolver, SocketChannel client) {
        this(proxyServer, dnsResolver);
        this.client = client;
    }

    public ConnectionTunnel(SocketChannel client, SocketChannel destServer, ProxyServer proxyServer, DnsResolver dnsResolver) {
        this.client = client;
        this.destServer = destServer;
        this.dnsResolver = dnsResolver;
        this.proxyServer = proxyServer;
        serverSelector = proxyServer.getSelector();
        isConfigured = true;
        isWaitingDnsResponse = false;
    }
    public void setDestServer(AsyncDnsResolverAnswer answer) throws IOException {
        if (isWaitingDnsResponse) {
            createDestServerSocket(getAsyncDnsResponse(answer));
        }
    }

    public void configureConnection() throws IOException {
        try {
            ByteBuffer recvBuffer = ByteBuffer.allocate(BufferSize);
            int read = client.read(recvBuffer);
            if (read == -1) {
                throw new IOException();
            }

            if (stepOfAuthentication == 0 & isCorrectFirstGreeting(recvBuffer)) {
                ByteBuffer sendBuf = ByteBuffer.allocate(2);
                sendBuf.put(0, (byte) 0x05);
                sendBuf.put(1, (byte) 0x00);
                client.write(sendBuf);
                stepOfAuthentication++;
                return;
            }

            InetSocketAddress destAddr;
            if (stepOfAuthentication == 1 & isCorrectSecondGreeting(recvBuffer.array())) {
                switch (recvBuffer.get(3)) {
                    case 0x01 -> {
                        requestMode = 0x01;
                        destAddr = createAddrByIpv4(recvBuffer);
                    }
                    case 0x03 -> {
                        requestMode = 0x03;
                        makeAsyncDnsRequest(recvBuffer);
                        return;
                    }
                    default -> {
                        return;
                    }
                }

                createDestServerSocket(destAddr);
            }
        } catch (IOException exception) {
            System.out.println("impossible to create connection with client");
            logger.error("impossible to create connection with client");

            client.close();
            client.keyFor(serverSelector).cancel();
            proxyServer.removeConnection(this);
        }
    }

    private void createDestServerSocket(InetSocketAddress destAddr) throws IOException {
        destServer = SocketChannel.open();
        destServer.socket().connect(destAddr);
        destServer.configureBlocking(false);
        destServer.register(serverSelector, SelectionKey.OP_READ);

        isConfigured = true;
        ByteBuffer sendBuffer = ByteBuffer.allocate(4 + destResource.getBytes().length + 2);
        sendBuffer.put(0, (byte) 0x05);
        sendBuffer.put(1, (byte) 0x00);
        sendBuffer.put(2, (byte) 0x00);
        sendBuffer.put(3, (byte) requestMode);
        sendBuffer.put(4, destResource.getBytes());
        sendBuffer.putShort(4 + destResource.getBytes().length, (short) destPort);
        client.write(sendBuffer);

        String str = String.format("Client: %s:%s connection to %s:%s success!",
                client.socket().getInetAddress().getHostName(),
                client.socket().getPort(),
                destServer.socket().getInetAddress().getHostName(),
                destPort
        );

        System.out.println(str);
        logger.info(str);
    }

    public void resendData(SocketChannel socket) throws IOException {
        ByteBuffer recvBuffer = ByteBuffer.allocate(BufferSize);
        try {
            if (socket.equals(destServer)) {
                int read = destServer.read(recvBuffer);
                if (read == -1)
                    throw new NotYetConnectedException();

                ByteBuffer data = ByteBuffer.allocate(read);
                System.arraycopy(recvBuffer.array(), 0, data.array(), 0, read);

                client.write(data);
                String str = String.format("resend %s bytes, from %s:%s to %s:%s",
                        read,
                        destServer.socket().getInetAddress().getHostName(),
                        destServer.socket().getPort(),
                        client.socket().getInetAddress().getHostName(),
                        client.socket().getPort()
                );
                logger.info(str);

            } else {
                int read = client.read(recvBuffer);
                if (read == -1)
                    throw new NotYetConnectedException();

                ByteBuffer data = ByteBuffer.allocate(read);
                System.arraycopy(recvBuffer.array(), 0, data.array(), 0, read);

                destServer.write(data);
                String str = String.format("resend %s bytes, from %s:%s to %s:%s",
                        read,
                        client.socket().getInetAddress().getHostName(),
                        client.socket().getPort(),
                        destServer.socket().getInetAddress().getHostName(),
                        destServer.socket().getPort()
                );
                logger.info(str);
            }
        } catch (IOException | NotYetConnectedException exception) {
            System.out.println("client " + client.socket().getInetAddress().getHostName() + " close connection");
            System.out.println("remove: " + client.socket().getInetAddress().getHostName() + ":" + client.socket().getPort());
            destServer.close();
            client.close();
            destServer.keyFor(serverSelector).channel();
            client.keyFor(serverSelector).cancel();
            proxyServer.removeConnection(this);
        }
    }

    private InetSocketAddress createAddrByIpv4(ByteBuffer recvBuffer) throws UnknownHostException {
        ByteBuffer ipv4Addr = ByteBuffer.allocate(4);
        ipv4Addr.put(recvBuffer.slice(4, 4));
        destResource = new String(recvBuffer.slice(4, 4).array());
        destPort = recvBuffer.getShort(4);

        return new InetSocketAddress(InetAddress.getByAddress(ipv4Addr.array()), destPort);
    }

    private void makeAsyncDnsRequest(ByteBuffer recvBuffer) throws IOException {
        int urlLength = recvBuffer.get(4);
        ByteBuffer url = ByteBuffer.allocate(urlLength);
        System.arraycopy(recvBuffer.array(), 5, url.array(), 0, urlLength);


        destPort = recvBuffer.getShort(5 + urlLength);
        ByteBuffer destResourceBytes = ByteBuffer.allocate(recvBuffer.get(4) + 1);
        System.arraycopy(recvBuffer.array(), 4, destResourceBytes.array(), 0, recvBuffer.get(4) + 1);
        destResource = new String(destResourceBytes.array());

        dnsRequestId = dnsResolver.asyncResolveRequest(new String(url.array()));
        isWaitingDnsResponse = true;
    }

    private InetSocketAddress getAsyncDnsResponse(AsyncDnsResolverAnswer answer) {
        isWaitingDnsResponse = false;
        return new InetSocketAddress(answer.ipAddress.get(0), destPort);
    }

    private boolean isCorrectFirstGreeting(ByteBuffer recvBuffer) {
        int clientAuthMethodsCount = recvBuffer.get(1);
        boolean isWithNoAuthentication = false;

        for (int i = 0; i < clientAuthMethodsCount; i++) {
            if (recvBuffer.get(i + 2) == 0x00) {
                isWithNoAuthentication = true;
                break;
            }
        }

        // SOCKS version (0x05)
        return !(recvBuffer.get(0) != 0x05 | !isWithNoAuthentication);
    }

    private boolean isCorrectSecondGreeting(byte[] recvBuffer) {
        return (recvBuffer[0] == 0x05 & recvBuffer[1] == 0x01 & recvBuffer[2] == 0x00);
    }
}
