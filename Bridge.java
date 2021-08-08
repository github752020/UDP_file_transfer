
/**
 * Write a description of class Bridge here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Bridge
{
    // instance variables - replace the example below with your own
    private static int serverToClientPacket = 0;
    private static int clientToServerPacket = 0;
    private static DatagramSocket serverSocket; 
    private static DatagramSocket clientSocket; 
    private static boolean randomSwitch = false;

    
    public static byte[] receiveSendClient () throws Exception
    {
        //receive from client on port 4000
       
       Random ran = new Random();
        
        while(true)
       {
          byte[] receiveData = new byte[128];
          DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
          serverSocket.receive(receivePacket);
          
          InetAddress IPAddress = receivePacket.getAddress();
          int port = receivePacket.getPort();
          byte[] fromClientData = new byte[128];
          fromClientData = receivePacket.getData();
          String sentence1 = new String( receivePacket.getData(), 0, receivePacket.getLength());
          System.out.println("RECEIVED FROM CLIENT: " + sentence1);
          
          
          if (sentence1.equals("START")){
              randomSwitch = true;
              System.out.println("random mode on");
            }          
          
          byte[] toClientData = new byte[128];
          toClientData=sendReceiveServer(fromClientData);        
          DatagramPacket sendPacket = new DatagramPacket(toClientData,toClientData.length, IPAddress, port);
          
          if (randomSwitch){
              int drop = ran.nextInt(5);
              if (drop !=4) {
                //drop packet when drop = 4
                serverSocket.send(sendPacket);
                serverToClientPacket +=1;
                String sentence2 = new String(sendPacket.getData(), 0, sendPacket.getLength());
                System.out.println("SENT TO CLIENT: " + sentence2);
              } else {
                System.out.println("packet to client dropped");
              }
          }  else {
                serverSocket.send(sendPacket);
                String sentence2 = new String(sendPacket.getData(), 0, sendPacket.getLength());
                serverToClientPacket +=1;
                System.out.println("SENT TO CLIENT: " + sentence2);
            } 
          
          System.out.println("No. of packets SENT TO CLIENT: " + serverToClientPacket);
          System.out.println("No. of packets SENT TO SERVER: " + clientToServerPacket);
          
       }
        
    }
    
    public static byte[] sendReceiveServer(byte[] fromClientData) throws Exception
   {
      // send to server on port 5000
      
      InetAddress IPAddress = InetAddress.getByName("localhost");
      byte[] toServerData = new byte[128];
      byte[] toClientData = new byte[128];
      byte[] fromServerData = new byte[128];
      toServerData = fromClientData;
      DatagramPacket sendPacket = new DatagramPacket(toServerData, toServerData.length, IPAddress, 5000);
      clientSocket.send(sendPacket);
      clientToServerPacket +=1;
      TimeUnit.MICROSECONDS.sleep(1); //Limit the transmission speed
      String sentence1 = new String(sendPacket.getData(), 0, sendPacket.getLength());
      System.out.println("TO SERVER:" + sentence1);
      DatagramPacket receivePacket = new DatagramPacket(fromServerData, fromServerData.length);
      clientSocket.receive(receivePacket);
      System.out.println("got packet from server");
      
      toClientData = receivePacket.getData();
      System.out.println("got data from server");
      String sentence2 = new String(receivePacket.getData(), 0, receivePacket.getLength());
      System.out.println("FROM SERVER:" + sentence2);
      if (sentence2.equals("DONE")){
          randomSwitch = false;
          System.out.println("random mode off");
       }
      //clientSocket.close();
      return toClientData;
   }
         
    public static void main(String args[]) throws Exception
      {
          System.out.println("Author: Quinn Chan");
          System.out.println("SID: 103053395");
          serverSocket = new DatagramSocket(4000);        
          clientSocket = new DatagramSocket();
          receiveSendClient();
      
      }
}
