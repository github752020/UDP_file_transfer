

/**
 * Write a description of class UDPServer here.
 *
 * @author (https://systembash.com/a-simple-java-udp-server-and-udp-client/)
 * @version (a version number or a date)
 */
import java.io.*;
import java.net.*;

class UDPServer
{
   private static InetAddress IPAddress;
   private static int port;
   private static DatagramSocket serverSocket;
   //private static byte[] receiveData = new byte[128];
   //private static byte[] sendData = new byte[128];
   private static DatagramPacket sendPacket; 
   private static DatagramPacket receivePacket;
   
   public static void pushString(String s) {
       byte[] sendData = new byte[128];
       sendData = s.getBytes();
       sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
       try {
           serverSocket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
   public static String pullString() {
       String s = null;
       byte[] receiveData = new byte[128];
       receivePacket = new DatagramPacket(receiveData, receiveData.length);
       try {
           serverSocket.receive(receivePacket);
           s = new String(receivePacket.getData(), 0, receivePacket.getLength()); 
           //s = s.replace("\n", "").replace("\r", "").trim();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
       return s;
    }
    
   public static void createFile() {
       try {
           File myFile = new File("./server/text.txt");
           if (myFile.createNewFile()) {
               System.out.println("File created: " + myFile.getName());
            } else {
                System.out.println("File already exists.");
            }
       } catch (IOException e) {
           System.out.println("An error occurred.");
           e.printStackTrace();
       }
   }
   
   public static void writeToFile(String sentence) {
       try {
           FileWriter myWriter = new FileWriter("./server/text.txt", true);
           myWriter.write(sentence+"\n");
           myWriter.close();
           System.out.println("Successfully wrote to the file.");
       } catch (IOException e) {
           System.out.println("An error occurred.");
           e.printStackTrace();
       }
   }
   
   public static boolean searchDirectory(String fileName) {
       
       String path2 = "C:/Users/user/Desktop/swinburne/2nd/DCS/assign2/UDP_Chat/server/"+fileName;
       File tempFile = new File(path2);
       System.out.println("Looking for                "+tempFile.getAbsolutePath());
       boolean found = tempFile.exists();
       return found;
   }   
   
   private static void sendFile(String fileName) throws Exception{
       System.out.println("preparing to send file");
       
       //get message start
       String s = pullString();
       System.out.println(s);
       
       //read file to bytes
       String path = "C:/Users/user/Desktop/swinburne/2nd/DCS/assign2/UDP_Chat/server/"+fileName;
       File file = new File(path);
       byte[] fileBytes = new byte[(int)file.length()];
       FileInputStream fileInputStream = new FileInputStream(file);
       fileInputStream.read(fileBytes);
       fileInputStream.close();
       System.out.println("finish reading file");
       
       boolean eof = false;
       int seqNum = 0;
       int chunk = 126;
       
       //break fileBytes into chunks
       byte[] buffer = new byte [128];
       for (int i = 0; i<fileBytes.length; i+=chunk){
                   
           //set the sequence number to two bytes
           seqNum += 1;
           buffer[0] = (byte)((seqNum >>> 8) & 0xFF);
           buffer[1]= (byte) (seqNum & 0xFF);           
           System.out.println(((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff));
                      
           //construct the packet
           if ((i + chunk) < fileBytes.length){
               System.arraycopy(fileBytes, i, buffer, 2, chunk);  
               System.out.println("copy fileBytes to buffer " + i);
            } else {
               chunk = fileBytes.length - i;
               System.arraycopy(fileBytes, i, buffer, 2, chunk);
               eof = true;
               System.out.println("last packet");
            }
           
           //send packet
           sendPacket = new DatagramPacket(buffer, buffer.length, IPAddress, port);
           serverSocket.send(sendPacket);
           System.out.println("Sent file packet "+ seqNum);
           
           //wait for ack
           byte[] receiveData = new byte[2];
           receivePacket = new DatagramPacket(receiveData, receiveData.length);
           System.out.println("waiting for ack");
           
           //check ack number
           serverSocket.receive(receivePacket);
           System.out.println("got ack packet");
           int ackNum = ((receiveData[0] & 0xff) << 8) + (receiveData[1] & 0xff); //byte to int
           System.out.println("received ack is" + ackNum);
           if (ackNum != seqNum) {
               //incorrectly acknowledged = NACK, resend the same packet in next loop
               System.out.println("ack not right, ack is " + ackNum + "seq is "+ seqNum);
               i -= chunk;
               seqNum -=1;               
            }
           
           //send EOF packet if end-of-file
           if ((eof)&&(ackNum == seqNum)){
               seqNum = 0;
               byte [] eofData = new byte[2]; // int to byte array
               eofData[0] = (byte)((seqNum >>> 8) & 0xFF);
               eofData[1]= (byte) (seqNum & 0xFF);
               sendPacket = new DatagramPacket(eofData, eofData.length, IPAddress, port);
               serverSocket.send(sendPacket);
               System.out.println("sent eof");
               
               //wait for ack
               receiveData = new byte[2];
               receivePacket = new DatagramPacket(receiveData, receiveData.length);
               System.out.println("waiting for ack");
               
               //check ack number
               serverSocket.receive(receivePacket);
               System.out.println("got ack packet");
               ackNum = ((receiveData[0] & 0xff) << 8) + (receiveData[1] & 0xff); //byte to int
               System.out.println("received ack is" + ackNum);
               while (ackNum != seqNum){
                   System.out.println("eof ack is not right");
                   serverSocket.send(sendPacket);
                   System.out.println("sent eof again");
                   
                   //wait for ack eof
                   serverSocket.receive(receivePacket);
                   ackNum = ((receiveData[0] & 0xff) << 8) + (receiveData[1] & 0xff);
                   System.out.println("received ack is" + ackNum);
                }
               if (ackNum == seqNum) {pushString("DONE");}
            }
        } 
               
    }
   
   public static void main(String args[]) throws Exception
      {
            serverSocket = new DatagramSocket(5000);
            
            System.out.println("Author: Quinn Chan");
            System.out.println("SID: 103053395");
            createFile();
            while(true)
               {
                  String sentence = pullString();
                  if (!sentence.equals("/q")) {
                      System.out.println("RECEIVED: " + sentence);
                      IPAddress = receivePacket.getAddress();
                      port = receivePacket.getPort();
                      System.out.println("received from port" + port);
                      
                      //if receive a file name
                      try {
                          if (sentence.substring(0,3).equals("GET")) {
                              String fileName = sentence.replace("GET ","");
                              fileName = fileName.replace("\n", "").replace("\r", "").trim();
                              if (searchDirectory(fileName)){
                                  pushString("FILE FOUND");
                                  sendFile(fileName);
                              }else{
                                  pushString("FILE NOT FOUND");
                              }
                              
                          }else {
                              
                              //if receive a sentence
                              String capitalizedSentence = sentence.toUpperCase();
                              pushString(capitalizedSentence);                     
                              writeToFile(sentence);                    
                            
                           }
                        } catch (Exception e){
                            System.out.println("Something went wrong");
                            e.printStackTrace();
                        }
                      
                      
                  }
            }
      }
}