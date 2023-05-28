package ru.nsu.vorobev.chat.server.protocolrealisation.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.nsu.vorobev.chat.network.connection.*;
import ru.nsu.vorobev.chat.server.protocolrealisation.xml.factory.Operable;
import ru.nsu.vorobev.chat.server.ChatServer;
import ru.nsu.vorobev.chat.server.protocolrealisation.Connection;
import ru.nsu.vorobev.chat.server.protocolrealisation.Log;
import ru.nsu.vorobev.chat.server.protocolrealisation.MessageType;
import ru.nsu.vorobev.chat.server.protocolrealisation.User;
import ru.nsu.vorobev.chat.server.protocolrealisation.xml.factory.Factory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class XMLProtocol implements TCPConnectionListener, Connection {

    private static int ID = 0;
    private final ChatServer server;
    private final DocumentBuilder builder;
    private final LSSerializer writer;
    private final LSOutput lsOutput;
    private final StringWriter stringWriter = new StringWriter();
    private final Context context;
    private final Factory operatorsFactory;

    private final int port;

    public XMLProtocol(int port, ChatServer server) {
        this.port = port;
        this.server = server;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            writer = impl.createLSSerializer();
            lsOutput = impl.createLSOutput();
            lsOutput.setEncoding("UTF-8");
            lsOutput.setCharacterStream(stringWriter);
        } catch (ParserConfigurationException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        context = new Context(server,builder,lsOutput,stringWriter,writer);
        operatorsFactory = new Factory();
    }

    @Override
    public void start() {
        System.out.println("Server running...");
        Log.log(Log.getTime() + ":Server start working",Log.TypeOfLoggers.INFO);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    new TCPConnectionByte(this, serverSocket.accept());
                } catch (IOException ex) {
                    Log.log(Log.getTime() + ":TCPConnection exception:" + ex, Log.TypeOfLoggers.ERROR);
                } catch (UserWithSameName ignored) {
                }

            }
        } catch (IOException ex) {
            Log.log(Log.getTime() + ":Server socket error! Server shutdown... exception:" + ex, Log.TypeOfLoggers.ERROR);
            throw new RuntimeException(ex);
        }
    }



    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        Document ans = builder.newDocument();
        Element rootElement = ans.createElement("event");
        rootElement.setAttribute("name", "userlogin");
        ans.appendChild(rootElement);
        String name = "";
        for (TCPConnection connection : server.getUsers().keySet()) {
            if (connection == tcpConnection) {
                name = server.getUsers().get(connection).getNickname();
                break;
            }
        }
        Element nameElement = ans.createElement("name");
        nameElement.setTextContent(name);
        rootElement.appendChild(nameElement);
        stringWriter.getBuffer().setLength(0);
        writer.write(ans, lsOutput);
        Log.log(Log.getTime() + ":Client connected. Nickname:" +name + " ID=" + ID, Log.TypeOfLoggers.INFO);
        Utils.broadCastMessage(context,stringWriter.toString());
        for (MessageType msg : server.getMessages()) {
            tcpConnection.sendData(Utils.getMessage(context,msg.getMsg(),msg.getSender()).getBytes());
        }
        System.out.println("User connected");
    }

    @Override
    public synchronized void onReceiveData(TCPConnection tcpConnection, byte[] obj) {

        try {
            Document reqv = builder.parse(new InputSource(new StringReader(new String(obj, StandardCharsets.UTF_8))));
            Element reqvElement = (Element) reqv.getElementsByTagName("command").item(0);
            String name = reqvElement.getAttribute("name");
            Operable operator = operatorsFactory.getOperator(name);
            context.setDocument(reqv);
            operator.doOperation(context,tcpConnection);

        } catch (IOException | SAXException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnectionSerializable) {
        String name = null;
        for (TCPConnection connection : server.getUsers().keySet()) {
            if (tcpConnectionSerializable == connection) {
                name = server.getUsers().get(connection).getNickname();
                server.getUsers().remove(connection);
                break;
            }
        }
        Document ans = builder.newDocument();
        Element rootElement = ans.createElement("event");
        rootElement.setAttribute("name", "userlogout");
        ans.appendChild(rootElement);

        Element nameElement = ans.createElement("name");
        nameElement.setTextContent(name);
        rootElement.appendChild(nameElement);
        stringWriter.getBuffer().setLength(0);
        writer.write(ans, lsOutput);
        Utils.broadCastMessage(context,stringWriter.toString());
        Log.log(Log.getTime() + ":Client disconnected. Nickname:" +name,Log.TypeOfLoggers.INFO);
        System.out.println("User disconected");
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnectionSerializable, Exception ex) {
        Log.log(Log.getTime() + ":" + ex, Log.TypeOfLoggers.WARNING);
    }

    @Override
    public void onRegistration(TCPConnection tcpConnection) throws IOException, ClassNotFoundException {
        try {
            byte[] in = tcpConnection.receiveData();
           // System.out.println(str);
            if(in == null){
                throw new SAXException();
            }

            Document answer = builder.parse(new InputSource(new StringReader(new String(in, StandardCharsets.UTF_8))));
            System.out.println("Got data");
            Element ansElement = (Element) answer.getElementsByTagName("command").item(0);
            String name = ansElement.getAttribute("name");
            if (!name.equals("login")) {
                throw new SocketException("Bad input");
            }

            String userName = null;

            // ansElement = (Element) answer.getElementsByTagName("name").item(0);


            NodeList nodeList = ansElement.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    userName = nodeList.item(i).getTextContent();
                    break;
                }
            }
            Document doc = builder.newDocument();

            for (User user : server.getUsers().values()) {
                if (Objects.equals(user.getNickname(), userName)) {

                    Element rootElement = doc.createElement("error");
                    doc.appendChild(rootElement);

                    Element reason = doc.createElement("reason");
                    reason.setTextContent("Exist user with same name");
                    rootElement.appendChild(reason);
                    stringWriter.getBuffer().setLength(0);
                    writer.write(doc, lsOutput);
                    String ans = stringWriter.toString();
                    System.out.println(ans);
                    tcpConnection.sendData(ans.getBytes());
                    tcpConnection.disconnect();
                    Log.log(Log.getTime() + ":Client try to connect with exist nickname connection was closed. Nickname:" + userName,Log.TypeOfLoggers.INFO);
                    throw new UserWithSameName("Exist user with same name");
                }
            }
            Element rootElement = doc.createElement("success");
            doc.appendChild(rootElement);
            Element reason = doc.createElement("session");
            int userID = ID++;
            reason.setTextContent("" + userID);
            rootElement.appendChild(reason);
            stringWriter.getBuffer().setLength(0);

            writer.write(doc, lsOutput);
            tcpConnection.sendData(stringWriter.toString().getBytes());
            server.getUsers().put(tcpConnection,new User(userName,userID));
        } catch (SAXException exception) {
            Document document = builder.newDocument();
            Element root = document.createElement("error");
            document.appendChild(root);
            Element reason = document.createElement("reason");
            reason.setTextContent("Wrong protocol");
            root.appendChild(reason);
            stringWriter.getBuffer().setLength(0);
            writer.write(reason, lsOutput);
            tcpConnection.sendData(stringWriter.toString().getBytes());
            Log.log(Log.getTime() + ":TCPConnection try to connect with wrong protocol " + tcpConnection, Log.TypeOfLoggers.WARNING);
            throw new ClassNotFoundException("Wrong protocol");



           // exception.printStackTrace();
        }
    }
}
