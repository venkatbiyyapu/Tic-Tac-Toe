import java.awt.* ;
import java.awt.event.* ;
import java.io.* ;
import java.net.* ;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.* ;
import javax.swing.border.* ;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ttt1 extends JFrame
        implements ActionListener {
   JButton b11,b21,b31,
           b12,b22,b32,
           b13,b23,b33,
           newGame, nGames, stopReset,changePlayer;
   JRadioButton humanRadioButton,computerRadioButton;
   FileHandler fh;
   String winner="";
   Boolean game_progress= false;
   boolean myturn ;
   BufferedReader br,br2 ;
   BufferedWriter bw,bw2 ;
   Thread connection,connection2 ;
   Process prologProcess,prologProcess2 ;
   String prolog ;
   String ttt,tttx;
   String player1="Human";
   JLabel p1Label, p2Label,xWinCounter,oWinCounter,tieCounter,totGamesCounter;

   Logger logger;
   AtomicInteger gameCount;

   Integer xWins=0,oWins=0,ties=0,totGamesPlayed=0;

   /**
    *  Create a tic tac toe game,
    *  prolog is the prolog command (e.g. "/opt/local/bin/swipl").
    *  ttt is the locator for ttt.pl (e.g. "/javalib/TicTacToe/ttt.pl").
    */
   public ttt1(String prolog, String ttt) {
      this.prolog = prolog ;
      this.ttt = ttt ;
      this.tttx =ttt.replace(".pl","x.pl");
      logger = Logger.getLogger(ttt1.class.getName());

      try {
         fh = new FileHandler("ttt-java-log.txt");
         fh.setFormatter(new SimpleFormatter());
         logger.addHandler(fh);
      } catch (Exception e) {
         e.printStackTrace();
      }
      b11 = new JButton("") ;
      b21 = new JButton("") ;
      b31 = new JButton("") ;
      b12 = new JButton("") ;
      b22 = new JButton("") ;
      b32 = new JButton("") ;
      b13 = new JButton("") ;
      b23 = new JButton("") ;
      b33 = new JButton("") ;
      b11.setActionCommand("(1,1).") ;
      b21.setActionCommand("(2,1).") ;
      b31.setActionCommand("(3,1).") ;
      b12.setActionCommand("(1,2).") ;
      b22.setActionCommand("(2,2).") ;
      b32.setActionCommand("(3,2).") ;
      b13.setActionCommand("(1,3).") ;
      b23.setActionCommand("(2,3).") ;
      b33.setActionCommand("(3,3).") ;
      Font f = new Font("monospaced",Font.PLAIN,64) ;
      JButton[] buttons = { b11, b21, b31, b12, b22, b32, b13, b23, b33 };
      for (JButton button : buttons) {
         button.setFont(f);
         button.addActionListener(this);
      }

      newGame = new JButton("New Game");
      nGames=  new JButton("N Games");
      stopReset= new JButton("Stop/Reset");
      newGame.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            startNewGame();
         }

      });
      nGames.addActionListener(new nGamesListener());

      stopReset.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            stopResetGame();
         }
      });

      JPanel subPanel = new JPanel();
      subPanel.add(newGame);
      subPanel.add(nGames);
      subPanel.add(stopReset);
      this.getContentPane().add(subPanel,BorderLayout.PAGE_START);
      JPanel panel = new JPanel() ;
      panel.setLayout(new GridLayout(3,3)) ;
      for (JButton button : buttons) {
         panel.add(button);
      }

      this.setTitle("Tic Tac Toe-VXB220005") ;
      Border panelborder = BorderFactory.createLoweredBevelBorder() ;
      panel.setBorder(panelborder) ;
      this.getContentPane().add(panel) ;

      JPanel gameRecordDisplay =  new JPanel();
      gameRecordDisplay.setLayout(new GridLayout(4, 1));

      JLabel xWinLabel = new JLabel("  X Wins:  ");
      JLabel oWinLabel = new JLabel("  O Wins:  ");
      JLabel tieLabel = new JLabel("  Ties:   ");
      JLabel totGamesLabel = new JLabel(" #Games: ");

      xWinCounter = new JLabel(Integer.toString(xWins));
      oWinCounter= new JLabel(Integer.toString(oWins));
      tieCounter = new JLabel(Integer.toString(ties));
      totGamesCounter = new JLabel(Integer.toString(totGamesPlayed));

      gameRecordDisplay.add(xWinLabel);
      gameRecordDisplay.add(xWinCounter);

      gameRecordDisplay.add(oWinLabel);
      gameRecordDisplay.add(oWinCounter);

      gameRecordDisplay.add(tieLabel);
      gameRecordDisplay.add(tieCounter);

      gameRecordDisplay.add(totGamesLabel);
      gameRecordDisplay.add(totGamesCounter);

      this.getContentPane().add(gameRecordDisplay,BorderLayout.EAST);

      changePlayer = new JButton("Change Player1");
      changePlayer.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            humanRadioButton = new JRadioButton("Human");
            computerRadioButton = new JRadioButton("Computer");
            ButtonGroup group = new ButtonGroup();
            group.add(humanRadioButton);
            group.add(computerRadioButton);
            JPanel panel = new JPanel();
            panel.add(new JLabel("Select Player Type:"));
            panel.add(humanRadioButton);
            panel.add(computerRadioButton);
            int input = JOptionPane.showConfirmDialog(null, panel, "Player Type Selection", JOptionPane.OK_CANCEL_OPTION);
            if (input == JOptionPane.OK_OPTION) {
               if (humanRadioButton.isSelected()) {
                  player1 = "Human";
               } else if (computerRadioButton.isSelected()) {
                  player1 = "Computer";
               }
               p1Label.setText("Player1: " + player1);
            }
         }
      });

      JPanel playerDisplay =  new JPanel();
      playerDisplay.setLayout(new GridLayout(1, 3));

      p1Label = new JLabel("Player1: "+player1);
      p2Label = new JLabel("\t\tPlayer2: Computer");

      playerDisplay.add(p1Label);
      playerDisplay.add(changePlayer);
      playerDisplay.add(p2Label);
      playerDisplay.setSize(300,500);
      this.getContentPane().add(playerDisplay,BorderLayout.PAGE_END);
      this.setSize(450,450);
      this.setLocation(900,300) ;
