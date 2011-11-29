
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.Arrays;

public class OnlineGameClient extends JFrame {	// client socket	

    Socket socket;
    // output stream - data sent to the server will be written to this stream
    ObjectOutputStream clientOutputStream;
    // input stream - data sent by the server will be read from this stream
    ObjectInputStream clientInputStream;
    // variables for the GUI components of the game 
    Container c;
    JButton logonButton, rollButton, yesButton, noButton, startButton;
    ButtonHandler bHandler;
    JPanel gamePanel, buttonPanel, messagePanel, outputPanel, logonFieldsPanel, logonButtonPanel, myMovesLabel, broadcastLabel;
    JLabel usernameLabel, passwordLabel, dieImage1, dieImage2, dieImage3;
    JTextArea outputArea, playerArea, broadcastArea;
    JTextField username;
    JPasswordField password;
    ImageIcon[] diceImages = {new ImageIcon("blank.gif"), new ImageIcon("die_1.gif"), new ImageIcon("die_2.gif"), new ImageIcon("die_3.gif"), new ImageIcon("die_4.gif"), new ImageIcon("die_5.gif"), new ImageIcon("die_6.gif")};

    public OnlineGameClient() {
        super("Online Game Client");
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        /* the initial GUI will provide a text field and password field 
        to enable the user to enter their username and password and 
        attempt to logon to the game system */

        // create and add GUI components
        c = getContentPane();
        c.setLayout(new BorderLayout());

        // GUI components for the username
        logonFieldsPanel = new JPanel();
        logonFieldsPanel.setLayout(new GridLayout(2, 2, 5, 5));
        usernameLabel = new JLabel("Enter Username: ");
        logonFieldsPanel.add(usernameLabel);
        username = new JTextField(10);
        logonFieldsPanel.add(username);

        // GUI components for the password
        passwordLabel = new JLabel("Enter Password: ");
        logonFieldsPanel.add(passwordLabel);
        password = new JPasswordField(10);
        logonFieldsPanel.add(password);
        c.add(logonFieldsPanel, BorderLayout.CENTER);

        // panel for the logon button
        logonButtonPanel = new JPanel();
        logonButton = new JButton("logon");
        bHandler = new ButtonHandler();
        logonButton.addActionListener(bHandler);
        logonButtonPanel.add(logonButton);
        c.add(logonButtonPanel, BorderLayout.SOUTH);

        setSize(300, 125);
        setResizable(false);
        setVisible(true);
    }

