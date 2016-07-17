package IO;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import Users.User;

public class OpenTileServer {
	
	private ServerSocket server;
	public static ArrayList<User> users;
	
	public static String tmxData;
	public static BufferedImage tileSet;
	public static Document tmxDoc;
	private static int tilesetWidth;
	private static int tilesetHeight;
	private String mapData;
	
	private static String[] mapDataArray;
	
	public static String map2;
	
	public static String payload; 
	
	public static int numMapChanges = 0;
	
	public OpenTileServer(int port) {
		payload = "";
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		users = new ArrayList<User>();
		
		initFiles();
		
		while(true) {
			try {
				users.add(new User(server.accept()));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void initFiles() {
		BufferedReader br = null;
		tmxData = "";
		try {
			br = new BufferedReader(new FileReader(Main.inputFile));
			String line = "";
		    while ((line = br.readLine()) != null) {
		    	tmxData += (line) + "\n";
		    }
		    tileSet = ImageIO.read(new File(Main.inputTileset));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
	    DocumentBuilder builder;  
	    try  
	    {  
	        builder = factory.newDocumentBuilder();  
	        tmxDoc = builder.parse( new InputSource( new StringReader( tmxData ) ) );  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    } 
	    tilesetWidth = Integer.parseInt(tmxDoc.getElementsByTagName("layer").item(0).getAttributes().getNamedItem("width").getTextContent());
		tilesetHeight = Integer.parseInt(tmxDoc.getElementsByTagName("layer").item(0).getAttributes().getNamedItem("height").getTextContent());
		mapData = tmxDoc.getElementsByTagName("data").item(0).getTextContent(); 
		mapDataArray = mapData.replace("\n","").split(",");
		map2 = mapData;
	}
	
	public static void changeMap(int x, int y, int tileID) {
		appendPayload("{" +
						"protocol: 'update', " +
						"mapchange: {" +
								"x: " + x + ", " +
								"y: " + y + ", " +
								"tileID:" + tileID + 
							"}" +
					  "}");
		broadcast();
		int pos = (((y)*tilesetWidth) + x);
		int counter = 0;
		String newMapData = "";
		if(pos < mapDataArray.length && pos > -1) {
			mapDataArray[pos] = Integer.toString(tileID);
			for(int i = 0; i < tilesetHeight; i++) {
				for(int j = 0; j < tilesetWidth; j++) {
					newMapData += mapDataArray[((i)*tilesetWidth) + j] + ",";
				}
				newMapData += '\n';
			}
		}
		tmxDoc.getElementsByTagName("data").item(0).setTextContent(newMapData);
		
		numMapChanges++;
		if(numMapChanges >= Main.saveFrequency) {
			snapshot();
			numMapChanges = 0;
		}
	}
	
	private static void appendPayload(String payload1) {
		payload += payload1; 
	}
	
	public static void broadcast() {
		for(User user: users) {
			user.setPayload(payload);
		}
		payload = "";
	}

	public int getTilesetWidth() {
		return tilesetWidth;
	}

	public int getTilesetHeight() {
		return tilesetHeight;
	}
	
	public static String getTMXDocString() {
		TransformerFactory tf = TransformerFactory.newInstance();
		String result = "";
		try {
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(tmxDoc), new StreamResult(writer));
			result = writer.getBuffer().toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void snapshot() {
		Transformer tf = null;
		try {
			tf = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException
				| TransformerFactoryConfigurationError e1) {
			e1.printStackTrace();
		}
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		tf.setOutputProperty(OutputKeys.METHOD, "xml");
		tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		DOMSource domSource = new DOMSource(tmxDoc);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date =  new Date();
		try {
			new File(Main.snapshotDirectory + File.separator + "(snapshot)" + dateFormat.format(date) +".tmx").createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		StreamResult sr = new StreamResult(new File(Main.snapshotDirectory + "/(snapshot)" + dateFormat.format(date) +".tmx"));
		try {
			tf.transform(domSource, sr);
		} catch (TransformerException e1) {
			e1.printStackTrace();
		} 
	}

	
	
}
