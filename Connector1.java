import java.io.*;
import java.net.*;
import java.util.* ;
import javax.swing.* ; 

/**
  *  A Connector1 is a server that listens for I/O connections
  *  at a port.  Each client gets a Socket and buffered 
  *  i/o streams bundled in a Transducer object. 
  *  Text read at any input is rebroadcast to other clients.
  *  <pre>
  *  =====================================
  *   Copyright 1999-- John R. Fisher 
  *   jrfisher@csupomona.edu
  *  =====================================  
  *  </pre>
  *  @author jrfisher@csupomona.edu
  */

public class Connector1 extends Thread { 
   int clientNum ; 
   int port ; 
   ServerSocket portalSocket ; 
   Vector collaborators ; // Object output streams for clients 

   public Connector1(int port) { 
      this.clientNum = 1 ; 
      this.port = port ; 
      this.collaborators = new Vector() ;
   }

   public void run() { 
      // Catch big exceptions that prevent server from continuing.
      try { // 1
         portalSocket = new ServerSocket(port) ; 
         while(true) { 
            // Catch smaller exceptions so server itself can continue..
            try { // 2
               Socket soc = portalSocket.accept() ;
               System.out.print("t");
               BufferedWriter out = 
                  new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())) ;
               collaborators.add(out) ; 
               System.out.println("Spawning Transducer for " + clientNum) ; 
               Transducer b = 
                  new Transducer(this, 
                               new BufferedReader(new InputStreamReader(soc.getInputStream())), 
                               out, 
                               clientNum) ; 
               b.start() ;
               clientNum++ ; 
            }
            catch(Exception e2) { 
               JOptionPane.showMessageDialog(null,e2.toString(),"Connector1 EXCEPTION #2",JOptionPane.WARNING_MESSAGE) ;
            }
         }
      }
      catch (Exception e1) {
         // Could not make ServerSocket
         JOptionPane.showMessageDialog(null,e1.toString(),"Connector1 EXCEPTION #1",JOptionPane.WARNING_MESSAGE) ; 
      }
   } 

   /**
     *  From the command line ...
     *     java -classpath <path> Connector1 <port#>
     */
   public static void main(String[] args) { 
      try { 
         int port = Integer.parseInt(args[0]) ;
         Connector1 prtl = new Connector1(port) ; 
         prtl.start() ; 
         //System.out.println("Starting portal on port " + prtl.portalSocket.getInetAddress() +  port) ;
      }
      catch(Exception e) { 
         System.out.println(e) ; 
         System.out.println("usage: java -classpath <> Connector1 <port>") ; 
      }
   }
}

/**
  *  A Transducer is an attachment to a particular client.
  *  The Transducer listens to this client and rebroadcasts its 
  *  contribution.
  */
class Transducer extends Thread {
   BufferedReader in ; 
   BufferedWriter out ; 
   int client ; 
   Connector1 portal ; 

   Transducer(Connector1 p, BufferedReader instream,
                    BufferedWriter outstream, int k) { 
      this.portal = p ; 
      in = instream ; 
      out = outstream ; 
      client = k ;
   }

   public void run() {
      while(true) { 
         try { 
            // if can read a good input ... 
            String s = in.readLine() ;
            System.out.println(s);
            if (s == null) {    // THIS CLIENT IS GONE ...  
               portal.collaborators.remove(out) ;   // remove this client from server 
               break ;  // stop running this client
            }
            // try to tell everyone ...
            Iterator it = portal.collaborators.iterator() ; 
            while(it.hasNext()) { 
               // Avoid bad collaborators if possible or necessary ...
               BufferedWriter bw = null ; 
               try { 
                  bw = (BufferedWriter)(it.next()) ; 
                  if (bw != out) {  // don't tell self
                     bw.write(s+"\r") ; // return for readLine at other end
                     bw.flush() ; 
                  }
               }
               catch(SocketException | ConcurrentModificationException socEx2){}
               catch (Exception e2) {
                  JOptionPane.showMessageDialog(null,e2.toString(),"TRANSDUCER EXCEPTION #2",JOptionPane.WARNING_MESSAGE) ; 
               }

            }   
         }   
         // running exception not otherwise handled ...
         catch(SocketException socEx1){}
         catch(Exception e1) {   
            JOptionPane.showMessageDialog(null,e1.toString(),"TRANSDUCER EXCEPTION #1",JOptionPane.WARNING_MESSAGE) ; 
         }
      }  
   }
}