//      this.myturn = true ;

      Connector1 connector = new Connector1(54323) ;
      connector.start() ;

      Socket sock ;
      try {
         sock = new Socket("127.0.0.1",54323) ;
         br = new BufferedReader(new InputStreamReader(sock.getInputStream())) ;
         bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())) ;
      } catch(Exception x) { x.printStackTrace() ; }

      connection = new Thread() {
         public void run() {
            while(true) {
               try{
                  String s = br.readLine() ;
                  computer_move_o(s) ;
               } catch(Exception xx) { xx.printStackTrace() ; }
            }
         }
      } ;
      connection.start() ;

      Connector1 connector2 = new Connector1(54322) ;
      connector2.start() ;

      Socket sock2 ;
      try {
         sock2 = new Socket("127.0.0.1",54322) ;
         br2 = new BufferedReader(new InputStreamReader(sock2.getInputStream())) ;
         bw2 = new BufferedWriter(new OutputStreamWriter(sock2.getOutputStream())) ;
      } catch(Exception x) { x.printStackTrace() ; }

      System.out.println(br2.toString());

      connection2 = new Thread() {
         public void run() {
            while(true) {
               try{
                  String s = br2.readLine() ;
                  System.out.println(s);
                  computer_move_x(s) ;
               } catch(Exception xx) { xx.printStackTrace() ; }
            }
         }
      } ;
      connection2.start() ;

      Thread shows = new Thread() {
         public void run() {
            setVisible(true) ;
         }
      } ;
      EventQueue.invokeLater(shows);

//       Start the prolog player

