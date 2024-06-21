package org.bdware.sw.server;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.util.List;

public class MLParser {
    public static void main(String[] args) {
        String xmlString = "<dp><input>abc</input><output>222</output></dp>";
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(new StringReader(xmlString));
            List<Node> nodes = document.selectNodes("//input | //output");
            for (Node node : nodes) {
                String inputText = node.getText();
                System.out.println("Extracted text: " + inputText);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
