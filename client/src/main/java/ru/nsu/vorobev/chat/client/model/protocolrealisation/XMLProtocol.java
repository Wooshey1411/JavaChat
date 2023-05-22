package ru.nsu.vorobev.chat.client.model.protocolrealisation;

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
import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionByte;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionListener;
import ru.nsu.vorobev.chat.network.connection.UserWithSameName;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketException;

public class XMLProtocol implements TCPConnectionListener, Connection {

    private final Model model;

    private final DocumentBuilder builder;

    final DOMImplementationRegistry registry;
    final DOMImplementationLS impl;
    final LSSerializer writer;
    LSOutput lsOutput;
    StringWriter stringWriter = new StringWriter();



    public XMLProtocol(Model model) {
        this.model = model;
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

    private TCPConnectionByte connection;

    @Override
    public void connect() throws IOException {
        connection = new TCPConnectionByte(XMLProtocol.this, new Socket(model.getIpAddress(), model.getPort()));
    }

    @Override
    public void disconnect() {
        connection.disconnect();
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnectionSerializable) {
        model.onModelReceive("Connection ready...");
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnectionSerializable) {
        model.onModelReceive("Connection closed");
    }

    @Override
    public void onException(TCPConnection tcpConnectionSerializable, Exception ex) {
        model.onModelReceive("Connection exception " + ex);
    }


    @Override
    public void sendMsg(String msg) {

    }

    @Override
    public void usersListRequest() {

    }

    @Override
    public void onReceiveData(TCPConnection tcpConnection, Object o) {

    }

    @Override
    public void onRegistration(TCPConnection tcpConnection) throws IOException, ClassNotFoundException {
        Document doc = builder.newDocument();

        Element rootElement = doc.createElement("command");
        rootElement.setAttribute("name","login");
        doc.appendChild(rootElement);

        Element name = doc.createElement("name");
        name.setTextContent(model.getName());
        rootElement.appendChild(name);

        Element type = doc.createElement("type");
        type.setTextContent(model.getName());
        rootElement.appendChild(type);
        stringWriter.getBuffer().setLength(0);
        writer.write(doc,lsOutput);
        tcpConnection.sendData(stringWriter.toString());

        try {
            String str = (String) tcpConnection.receiveData();
            System.out.println(str);
            Document answer = builder.parse(new InputSource(new StringReader(str)));
            Element ansElement = (Element) answer.getElementsByTagName("error").item(0);
            if(ansElement != null){
                NodeList nodeList = ansElement.getChildNodes();
                for (int i = 0; i <nodeList.getLength(); i++){
                    if(nodeList.item(i).getNodeType() == Node.ELEMENT_NODE){
                        model.onModelReceive(nodeList.item(i).getTextContent());
                        throw new UserWithSameName("Exists user with same nickname");
                    }
                }
            }

            ansElement = (Element) answer.getElementsByTagName("success").item(0);
            if(ansElement != null) {
                NodeList nodeList = ansElement.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        model.setID(Integer.parseInt(nodeList.item(i).getTextContent()));
                        return;
                    }
                }
            }
            throw new SocketException("Error during receiving message");
        } catch (SAXException exception){
            exception.printStackTrace();
        }
    }

    public void close() {
        try {
            stringWriter.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