//      try {
//         prologProcess =
//           Runtime.getRuntime().exec(prolog + " -f " + ttt) ;
//      } catch(Exception xx) {System.out.println(xx) ; }
//      game_progress = true;

      // On closing, kill the prolog process first and then exit
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent w) {
            destroy_prolog();
            System.exit(0) ;
         }
      }) ;

   }

   public static void main(String[] args) {
      String ttt = "ttt.pl" ;
      String prolog="/opt/homebrew/bin/swipl";
      boolean noargs = true ;
      try {
         prolog = args[0] ;
         ttt = args[1] ;
         noargs = false ;
      }
      catch (Exception xx) {
         System.out.println("usage: java TicTactoe  <where prolog>  <where ttt>") ;
      }
      if (noargs) {
         Object[] message = new Object[4] ;
         message[0] = new Label("  prolog command") ;
         message[1] = new JTextField(prolog) ;
         message[2] = new Label("  where ttt.pl ") ;
         message[3] = new JTextField(ttt) ;
         try {
            int I = JOptionPane.showConfirmDialog(null,message,"Where are Prolog and ttt.pl? ",JOptionPane.OK_CANCEL_OPTION) ;
            if (I == 2 | I == 1) System.exit(0) ;
            System.out.println(I) ;
            new ttt1(((JTextField)message[1]).getText().trim(),((JTextField)message[3]).getText().trim()) ;
         } catch(Exception yy) {}
      }
      else
         new ttt1(prolog,ttt) ;
   }
   synchronized void destroy_prolog(){
      if (prologProcess != null) prologProcess.destroy() ;
      if (prologProcess2 != null) prologProcess2.destroy() ;
      game_progress = false;
   }
   private void clearGrid() {
      JButton[] buttons = { b11, b21, b31, b12, b22, b32, b13, b23, b33 };
      for (JButton button : buttons) {
         button.setText("");
         button.setBackground(null);
         button.setOpaque(true);
         button.setBorderPainted(true);
      }
   }
   class nGamesListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         int input = Integer.parseInt(JOptionPane.showInputDialog("Enter n value between 1-10"));
         resetNGames();
         if (input>=1 && input<=10) {
            SwingWorker<Void, Void> worker=new SwingWorker() {
               @Override
               protected Void doInBackground() throws Exception {
                  gameCount = new AtomicInteger(input);
                  while (gameCount.get() > 0) {
                     synchronized (nGamesListener.class) {
                        if (!game_progress) {
                           startNewGame();
                           waitUntilGameFinished();
                           gameCount.decrementAndGet();
                        }
                     }
                  }
                  return null;
               }

               @Override
               protected void done() {
                  String overAllWinner="";
                  if(xWins>oWins) {
                     overAllWinner="Player1(X)";
                     JOptionPane.showMessageDialog(null, "Player1(X) is the series Winner!!", input + " Games are Over", JOptionPane.INFORMATION_MESSAGE);
                  }
                  else if(xWins<oWins) {
                     overAllWinner="Player2(O)";
                     JOptionPane.showMessageDialog(null, "Player2(0) is the series Winner!!", input + " Games are Over", JOptionPane.INFORMATION_MESSAGE);
                  }
                  else {
                     overAllWinner="No One";
                     JOptionPane.showMessageDialog(null, "No Winner", input + " Games are over", JOptionPane.INFORMATION_MESSAGE);
                  }

                  logger.info("Total X-Wins : " + xWins);
                  logger.info("Total O-Wins : " + oWins);
                  logger.info("Total Ties : " + ties);
                  logger.info("Overall Winner is " + overAllWinner);
               }
            };

            worker.execute();
         } else {
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);

         }
      }

      private void waitUntilGameFinished() {
         synchronized (nGamesListener.class) {
            while (game_progress) {
               try {
                  nGamesListener.class.wait();
                  System.out.println("Waiting is done");
               } catch (InterruptedException ex) {
                  ex.printStackTrace();
               }
            }
         }
      }
   }

   synchronized private void startNewGame(){
      logger.info("Game "+totGamesPlayed+1+" Started");
      clearGrid();
      if (game_progress)
         destroy_prolog();
      game_progress = true;
      myturn=true;
      try {
         prologProcess =
                 Runtime.getRuntime().exec(prolog + " -f " + ttt);
         Thread.sleep(200);
      } catch (Exception xx) {
         System.out.println(xx);
      }
      try {
         prologProcess2 =
                 Runtime.getRuntime().exec(prolog + " -f " + tttx) ;
      } catch(Exception xx) {xx.printStackTrace() ; }
   }

