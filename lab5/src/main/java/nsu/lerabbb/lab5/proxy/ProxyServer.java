package nsu.lerabbb.lab5.proxy;

import nsu.lerabbb.lab5.proxy.exceptions.DnsNotFoundException;
import nsu.lerabbb.lab5.proxy.exceptions.ProxyServerException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

public class ProxyServer {
    private final static Logger logger = LogManager.getLogger(ProxyServer.class);

    private int srcPort;
    private ServerSocketChannel serverSocket;
    private Selector selector;
    private DnsResolver dnsResolver;
    private ArrayList<ConnectionTunnel> tunnels;

    public ProxyServer(int srcPort) throws ProxyServerException {
        this.srcPort = srcPort;
        this.tunnels = new ArrayList<>();

        try {
            this.selector = Selector.open();

            this.serverSocket = ServerSocketChannel.open();
            this.serverSocket.bind(new InetSocketAddress("localhost", this.srcPort));

            this.serverSocket.configureBlocking(false);
            this.dnsResolver = new DnsResolver(false);

            this.serverSocket.register(this.selector, SelectionKey.OP_ACCEPT);
            this.dnsResolver.getChannel().register(this.selector, SelectionKey.OP_READ);
        } catch (IOException | DnsNotFoundException exception) {
            throw new ProxyServerException();
        }
    }

    public Selector getSelector() {
        return selector;
    }

    public void run() throws IOException, ProxyServerException {
        String str = String.format("Proxy server up on:\n%s:%s",
                serverSocket.socket().getInetAddress().getHostAddress(),
                serverSocket.socket().getLocalPort()
        );

        System.out.println(str);
        logger.info(str);

        while (true) {
            if (selector.select() == 0) {
                continue;
            }

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isReadable() & (dnsResolver.getChannel() == key.channel())) {
                    //resolve dns для какого-то соединения
                    AsyncDnsResolverAnswer answer = dnsResolver.asyncResolveResponse();

                    String str2 = String.format("new dns answer: id %s ip: %s",
                            answer.requestId,
                            answer.ipAddress.get(0)
                    );
                    System.out.println(str2);
                    logger.info(str2);

                    for (ConnectionTunnel tunnel : tunnels) {
                        if (tunnel.getDnsRequestId() == answer.requestId) {
                            tunnel.setDestServer(answer);
                            break;
                        }
                    }
                    continue;
                }

                if (key.isAcceptable()) {
                    acceptConnection();
                    continue;
                }
                if (key.isReadable()) {
                    processClient(key);
                }
            }
        }
    }

    private void acceptConnection() {
        try {
            SocketChannel client = serverSocket.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            ConnectionTunnel tunnel = new ConnectionTunnel(this, dnsResolver, client);
            tunnels.add(tunnel);
        } catch (IOException exception) {
            System.out.println("impossible to accept new connection");
            logger.error("impossible to accept new connection");
        }
    }

    private void processClient(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ConnectionTunnel tunnel = findTunnelBySocketChannel(channel);

        if (tunnel.isConfigured()) {
            tunnel.resendData(channel);
        }
        else {
            tunnel.configureConnection();
        }
    }

    public void removeConnection(ConnectionTunnel tunnel) {
        tunnels.remove(tunnel);
    }

    private ConnectionTunnel findTunnelBySocketChannel(SocketChannel socket) {
        for (ConnectionTunnel tunnel : tunnels) {
            if (tunnel.getClient().equals(socket)) {
                return tunnel;
            }

            if (tunnel.getDestServer() != null && tunnel.getDestServer().equals(socket)) {
                return tunnel;
            }
        }
        throw new IllegalArgumentException();
    }
}
