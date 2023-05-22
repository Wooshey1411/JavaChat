package ru.nsu.vorobev.chat.server.protocolrealisation;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XMLProtocol implements TCPConnectionListener, Connection {

    private static int ID = 0;

    private final List<User> users = new ArrayList<>();

    private final List<String> messagesHistory = new ArrayList<>();
    static final int maxHistoryLen = 5;

    private final DocumentBuilder builder;

    final DOMImplementationRegistry registry;
    final DOMImplementationLS impl;
    final LSSerializer writer;
    LSOutput lsOutput;
    StringWriter stringWriter = new StringWriter();

    private final int port;

    public XMLProtocol(int port) {
        this.port = port;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
            registry = DOMImplementationRegistry.newInstance();
            impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            writer = impl.createLSSerializer();
            lsOutput = impl.createLSOutput();
            lsOutput.setEncoding("UTF-8");
            lsOutput.setCharacterStream(stringWriter);
        } catch (ParserConfigurationException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void start() {
        System.out.println("Server running...");
        Log.enableLogger();
        Log.init();
        Log.log(Log.getTime() + ":Server start working", Log.TypeOfLoggers.INFO);
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

    private synchronized void broadCastMessage(String msg) {
        //  System.out.println("Broadcast: " + object.toString());
        for (User user : users) {
            user.getConnection().sendData(msg);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        Document ans = builder.newDocument();
        Element rootElement = ans.createElement("event");
        rootElement.setAttribute("name", "userlogin");
        ans.appendChild(rootElement);
        String name = "";
        for (User user : users){
            if(user.getConnection() == tcpConnection){
                name = user.getNickname();
                break;
            }
        }
        Element nameElement = ans.createElement("name");
        nameElement.setTextContent(name);
        rootElement.appendChild(nameElement);
        stringWriter.getBuffer().setLength(0);
        writer.write(ans, lsOutput);
        broadCastMessage(stringWriter.toString());
        for(String msg : messagesHistory){
            tcpConnection.sendData(msg);
        }
        System.out.println("User connected");
    }



    private synchronized boolean checkIDAndSendIfWrong(TCPConnection tcpConnection, int ID, String attribute, String reasonS){
        Document ans = builder.newDocument();
        boolean IDFound = false;
        for (User user : users) {
            if (user.getID() == ID) {
                IDFound = true;
                break;
            }
        }
        if (!IDFound) {
            Element rootElement = ans.createElement("error");
            ans.appendChild(rootElement);
            Element reason = ans.createElement("reason");
            reason.setAttribute("name",attribute);
            reason.setTextContent(reasonS);
            rootElement.appendChild(reason);
            stringWriter.getBuffer().setLength(0);
            writer.write(ans, lsOutput);
            tcpConnection.sendData(stringWriter.toString());
            return true;
        }
        return false;
    }

    private synchronized void sendSuccess(TCPConnection tcpConnection, String attribute){
        Document ans = builder.newDocument();
        Element rootElement = ans.createElement("success");
        rootElement.setAttribute("name", attribute);
        ans.appendChild(rootElement);
        stringWriter.getBuffer().setLength(0);
        writer.write(ans, lsOutput);
        String ansS = stringWriter.toString();
        tcpConnection.sendData(ansS);
    }
    @Override
    public synchronized void onReceiveData(TCPConnection tcpConnection, Object obj) {

        try {
            Document reqv = builder.parse(new InputSource(new StringReader((String) obj)));
            Element reqvElement = (Element) reqv.getElementsByTagName("command").item(0);
            String name = reqvElement.getAttribute("name");

            switch (name) {
                case "list" -> {
                    NodeList nodeList = reqvElement.getChildNodes();
                    int ID = -1;
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            ID = Integer.parseInt(nodeList.item(i).getTextContent());
                            break;
                        }
                    }
                    if (checkIDAndSendIfWrong(tcpConnection, ID, "list", "wrong session ID for get list of users")) {
                        return;
                    }
                    Document ans = builder.newDocument();
                    Element rootElement = ans.createElement("success");
                    rootElement.setAttribute("name", "list");
                    ans.appendChild(rootElement);
                    Element listusersElement = ans.createElement("listusers");
                    rootElement.appendChild(listusersElement);
                    for (User user : users) {
                        Element userElement = ans.createElement("user");
                        Element nameElement = ans.createElement("name");
                        nameElement.setTextContent(user.getNickname());
                        Element typeElement = ans.createElement("type");
                        typeElement.setTextContent("" + user.getID());
                        userElement.appendChild(nameElement);
                        userElement.appendChild(typeElement);
                        listusersElement.appendChild(userElement);
                    }
                    stringWriter.getBuffer().setLength(0);
                    writer.write(ans, lsOutput);
                    String ansS = stringWriter.toString();
                    tcpConnection.sendData(ansS);
                }
                case "message" -> {
                    Element msgElem = (Element) reqv.getElementsByTagName("message").item(0);
                    Element sessionElem = (Element) reqv.getElementsByTagName("session").item(0);
                    if (msgElem == null || sessionElem == null) {
                        return;
                    }
                    ID = Integer.parseInt(sessionElem.getTextContent());
                    if (checkIDAndSendIfWrong(tcpConnection, ID, "message", "wrong session ID for send message of users")) {
                        return;
                    }
                    Document BCMessage = builder.newDocument();
                    Element rootElement = BCMessage.createElement("event");
                    rootElement.setAttribute("name", "message");
                    BCMessage.appendChild(rootElement);
                    Element messageElement = BCMessage.createElement("message");
                    messageElement.setTextContent(msgElem.getTextContent());
                    Element nameElement = BCMessage.createElement("name");
                    String senderName = "";
                    for (User user : users) {
                        if (user.getID() == ID) {
                            senderName = user.getNickname();
                            break;
                        }
                    }
                    nameElement.setTextContent(senderName);
                    rootElement.appendChild(messageElement);
                    rootElement.appendChild(nameElement);
                    stringWriter.getBuffer().setLength(0);
                    writer.write(BCMessage, lsOutput);
                    String msg = stringWriter.toString();
                    if(messagesHistory.size() == maxHistoryLen){
                        messagesHistory.remove(0);
                    }
                    messagesHistory.add(msg);
                    broadCastMessage(msg);
                    sendSuccess(tcpConnection, "message");
                }
            }

        } catch (IOException | SAXException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnectionSerializable) {
        String name = null;
        for(User user : users){
            if (tcpConnectionSerializable == user.getConnection()){
                name = user.getNickname();
                users.remove(user);
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
        broadCastMessage(stringWriter.toString());

        System.out.println("User disconected");
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnectionSerializable, Exception ex) {

    }

    @Override
    public void onRegistration(TCPConnection tcpConnection) throws IOException, ClassNotFoundException {
        try {
            String str = (String) tcpConnection.receiveData();
            System.out.println(str);
            Document answer = builder.parse(new InputSource(new StringReader(str)));
            System.out.println("Got data");
            Element ansElement = (Element) answer.getElementsByTagName("command").item(0);
            String name = ansElement.getAttribute("name");
            if (!name.equals("login")) {
                throw new SocketException("Bad input");
            }

            String userName = null;

            // ansElement = (Element) answer.getElementsByTagName("name").item(0);

            if (ansElement != null) {
                NodeList nodeList = ansElement.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        userName = nodeList.item(i).getTextContent();
                        break;
                    }
                }
                Document doc = builder.newDocument();

                for (User user : users) {
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
                        tcpConnection.sendData(ans);
                        tcpConnection.disconnect();
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
                tcpConnection.sendData(stringWriter.toString());
                users.add(new User(tcpConnection, userName, userID));
                return;
            }

            throw new SocketException("Error during receiving message");
        } catch (SAXException exception) {
            exception.printStackTrace();
        }
    }
}
