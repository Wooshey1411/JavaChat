package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml;

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
import ru.nsu.vorobev.chat.client.model.protocolrealisation.Connection;
import ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory.Context;
import ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory.Factory;
import ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory.Operable;
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
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class XMLProtocol implements TCPConnectionListener, Connection {

    private final Model model;

    private final DocumentBuilder builder;

    final DOMImplementationRegistry registry;
    final DOMImplementationLS impl;
    final LSSerializer writer;
    LSOutput lsOutput;
    StringWriter stringWriter = new StringWriter();
    private Context context;

    private final Factory operatorsFactory;


    public XMLProtocol(Model model) {
        this.model = model;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        operatorsFactory = new Factory();
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
        context = new Context(model,connection);
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
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("command");
        rootElement.setAttribute("name","message");
        doc.appendChild(rootElement);
        Element messageElem = doc.createElement("message");
        messageElem.setTextContent(msg);
        Element sessionElem = doc.createElement("session");
        sessionElem.setTextContent("" + model.getID());
        rootElement.appendChild(messageElem);
        rootElement.appendChild(sessionElem);
        stringWriter.getBuffer().setLength(0);
        writer.write(doc,lsOutput);
        connection.sendData(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void usersListRequest() {
        Document doc = builder.newDocument();

        Element rootElement = doc.createElement("command");
        rootElement.setAttribute("name","list");
        doc.appendChild(rootElement);

        Element session = doc.createElement("session");
        session.setTextContent("" + model.getID());
        rootElement.appendChild(session);
        stringWriter.getBuffer().setLength(0);
        writer.write(doc,lsOutput);
        connection.sendData(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void disconnectRequest() {
        Document doc = builder.newDocument();

        Element rootElement = doc.createElement("command");
        rootElement.setAttribute("name","logout");
        doc.appendChild(rootElement);

        Element session = doc.createElement("session");
        session.setTextContent("" + model.getID());
        rootElement.appendChild(session);
        stringWriter.getBuffer().setLength(0);
        writer.write(doc,lsOutput);
        connection.sendData(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void onReceiveData(TCPConnection tcpConnection, byte[] o) {
        try {
            String r = new String(o, StandardCharsets.UTF_8);
            Document reqv = builder.parse(new InputSource(new StringReader(r)));
            Element reqvElement = (Element) reqv.getElementsByTagName("success").item(0);
            context.setDoc(reqv);
            if(reqvElement != null){
                String name = reqvElement.getAttribute("name");
                Operable operator = operatorsFactory.getOperator(name + "success");
                operator.doOperation(context);
                return;
            }

            reqvElement = (Element) reqv.getElementsByTagName("error").item(0);
            if(reqvElement != null) {
                String name = reqvElement.getAttribute("name");
                Operable operator = operatorsFactory.getOperator(name + "error");
                operator.doOperation(context);
                return;
            }

            reqvElement = (Element) reqv.getElementsByTagName("event").item(0);
            if(reqvElement != null){
                String name = reqvElement.getAttribute("name");
                Operable operator = operatorsFactory.getOperator(name + "event");
                operator.doOperation(context);
            }

        } catch (IOException | SAXException ex){
            ex.printStackTrace();
        }
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
        type.setTextContent("REGISTRATION");
        rootElement.appendChild(type);
        stringWriter.getBuffer().setLength(0);
        writer.write(doc,lsOutput);
        tcpConnection.sendData(stringWriter.toString().getBytes());

        try {
            byte[] in = tcpConnection.receiveData();
            Document answer = builder.parse(new InputSource(new StringReader(new String(in, StandardCharsets.UTF_8))));
            Element ansElement = (Element) answer.getElementsByTagName("error").item(0);
            if(ansElement != null){
                NodeList nodeList = ansElement.getChildNodes();
                for (int i = 0; i <nodeList.getLength(); i++){
                    if(nodeList.item(i).getNodeType() == Node.ELEMENT_NODE){
                        model.setError(nodeList.item(i).getTextContent());
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
        } catch (SAXException | NullPointerException exception){
            model.setMsg("Wrong protocol");
            tcpConnection.disconnect();
            throw new ProtocolException("Wrong protocol");
        }
    }

}
