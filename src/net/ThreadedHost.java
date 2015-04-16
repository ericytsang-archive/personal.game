package net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ThreadedHost implements Server<ServerSocket,Socket>, Client<Socket>
{
    /**
     * handle to the accept thread running on this server, accepting new
     *   connections.
     */
    private Map<ServerSocket,AcceptThread> acceptThreads;

    //////////////////////
    // public interface //
    //////////////////////

    public ThreadedHost()
    {
        acceptThreads = new LinkedHashMap<>();
    }

    /**
     * starts the server, and makes it listening for connections on the passed
     *   port.
     * @throws IOException
     */
    public ServerSocket startListening(int serverPort)
    {
        // open a new server socket
        ServerSocket sock;
        try
        {
            sock = new ServerSocket();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // pass the server socket to an AcceptThread
        if(acceptThreads.get(serverPort) == null)
        {
            
            AcceptThread acceptThread = new AcceptThread(sock,serverPort);
            acceptThreads.put(sock,acceptThread);
            acceptThread.start();
        }

        // return the ServerSocket
        return sock;
    }

    /**
     * stops the server, and closes all connections to it.
     */
    public void stopListening(ServerSocket sock)
    {
        AcceptThread acceptThread = acceptThreads.get(sock);
        if(acceptThread != null)
        {
            acceptThread.cancel();
            acceptThreads.remove(sock);
        }
    }

    /**
     * initiates a new connection to the remote server at the address
     *   {remoteAddr}:{remotePort}. when the socket connects, the onConnect
     *   callback will be invoked. if the socket fails to connect, the
     *   {onConnectFail} callback is invoked.
     *
     * @date     2015-04-12T17:48:35-0800
     *
     * @author   Eric Tsang
     *
     * @param    remoteAddr   IP address of the remote host.
     * @param    remotePort   port number of the remote host to connect to.
     *
     * @return   returns the socket that is performing the connect call.
     */
    public Socket connect(String remoteAddr, int remotePort)
    {
        Socket sock = new Socket();
        new ConnectThread(sock,remoteAddr,remotePort).start();
        return sock;
    }

    public void disconnect(Socket sock)
    {
        try
        {
            sock.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * sends a message to the client identified by the connection object.
     *
     * @param conn connection to send a message to
     * @param type type of message to send
     * @param msg message to send
     */
    public void sendMessage(Socket conn, Packet packet)
    {
        // send the message out the socket
        synchronized(conn)
        {
            try
            {
                byte[] packetBytes = packet.toBytes();
                DataOutputStream os = new DataOutputStream(
                    conn.getOutputStream());
                os.writeInt(packetBytes.length);
                os.write(packetBytes);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /////////////////////////
    // callbacks interface //
    /////////////////////////

    public abstract void onAccept(Socket conn);
    public abstract void onListenFail(ServerSocket conn, Exception e);
    public abstract void onConnect(Socket conn);
    public abstract void onConnectFail(Socket conn, Exception e);
    public abstract void onMessage(Socket conn, Packet packet);
    public abstract void onClose(Socket conn, boolean remote);

    ////////////////
    // Cancelable //
    ////////////////

    /**
     * interface implemented by threads, so then they all have a common cancel
     *   method, and can all be canceled when the server is shut down.
     */
    private interface Cancelable
    {
        public void cancel() throws IOException;
    }

    //////////////////
    // AcceptThread //
    //////////////////

    /**
     * the AcceptThread is the thread that's used to accept incoming connection
     *   requests, and pass them off to CommThread instances.
     */
    private class AcceptThread extends Thread
    {
        /**
         * socket that the AcceptThread is supposed to listen to.
         */
        private ServerSocket svrSock;

        /**
         * port number used by the {ServerSocket} to listen for connections
         *   from.
         */
        private int listeningPort;

        /**
         * instantiates a new AcceptThread that will listen to the passes server
         *   socket once the thread is started.
         *
         * @param  svrSock ServerSocket to listen to.
         */
        public AcceptThread(ServerSocket svrSock, int listeningPort)
        {
            this.svrSock = svrSock;
            this.listeningPort = listeningPort;
            setName("AcceptThread "+svrSock.getLocalPort());
        }

        /**
         * the threaded method.
         *
         * continuously accepts connections, and invokes the onAccept callback
         *   whenever a new connection is established.
         */
        @Override
        public synchronized void run()
        {
            // open the server socket for listening
            try
            {
                svrSock.bind(new InetSocketAddress(listeningPort));
            }
            catch (IOException e)
            {
                onListenFail(svrSock,e);
            }

            // accept new connections, and call callbacks
            while(true)
            {
                try
                {
                    Socket conn = svrSock.accept();
                    onAccept(conn);
                    CommThread commThread = new CommThread(conn);
                    commThread.start();
                }
                catch (IOException e)
                {
                    // notify all threads waiting for this thread to terminate
                    notifyAll();
                    break;
                }
            }
        }

        /**
         * cancels the accept thread.
         */
        public synchronized void cancel()
        {
            try
            {
                svrSock.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    ///////////////////
    // ConnectThread //
    ///////////////////

    /**
     * {ConnectThread}, when run, attempts to connect to the specified remote
     *   host. if it succeeds, it passes the socket off to a {CommThread}, and
     *   calls the {onConnect} callback. if the socket fails to connect, the
     *   {onConnectFail} callback is invoked.
     */
    private class ConnectThread extends Thread implements Cancelable
    {
        /**
         * address of the remote host to connect to.
         */
        private String remoteAddr;

        /**
         * port on the remote host to connect to.
         */
        private int remotePort;

        /**
         * socket to use to connect to the remote host.
         */
        private Socket sock;

        /**
         * instantiates a new {ConnectThread}.
         *
         * @date     2015-04-11T09:36:13-0800
         *
         * @author   Eric Tsang
         *
         * @param    remoteAddr   address of the remote host to connect to.
         * @param    remotePort   port on the remote host to connect to.
         *
         * @return   new instance of a {ConnectThread}.
         */
        public ConnectThread(Socket sock, String remoteAddr, int remotePort)
        {
            this.sock       = sock;
            this.remoteAddr = remoteAddr;
            this.remotePort = remotePort;
        }

        /**
         * the threaded part of the connect thread. this thread tries to connect
         *   with the remote host. when it succeeds, it invokes the onConnect
         *   callback, if it fail, it calls the onClose callback.
         *
         * @date     2015-04-11T18:08:54-0800
         *
         * @author   Eric Tsang
         */
        @Override
        public synchronized void run()
        {
            try
            {
                sock.connect(new InetSocketAddress(remoteAddr,remotePort));
                onConnect(sock);
                CommThread commThread = new CommThread(sock);
                commThread.start();
            }
            catch(Exception e)
            {
                onConnectFail(sock,e);
            }
        }

        /**
         * cancels the connect thread.
         * @throws IOException
         */
        public synchronized void cancel() throws IOException
        {
            sock.close();
        }
    }

    ////////////////
    // CommThread //
    ////////////////

    /**
     * the CommThread is used to listen to connections, and invoke callbacks
     *   when data is received, or if the socket is closed. When the socket is
     *   closed, the thread will terminate.
     */
    private class CommThread extends Thread implements Cancelable
    {
        /**
         * socket that's connected to a client.
         */
        private Socket sock;

        DataInputStream is;

        /**
         * constructs a new CommThread object that is used to read from the
         *   passed socket connection.
         *
         * @param  connection socket that this thread is supposed to listen to.
         */
        public CommThread(Socket connection)
        {
            setName("CommThread "+connection.getRemoteSocketAddress());

            this.sock = connection;

            // get the socket's input stream so we can read from it.
            try
            {
                is = new DataInputStream(sock.getInputStream());
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        /**
         * the threaded method.
         *
         * continuously reads from the socket, and invokes the onMessage
         *   callback whenever a new message is received from the connection.
         *   will call the onClose callback when the socket closes.
         */
        @Override
        public synchronized void run()
        {
            // continuously read from the connection, and invoke onMessage, or
            // invoke onClose when the socket is closed.
            while(true)
            {
                try
                {
                    // read from the socket & invoke onMessage callback
                    int len = is.readInt();
                    byte[] buffer = new byte[len];
                    is.read(buffer);
                    onMessage(sock,new Packet().fromBytes(buffer));
                }
                catch (SocketException e)
                {
                    // socket closed by local host
                    onClose(sock,false);
                    break;
                }
                catch (IOException e)
                {
                    // socket closed by remote host
                    onClose(sock,true);
                    break;
                }
            }

            // release resources
            try
            {
                is.close();
                sock.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        /**
         * cancels, and ends the thread.
         * @throws IOException
         */
        public synchronized void cancel() throws IOException
        {
            sock.close();
        }
    }

    //////////
    // main //
    //////////

    public static void main(String[] args) throws IOException
    {
        ThreadedHost h1 = new ThreadedHost()
        {
            public synchronized void onAccept(Socket conn)
            {
                System.out.println(conn.getLocalSocketAddress()+" accpeted.");
            }
            public synchronized void onConnect(Socket conn)
            {
                System.out.println(conn.getLocalSocketAddress()+" connected.");
                Packet p = new Packet().pushData("hey there! :D".getBytes());
                sendMessage(conn,p);
            }
            public synchronized void onMessage(Socket conn, Packet packet)
            {
                System.out.println(conn.getLocalSocketAddress()+": "
                    +new String(packet.toBytes()));
            }
            public synchronized void onClose(Socket conn, boolean remote)
            {
                System.out.printf("%s disconnected by %s host.\n",
                    conn.getLocalSocketAddress(),remote?"remote":"local");
            }
            @Override
            public void onConnectFail(Socket conn, Exception e)
            {
                // TODO Auto-generated method stub

            }
            @Override
            public void onListenFail(ServerSocket conn, Exception e)
            {
                // TODO Auto-generated method stub
                
            }
            @Override
            public void onAcceptFail(ServerSocket sock, Exception e)
            {
                // TODO Auto-generated method stub
                
            }
        };
        ThreadedHost h2 = new ThreadedHost()
        {
            public synchronized void onAccept(Socket conn)
            {
                System.out.println(conn.getLocalSocketAddress()+" accpeted.");
            }
            public synchronized void onConnect(Socket conn)
            {
                System.out.println(conn.getLocalSocketAddress()+" connected.");
                Packet p = new Packet().pushData("hey there! :D".getBytes());
                sendMessage(conn,p);
            }
            public synchronized void onMessage(Socket conn, Packet packet)
            {
                System.out.println(conn.getLocalSocketAddress()+": "
                    +new String(packet.toBytes()));
                disconnect(conn);
            }
            public synchronized void onClose(Socket conn, boolean remote)
            {
                System.out.printf("%s disconnected by %s host.\n",
                    conn.getLocalSocketAddress(),remote?"remote":"local");
            }
            @Override
            public void onConnectFail(Socket conn, Exception e)
            {
                // TODO Auto-generated method stub

            }
            @Override
            public void onListenFail(ServerSocket conn, Exception e)
            {
                // TODO Auto-generated method stub
                
            }
            @Override
            public void onAcceptFail(ServerSocket sock, Exception e)
            {
                // TODO Auto-generated method stub
                
            }
        };

        h1.startListening(7000);
        h2.startListening(7001);

        h1.connect("localhost",7001);
        h2.connect("localhost",7000);
    }
}