    void setUpGame(boolean loggedOn, String mess) {	// remove iniial GUI components (textfield, password field, logon button)
        c.remove(logonFieldsPanel);
        c.remove(logonButtonPanel);

        // if the player has not logged on an error message will be displayed 
        if (!loggedOn) {
            outputPanel = new JPanel();
            outputPanel.setBackground(Color.WHITE);
            // add text area	
            outputArea = new JTextArea(12, 30);
            outputArea.setEditable(false);
            outputArea.setLineWrap(true);
            outputArea.setWrapStyleWord(true);
            outputArea.setFont(new Font("Verdana", Font.BOLD, 11));
            // add message to text area
            outputArea.setText(mess);
            outputPanel.add(outputArea);
            outputPanel.add(new JScrollPane(outputArea));
            c.add(outputPanel, BorderLayout.CENTER);
            setSize(375, 300);
        } else {	// if the player has logged on the game GUI will be set up
            messagePanel = new JPanel();
            messagePanel.setLayout(new GridLayout(2, 1));
            // GUI components for broadcast messages area
            JPanel broadcastPanel = new JPanel();
            broadcastPanel.setLayout(new BorderLayout());
            JLabel broadcastLabel = new JLabel();
            broadcastLabel.setText("Broadcast Messages");
            broadcastPanel.add(broadcastLabel, BorderLayout.NORTH);
            broadcastArea = new JTextArea(15, 30);
            broadcastArea.setForeground(Color.GRAY);
            broadcastArea.setEditable(false);
            broadcastArea.setLineWrap(true);
            broadcastArea.setWrapStyleWord(true);
            broadcastArea.setFont(new Font("Verdana", Font.BOLD, 11));
            broadcastPanel.add(broadcastArea, BorderLayout.CENTER);
            broadcastPanel.add(new JScrollPane(broadcastArea), BorderLayout.CENTER);
            messagePanel.add(broadcastPanel);

            // GUI components for player messages area
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new BorderLayout());
            JLabel playerLabel = new JLabel();
            playerLabel.setText("My Messages");
            playerPanel.add(playerLabel, BorderLayout.NORTH);

            playerArea = new JTextArea(15, 30);
            playerArea.setEditable(false);
            playerArea.setLineWrap(true);
            playerArea.setWrapStyleWord(true);
            playerArea.setForeground(Color.BLACK);
            playerArea.setFont(new Font("Verdana", Font.BOLD, 11));
            // add the decompressed game rules to player message area
            playerArea.setText(mess + "\n--------------------------\n");
            playerPanel.add(playerArea, BorderLayout.CENTER);
            playerPanel.add(new JScrollPane(playerArea), BorderLayout.CENTER);
            messagePanel.add(playerPanel);
            c.add(messagePanel, BorderLayout.EAST);

            // panel that will contain the dice images and game buttons
            gamePanel = new JPanel();
            gamePanel.setLayout(new GridLayout(4, 1));
            gamePanel.setBackground(Color.WHITE);
            dieImage1 = new JLabel(diceImages[0], SwingConstants.CENTER);
            dieImage2 = new JLabel(diceImages[0], SwingConstants.CENTER);
            dieImage3 = new JLabel(diceImages[0], SwingConstants.CENTER);

            gamePanel.add(dieImage1);
            gamePanel.add(dieImage2);
            gamePanel.add(dieImage3);

            // create game buttons, initially they are disabled
            ButtonHandler bHandler = new ButtonHandler();
            rollButton = new JButton("roll dice");
            rollButton.addActionListener(bHandler);
            rollButton.setEnabled(false);
            yesButton = new JButton("yes");
            yesButton.addActionListener(bHandler);
            yesButton.setEnabled(false);
            noButton = new JButton("no");
            noButton.addActionListener(bHandler);
            noButton.setEnabled(false);
            buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(rollButton);
            buttonPanel.add(yesButton);
            buttonPanel.add(noButton);
            gamePanel.add(buttonPanel);
            c.add(gamePanel, BorderLayout.CENTER);
            setSize(500, 500);
        }
        setResizable(false);
        setVisible(true);
    }

    void sendLoginDetails() {
        try {	// get username from text field 
            //String uname = username.getText();
            // get password from password field 
            //String pword = new String (password.getPassword());	
            // send username to server 
            //clientOutputStream.writeObject(uname);
            // send password to server
            //clientOutputStream.writeObject(pword);

            // get username from text field and encrypt
            EncryptedMessage uname = new EncryptedMessage(username.getText());
            uname.encrypt();

            // get password from password field and encrypt
            EncryptedMessage pword = new EncryptedMessage(new String(password.getPassword()));
            pword.encrypt();

            // send encrypted username and password to server
            clientOutputStream.writeObject(uname);
            clientOutputStream.writeObject(pword);

            // get the compressed game rules from server
            CompressedMessage cm = (CompressedMessage) clientInputStream.readObject();
            // decompress game rules
            cm.decompress();
            // output decompressed game rules			
            addOutput("Rules of the game\n" + cm.getMessage());

        } catch (IOException e) // thrown by methods writeObject
        {
            System.out.println(e);
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    void addOutput(String s) {	// add a message to the player text output area
        playerArea.append(s + "\n");
        playerArea.setCaretPosition(playerArea.getText().length());
    }

    void addBroadcast(String s) {	// add a message to the broadcast text output area
        broadcastArea.append(s + "\n");
        broadcastArea.setCaretPosition(broadcastArea.getText().length());
    }

    void getConnections() {
        try {	// initialise a socket and get a connection to server
            socket = new Socket(InetAddress.getLocalHost(), 7500);
            // get input & output object streams
            clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientInputStream = new ObjectInputStream(socket.getInputStream());

            /* create a new thread of P7_ClientThread, sending input  
            stream variable as a parameter */
            ClientThread clientT = new ClientThread(clientInputStream);
            // start thread - execution will begin at method run
            clientT.start();
        } catch (UnknownHostException e) // thrown by method getLocalHost
        {
            System.out.println(e);
            System.exit(1);
        } catch (IOException e) // thrown by methods ObjectOutputStream, ObjectInputStream
        {
            System.out.println(e);
            System.exit(1);
        }
    }

    void sendDice() {
        try {	// an object of class Random is required to create random numbers for the dice
            Random randomNumbers = new Random();
            // get random numbers and store in an array
            int dice[] = new int[3];
            for (int i = 0; i < dice.length; i++) {
                dice[i] = 1 + randomNumbers.nextInt(6);
            }
            // sort array	
            Arrays.sort(dice);
            // set the appropriate images
            dieImage1.setIcon(diceImages[dice[0]]);
            dieImage2.setIcon(diceImages[dice[1]]);
            dieImage3.setIcon(diceImages[dice[2]]);
            // send the dice values to the server
            clientOutputStream.writeObject(dice);
        } catch (IOException e) // thrown by method writeObject
        {
            System.out.println(e);
            System.exit(1);
        }
    }

    void sendMessage(String message) {
        try {	// send a message (i.e. "yes" or "no") to the server
            clientOutputStream.writeObject(message);
        } catch (IOException e) // thrown by method writeObject
        {
            System.out.println(e);
            System.exit(1);
        }
    }

    void closeStreams() {
        try {	// close input stream 
            clientOutputStream.close();
            // close output stream 
            clientInputStream.close();
            // close socket 
            socket.close();
        } catch (IOException e) // thrown by method close
        {
            System.out.println(e);
            System.exit(1);
        }
    }

    // main method of class P7_Client
    public static void main(String args[]) {
        OnlineGameClient gameClient = new OnlineGameClient();
        gameClient.getConnections();
    }


    /* -----------------------------------------------------------------------
    beginning of class P7_ClientThread
    ----------------------------------------------------------------------- */
    private class ClientThread extends Thread {

        ObjectInputStream threadInputStream;

        public ClientThread(ObjectInputStream in) {	// initialise input stream
            threadInputStream = in;
        }

        public void run() {	// when method start is called thread execution will begin in this method  
            try {	/* read Boolean value sent by server - it is converted to
                a primitive boolean value */
                boolean loggedOn = (Boolean) threadInputStream.readObject();

                if (!loggedOn) { 	// call method to close input & output streams & socket
                    closeStreams();
                    // call method to display message
                    setUpGame(loggedOn, "Logon unsuccessful");
                } else {	// if the client is logged on read the game rules 
                    String rules = (String) threadInputStream.readObject();
                    // call method to set up game GUI
                    setUpGame(loggedOn, rules);

                    String message;
                    boolean roundOver = false;
                    // while game is in play
                    while (!roundOver) {	// read a message from the server
                        message = (String) threadInputStream.readObject();
                        /* if the first character of the message is 'B' this 
                        is a message for the broadcast text area */
                        if (message.charAt(0) == 'B') /* remove the first character of the message and 
                        add to broadcast output area */ {
                            addBroadcast(message.substring(1, message.length()));
                        } else // add message to the player text area
                        {
                            addOutput(message);
                        }

                        /* if the last character in the message is '?' 
                        enable the yes button and the no button */
                        if (message.charAt(message.length() - 1) == '?') {
                            yesButton.setEnabled(true);
                            noButton.setEnabled(true);
                        }
                        // if player should make a roll enable the roll button
                        if (message.equals("Make a roll")) {
                            rollButton.setEnabled(true);
                        }
                        // the game is finished, set variable to true to exit loop
                        if (message.equals("Game Over")) {
                            roundOver = true;
                        }
                    }
                    // call method to close input & output streams & socket
                    closeStreams();
                }
            } catch (IOException e) // thrown by method readObject
            {
                System.out.println(e);
                System.exit(1);
            } catch (ClassNotFoundException e) // thrown by method readObject
            {
                System.out.println(e);
                System.exit(1);
            }
        }
    } // end of class P7_ClientThread

    // beginning of class ButtonHandler - inner class for event handling
    private class ButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == logonButton) // if the logon button is clicked call method sendLoginDetails 
            {
                sendLoginDetails();
            } else {
                if (e.getSource() == rollButton) {	/* if the logon button is clicked disable the roll
                    button and call method sendDice */
                    rollButton.setEnabled(false);
                    sendDice();
                } else { 	/* if the "yes" or "no" buttons have been clicked disable 
                    both buttons */
                    yesButton.setEnabled(false);
                    noButton.setEnabled(false);
                    /* if the "yes" button was clicked call method sendMessage 
                    with parameter "yes" */
                    if (e.getSource() == yesButton) {
                        sendMessage("yes");
                    } else /* if the "no" button was clicked call method sendMessage 
                    with parameter "no" */ {
                        sendMessage("no");
                    }
                }
            }
        }
    }  // end of class ButtonHandler  
} // end of class P7_Client

