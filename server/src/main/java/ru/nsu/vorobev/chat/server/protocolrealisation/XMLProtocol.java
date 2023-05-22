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

public class XMLProtocol implements TCPConnectionListener,Connection {

    private static int ID = 0;

    private final List<User> serializableUsers = new ArrayList<>();

    private final List<Message> messagesHistory = new ArrayList<>();

    private final DocumentBuilder builder;

    final DOMImplementationRegistry registry;
    final DOMImplementationLS impl;
    final LSSerializer writer;
    LSOutput lsOutput;
    StringWriter stringWriter = new StringWriter();

    private final int port;

    public XMLProtocol(int port){
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
    public void start(){
        System.out.println("Server running...");
        Log.enableLogger();
        Log.init();
        Log.log(Log.getTime() + ":Server start working",Log.TypeOfLoggers.INFO);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    new TCPConnectionByte(this, serverSocket.accept());
                } catch (IOException ex) {
                    Log.log(Log.getTime() + ":TCPConnection exception:" + ex,Log.TypeOfLoggers.ERROR);
                }
                catch (UserWithSameName ignored){}

            }
        } catch (IOException ex) {
            Log.log(Log.getTime() + ":Server socket error! Server shutdown... exception:" + ex, Log.TypeOfLoggers.ERROR);
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void onConnectionReady(TCPConnection tcpConnectionSerializable) {

    }

    @Override
    public void onReceiveData(TCPConnection tcpConnectionSerializable, Object obj) {

    }

    @Override
    public void onDisconnect(TCPConnection tcpConnectionSerializable) {

    }

    @Override
    public void onException(TCPConnection tcpConnectionSerializable, Exception ex) {

    }

    @Override
    public void onRegistration(TCPConnection tcpConnection) throws IOException,ClassNotFoundException {
        try {
            String str = (String) tcpConnection.receiveData();
            System.out.println(str);
            Document answer = builder.parse(new InputSource(new StringReader(str)));
            System.out.println("Got data");
            Element ansElement = (Element) answer.getElementsByTagName("command").item(0);
            String name = ansElement.getAttribute("name");
            if(!name.equals("login")){
                throw new SocketException("Bad input");
            }

            String userName = null;

            // ansElement = (Element) answer.getElementsByTagName("name").item(0);

            if(ansElement != null) {
                NodeList nodeList = ansElement.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        userName = nodeList.item(i).getTextContent();
                        break;
                    }
                }
                Document doc = builder.newDocument();

                for (User user : serializableUsers){
                    if(Objects.equals(user.getNickname(), userName)){

                        Element rootElement = doc.createElement("error");
                        doc.appendChild(rootElement);

                        Element reason = doc.createElement("reason");
                        reason.setTextContent("Exist user with same name");
                        rootElement.appendChild(reason);
                        stringWriter.getBuffer().setLength(0);
                        writer.write(doc,lsOutput);
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

                writer.write(doc,lsOutput);
                tcpConnection.sendData(stringWriter.toString());
                serializableUsers.add(new User(tcpConnection,userName,userID));
                return;
            }

            throw new SocketException("Error during receiving message");
        } catch (SAXException exception){
            exception.printStackTrace();
        }
    }
}
