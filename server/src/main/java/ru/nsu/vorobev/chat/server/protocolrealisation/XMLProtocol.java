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
import ru.nsu.vorobev.chat.network.protocols.Message;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XMLProtocol implements TCPConnectionListener, Connection {

    private static int ID = 0;

    private final List<User> users = new ArrayList<>();

    private final List<Message> messagesHistory = new ArrayList<>();

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


    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnectionSerializable) {
        System.out.println("User connected");
    }

    @Override
    public synchronized void onReceiveData(TCPConnection tcpConnection, Object obj) {

        try {
            Document reqv = builder.parse(new InputSource(new StringReader((String) obj)));
            Element reqvElement = (Element) reqv.getElementsByTagName("command").item(0);
            String name = reqvElement.getAttribute("name");

            switch (name) {
                case "list":
                    NodeList nodeList = reqvElement.getChildNodes();
                    int ID = -1;
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            ID = Integer.parseInt(nodeList.item(i).getTextContent());
                            break;
                        }
                    }
                    Document ans = builder.newDocument();
                    boolean IDFound = false;
                    for (User user:users){
                        if(user.getID() == ID){
                            IDFound = true;
                            break;
                        }
                    }
                    if(!IDFound)
                        {
                            Element rootElement = ans.createElement("error");
                            ans.appendChild(rootElement);
                            Element reason = ans.createElement("reason");
                            reason.setTextContent("wrong session ID for get list of users");
                            rootElement.appendChild(reason);
                            stringWriter.getBuffer().setLength(0);
                            writer.write(ans, lsOutput);
                            tcpConnection.sendData(stringWriter.toString());
                            return;
                        }

                    Element rootElement = ans.createElement("success");
                    rootElement.setAttribute("name","list");
                    ans.appendChild(rootElement);
                    Element listusersElement = ans.createElement("listusers");
                    rootElement.appendChild(listusersElement);
                    for (User user : users){
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

        } catch (IOException | SAXException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnectionSerializable) {
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
