import java.io.*;
import java.net.*;

class Server {
	private static final int portNum = 7735;
    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(portNum);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        System.out.println("Server listening on port: "+serverSocket.getLocalPort());
        while (true) {
        	
        	
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String sentence = new String(receivePacket.getData());
            System.out.println(sentence);
//            System.out.println("RECEIVED: " + sentence);
//            InetAddress IPAddress = receivePacket.getAddress();
//            String capitalizedSentence = sentence.toUpperCase();
//            sendData = capitalizedSentence.getBytes();
//            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNum);
//            serverSocket.send(sendPacket);
            //break;//Extra line to break out of the loop once the request is processed.
        }
        //serverSocket.close();
    }
}
