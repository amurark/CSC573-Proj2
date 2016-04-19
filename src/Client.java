import java.io.*;
import java.net.*;
import java.util.*;
class Client {
	private static String serverHostName;
	private static int serverPort;
	private static String fileName;
	private static int windowSize;
	private static int mss;
    public static void main(String args[]) throws Exception {
    	/***********************************GET INFO FROM COMMAND LINE*******************************************/
    	serverHostName = args[0];
    	serverPort = Integer.parseInt(args[1]);
    	fileName = args[2];
    	windowSize = Integer.parseInt(args[3]);
    	mss = Integer.parseInt(args[4]);
    	/***********************************GET INFO FROM COMMAND LINE END***************************************/
    	
    	
    	/***********************************READ FROM TEXT FILE*******************************************/
    	BufferedReader br = new BufferedReader(new FileReader(fileName));
		String rfcString = "";
		try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        rfcString = sb.toString();
	    } finally {
	        br.close();
	    }
		/***********************************READ FROM TEXT FILE END*******************************************/
    	//System.out.println(rfcString);
		List<String> segmentArray = segmentationFunction(rfcString, mss);
    	Iterator<String> it = segmentArray.iterator();
//    	while(it.hasNext()) {
//			String segment = (String) it.next();
//			//System.out.println(segment);
//		}
    	

        DatagramSocket clientSocket = new DatagramSocket();//Creating a datagram.
        //Ip address of the local server, will have to change when server is hosted on EOS
        InetAddress IPAddress = InetAddress.getByName(serverHostName);
        byte[] sendData = new byte[1024];//Array of byte type.
//        byte[] receiveData = new byte[1024];
//        String sentence = inFromUser.readLine();//User Input
        
        while(it.hasNext()) {
			String segment = (String) it.next();
			System.out.println(segment);
			
			sendData = segment.getBytes();
			System.out.println(sendData);
	        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
	        clientSocket.send(sendPacket);
		}
        
     
//        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//        clientSocket.receive(receivePacket);
//        String modifiedSentence = new String(receivePacket.getData());
//        System.out.println("FROM SERVER:" + modifiedSentence);
//        clientSocket.close();
    }
    
    public static List<String> segmentationFunction(String rfcString, int mss) {
    	ByteArrayInputStream in = new ByteArrayInputStream (rfcString.getBytes());
    	byte[] buffer = new byte[mss];
    	List<String> segmentArray = new ArrayList<String>();
    	int len;
    	try {
			while ((len = in.read(buffer)) > 0) {
				String s = "";
				for (byte b : buffer) {
			        s+= ((char) b);
			    }
				segmentArray.add(s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return segmentArray;
    }
}
