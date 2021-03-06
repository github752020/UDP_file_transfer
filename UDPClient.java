

/**
 * Write a description of class UDPClient here.
 *
 * @author (https://systembash.com/a-simple-java-udp-server-and-udp-client/)
 * @version (a version number or a date)
 */
import java.io.*;
import java.net.*;
import java.util.*;

class UDPClient
{
   private static DatagramSocket clientSocket;
   private static InetAddress IPAddress;
   //private static byte[] receiveData = new byte[128];
   //private static byte[] sendData = new byte[128];
   private static DatagramPacket sendPacket; 
   private static DatagramPacket receivePacket;
   
   public static void pushString(String s) {
       byte[] sendData = new byte[128];
       sendData = s.getBytes();
       sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4000);
       try {
           clientSocket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
   public static String pullString() {
       String s = null;
       byte[] receiveData = new byte[128];
       receivePacket = new DatagramPacket(receiveData, receiveData.length);
       try {
           clientSocket.receive(receivePacket);
           s = new String(receivePacket.getData(), 0, receivePacket.getLength()); 
           //s = s.replace("\n", "").replace("\r", "").trim();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
       return s;
    } 
   
   private static void receiveFile(String fileName) throws Exception{
        System.out.println("prepare to receive file");     
       int ackNum = 0; 
       int seqNum = -1;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();        
                
        //while not eof
        while(seqNum != 0)
        {
            ackNum +=1;
            
            //receive file packet
            byte[] receiveData = new byte[128];
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            System.out.println("starting timeout, waiting for file packet " + ackNum);
            try {
                    // set the socket timeout for the packet acknowledgment
                    clientSocket.setSoTimeout(100);
                    clientSocket.receive(receivePacket);
                    System.out.println("received file packet");
                    //read the sequence Number   
                    seqNum = ((receiveData[0] & 0xff) << 8) + (receiveData[1] & 0xff); //byte to int????????
                    System.out.println("received sequence number is" + seqNum);

                }
                // we did not receive an ack
                catch (SocketTimeoutException e) {
                    System.out.println("Socket timed out");
                    seqNum = -1;
                }

            
            
            if ((seqNum == 0)&&(ackNum>1)) {
                System.out.println("handling eof packet");
                ackNum = 0;
                
                //send eof ack                 
                byte [] ackData = new byte[2];
                ackData[0] = (byte)((ackNum >>> 8) & 0xFF);
                ackData[1]= (byte) (ackNum & 0xFF);
                DatagramPacket ackPacket = new DatagramPacket(ackData,ackData.length, IPAddress, 4000);
                clientSocket.send(ackPacket);
                System.out.println("send eof ack "+ ackNum);
                
                //output to file
                byte[] finalBytes = baos.toByteArray();
                FileOutputStream fos = new FileOutputStream(new File(fileName));
                baos.writeTo(fos);
                fos.flush();
                fos.close();
                System.out.println(pullString());
                
            } else if (seqNum == ackNum){
                //while there is no missing packet
                byte[]fileBytes = Arrays.copyOfRange(receiveData, 2, receiveData.length);
                System.out.println("no missing packet, copy to fileBytes");
                
                //write byte array
                baos.write(fileBytes);
                
                //send ack number                 
                byte [] ackData = new byte[2];
                ackData[0] = (byte)((ackNum >>> 8) & 0xFF);
                ackData[1]= (byte) (ackNum & 0xFF);
                DatagramPacket ackPacket = new DatagramPacket(ackData,ackData.length, IPAddress, 4000);
                clientSocket.send(ackPacket);
                System.out.println("send ack "+ ackNum);
                //ackNum +=1;
            } else {
                //missing packet
                System.out.println("missing packet "+ ackNum);
                ackNum -= 1;
                byte [] ackData = new byte[2];
                ackData[0] = (byte)((ackNum >>> 8) & 0xFF);
                ackData[1]= (byte) (ackNum & 0xFF);
                DatagramPacket ackPacket = new DatagramPacket(ackData,ackData.length, IPAddress, 4000);
                clientSocket.send(ackPacket);
                System.out.println("resend ack of last packet");
            }
            
                        
        }
        
    }
               
      
   
   public static void main(String args[]) throws Exception
   {
      BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
      clientSocket = new DatagramSocket();
      IPAddress = InetAddress.getByName("localhost");

      System.out.println("Author: Quinn Chan");
      System.out.println("SID: 103053395");
      System.out.println("ENTER FIRST SENTENCE: ");
      String sentence = inFromUser.readLine();
      while (!sentence.equals("/q")){
          
          
          if (sentence.substring(0,3).equals("GET")){
              //System.out.println(sentence);
              pushString(sentence);
              String fileExist = pullString();
              System.out.println("FROM SERVER:" + fileExist);
              System.out.println(fileExist.equals("FILE FOUND                                                                                                                      "));
              if (fileExist.equals("FILE FOUND                                                                                                                      ")){
                  System.out.println("file is found");
                  pushString("START");
                  String fileName = sentence.replace("GET ","");
                  receiveFile("./client/"+fileName);
                  
               }
              System.out.println("ENTER NEXT SENTENCE: ");
              sentence = inFromUser.readLine();
              
          } else {
              pushString(sentence);
              String modifiedSentence = pullString();
              System.out.println("FROM SERVER:" + modifiedSentence);
              System.out.println("ENTER NEXT SENTENCE: ");
              sentence = inFromUser.readLine();
          }
      }
      System.out.println("BYE!");
      clientSocket.close();
   }
}   
