package Users;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.json.JSONObject;
import org.json.JSONException;

import IO.OpenTileServer;

public class User {
	
	private Socket client;
	
	private final Thread inputThread;
	private InputStream input;
	
	private Thread outputThread;
	private OutputStream output;
	private float posX, posY;
	
	private String payload; 
	
	public User(Socket socket) {
		payload = "";
		client = socket;
		try {
			input = client.getInputStream();
			output = client.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		inputThread = new Thread() {
			String inputLine;
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			DataOutputStream out = new DataOutputStream(output);
			@Override
			public void run() {
				System.out.println("client running");
				while(true) {
					try {
						inputLine = reader.readLine();
						System.out.println(inputLine);
						if(inputLine.contains("req:map")) {
							out.writeBytes(OpenTileServer.getTMXDocString() + "\n");
							out.writeBytes("EOF \n");
							out.flush(); 
						} else if(inputLine.contains("req:tileset")){
							ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
							ImageIO.write(OpenTileServer.tileSet, "png", byteOut);
							byte[] size = ByteBuffer.allocate(4).putInt(byteOut.size()).array();
							out.write(size);
							out.write(byteOut.toByteArray());
							out.flush(); 
						} else if(inputLine.contains("update")) { 
							/* Example
							 * {
							 *   protocol: 'update',
							 *   posX: 0,
							 *   posY: 0,
							 *   mapchange: {
							 *     x: 0,
							 *     y: 0,
							 *     tileID: 0
							 *   }
							 * }
							 */ 

							JSONObject data = new JSONObject(inputLine);
							
							try {
								posX = (float)data.getDouble("posX");
								posY = (float)data.getDouble("posY");
								if(!data.isNull("mapchange")) {
									JSONObject mapchange = data.getJSONObject("mapchange");
									int x = mapchange.getInt("x");
									int y = mapchange.getInt("y");
									int tileID = mapchange.getInt("tileID");
									OpenTileServer.changeMap(x, y, tileID);
								}
							} catch(JSONException e) {
								System.out.println("Malform json obejct: \n" + e.getMessage());
								e.printStackTrace();
							}
							out.writeBytes(payload + '\n');
							out.flush();
							payload = "";
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}; 
		
		inputThread.start();
	}
	
	public void setPayload(String payload) {
		this.payload = payload;
	}

}