synchronized void computer_move_x(String s) { // " x ## y '
    if(!player1.equals("Computer")){
       return;
    }
   String[] c = s.split(",") ;
   String buttonMove = null;
   int x = Integer.parseInt(c[0].trim()),
           y = Integer.parseInt(c[1].trim()) ;
   //System.out.println(x+","+y) ;
   if(player1.equals("Computer")){
      if (x == 1) {
         if (y == 1) {
            b11.setText("X");
            buttonMove = b11.getActionCommand();
         }
         else if (y == 2) {
            b12.setText("X") ;
            buttonMove = b12.getActionCommand();
         }
         else if (y == 3) {
            b13.setText("X") ;
            buttonMove = b13.getActionCommand();
         }
      }
      else if (x == 2) {
         if (y == 1) {
            b21.setText("X") ;
            buttonMove = b21.getActionCommand();
         }
         else if (y == 2) {
            b22.setText("X") ;
            buttonMove = b22.getActionCommand();
         }
         else if (y == 3) {
            b23.setText("X") ;
            buttonMove = b23.getActionCommand();
         }
      }
      else if (x == 3) {
         if (y == 1) {
            b31.setText("X") ;
            buttonMove = b31.getActionCommand();
         }
         else if (y == 2) {
            b32.setText("X") ;
            buttonMove = b32.getActionCommand();
         }
         else if (y == 3) {
            b33.setText("X") ;
            buttonMove = b33.getActionCommand();
         }
      }
      try {
         logger.info("X placed at "+buttonMove);
         bw.write(buttonMove + "\n") ;
         bw.flush() ;
         Thread.sleep(300);
      } catch(Exception xx) { xx.printStackTrace() ; }
   }
   System.out.println("Computer player: "+s+"\n");
   if (winner()) stopResetGame() ;
   else if (tie()) stopResetGame();
   else  myturn = false ;
}

   synchronized void computer_move_o(String s) { // " x ## y '
      logger.info("O placed at "+ s);
      String[] c = s.split(",") ;
      String buttonMove = null;
      int x = Integer.parseInt(c[0].trim()),
              y = Integer.parseInt(c[1].trim()) ;
      //System.out.println(x+","+y) ;
      if (x == 1) {
         if (y == 1) {
            b11.setText("O");
            buttonMove = b11.getActionCommand();
         }
         else if (y == 2) {
            b12.setText("O") ;
            buttonMove = b12.getActionCommand();
         }
         else if (y == 3) {
            b13.setText("O") ;
            buttonMove = b13.getActionCommand();
         }
      }
      else if (x == 2) {
         if (y == 1) {
            b21.setText("O") ;
            buttonMove = b21.getActionCommand();
         }
         else if (y == 2) {
            b22.setText("O") ;
            buttonMove = b22.getActionCommand();
         }
         else if (y == 3) {
            b23.setText("O") ;
            buttonMove = b23.getActionCommand();
         }
      }
      else if (x == 3) {
         if (y == 1) {
            b31.setText("O") ;
            buttonMove = b31.getActionCommand();
         }
         else if (y == 2) {
            b32.setText("O") ;
            buttonMove = b32.getActionCommand();
         }
         else if (y == 3) {
            b33.setText("O") ;
            buttonMove = b33.getActionCommand();
         }
      }
      if(player1.equals("Computer")){
         try {
            logger.info("X placed at "+buttonMove);
            bw2.write(buttonMove + "\n") ;
            bw2.flush() ;
            Thread.sleep(200);
         } catch(Exception xx) { xx.printStackTrace() ; }
      }
      if (winner()) stopResetGame() ;
      else if (tie()) stopResetGame();
      else  myturn = true ;
   }


   synchronized public void actionPerformed(ActionEvent act) {
      if (!myturn|| player1.equals("Computer")) return ;
      String s = ((JButton)act.getSource()).getText() ;
      if (!s.equals("")) return  ;
      ((JButton)(act.getSource())).setText("X") ;
      logger.info("X's move "+act.getActionCommand());
      try {
         bw.write(act.getActionCommand() + "\n") ;
         bw.flush() ;
      } catch(Exception xx) { System.out.println(xx) ; }

      if(winner())
         stopResetGame();
      else if(tie())
         stopResetGame();
      myturn = false ;
   }

   /**
    *  Do we have a winner?
    */
   synchronized boolean winner() {
      return  line(b11,b21,b31) ||
              line(b12,b22,b32) ||
              line(b13,b23,b33) ||
              line(b11,b12,b13) ||
              line(b21,b22,b23) ||
              line(b31,b32,b33) ||
              line(b11,b22,b33) ||
              line(b13,b22,b31) ;
   }
   synchronized private boolean tie() {
      JButton[] buttons = { b11, b21, b31, b12, b22, b32, b13, b23, b33 };
      for (JButton button : buttons) {
         if (button.getText().equals(""))
            return false;
      }
      winner="D";
      return true;
   }
   synchronized private void setBgColors(JButton[] buttonsList, Color bg){
      for (JButton button : buttonsList) {
         button.setBackground(bg);
         button.setOpaque(true);
      }
   }

   /**
    *  Are three buttons marked with same player?
    *  If, so color the line and return true.
    */
   synchronized private void stopResetGame() {
      if(game_progress)
         destroy_prolog();
      if(winner.equals("")){
         JOptionPane.showMessageDialog(null, "Game Stopped", "GameOver", JOptionPane.INFORMATION_MESSAGE);
         totGamesPlayed--;
         logger.info("Game has been Stopped!!!");
      }
      else if (winner.equals("X")) {
         JOptionPane.showMessageDialog(null, "Player1(" + winner + ") is the  Winner!!", "GameOver", JOptionPane.INFORMATION_MESSAGE);
         xWinCounter.setText(Integer.toString(++xWins));
         logger.info("Player1(X) wins!!");
      }
      else if(winner.equals("O")) {
         JOptionPane.showMessageDialog(null, "Player2(" + winner + ") is the Winner!!", "GameOver", JOptionPane.INFORMATION_MESSAGE);
         oWinCounter.setText(Integer.toString(++oWins));
         logger.info("Player2(O) wins!!");
      }
      else if (winner.equals("D")){
         setBgColors(new JButton[] {b11, b21, b31, b12, b22, b32, b13, b23, b33},Color.gray);
         JOptionPane.showMessageDialog(null, "It's a Tie", "Game Over", JOptionPane.INFORMATION_MESSAGE);
         tieCounter.setText(Integer.toString(++ties));
         logger.info("It's a Tie!!");
      }
      winner = "";
      totGamesCounter.setText(Integer.toString(++totGamesPlayed));
      logger.info("Game "+totGamesPlayed+" Ended");
      synchronized(nGamesListener.class){
         game_progress = false;
         nGamesListener.class.notifyAll();
      }
   }
   synchronized void resetNGames(){
      game_progress=false;
      xWins=oWins=ties=totGamesPlayed=0;
      xWinCounter.setText(Integer.toString(xWins));
      oWinCounter.setText(Integer.toString(oWins));
      tieCounter.setText(Integer.toString(ties));
      totGamesCounter.setText(Integer.toString(totGamesPlayed));
   }

   boolean line(JButton b, JButton c, JButton d) {
      if (!b.getText().equals("") &&b.getText().equals(c.getText()) &&
              c.getText().equals(d.getText()))  {
         if (b.getText().equals("O")) {
            setBgColors(new JButton[] {b,c,d},Color.red);
            winner=b.getText();
         }
         else {
            setBgColors(new JButton[] {b,c,d},Color.green);
            winner=b.getText();
         }
         return true ;
      } else return false;
   }
}


