 import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class OnlineGameServer extends JFrame
{	final int NO_OF_PLAYERS = 3;

	ArrayList<ServerThread> players = new ArrayList<ServerThread>();
	ArrayList<String> playerNames = new ArrayList<String>();
	int nextPlayer = -1;
	String results;
	boolean gameCompleted[] = new boolean[NO_OF_PLAYERS]; 
	int totals[] = new int[NO_OF_PLAYERS];
	
	JTextArea outputArea;
	private ServerSocket serverSocket;
	
	public OnlineGameServer()
	{	super("Online Game Server");
		addWindowListener
		(	new WindowAdapter()
			{	public void windowClosing(WindowEvent e)
				{	System.exit(0);
				}
			}
		);
			
		try
		{	// get a server socket & bind to port 6000
			serverSocket = new ServerSocket(7500);
		}
		catch(IOException e) // thrown by ServerSocket
		{	System.out.println(e);
			System.exit(1);
		}
		
		// create and add GUI components
		Container c = getContentPane();
		c.setLayout(new FlowLayout());
		
		// add text output area
		outputArea = new JTextArea(18,30);
		outputArea.setEditable(false);
		outputArea.setLineWrap(true);
		outputArea.setWrapStyleWord(true);
		outputArea.setFont(new Font("Verdana", Font.BOLD, 11));
		c.add(outputArea);
		c.add(new JScrollPane(outputArea));
		
		setSize(400,320);
		setResizable(false);
		setVisible(true);
	}
	
	void getPlayers()
	{	// output message
		addOutput("Server is up and waiting for a connection...");
		
		// arrays containing valid usernames and passwords
		String usernames[] = {"Robert Smyth", "Mark Allen", "Davina Clark"};
		String passwords[] = {"LuQuezz169", "amG4tyz", "Dw1wU9wy"};
                String gamerTags[] = {"RSFeed", "Arken", "Darklark"};
		boolean playerLoggedOn[] = new boolean[NO_OF_PLAYERS];
		
		// game rules
		String gameRules = "Rules of the game.";			
		 	
		int playerCount = 0;
		while(playerCount < NO_OF_PLAYERS)
		{	try
			{	/* client has attempted to get a connection to server, now 
			       create a socket to communicate with this client */ 
				Socket client = serverSocket.accept(); 
				
				// get input & output streams
				ObjectInputStream input = new ObjectInputStream(client.getInputStream());
				ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
				
				// read username from input stream
				String username = (String) input.readObject();			
				// read password from input stream
				String password = (String) input.readObject();
                                
                                // get the compressed game rules from server
			CompressedMessage cm = (CompressedMessage)input.readObject();
			// decompress game rules
			cm.decompress();
			// output decompressed game rules
			addOutput("Rules of the game\n" + cm.getMessage());
                                
                        // read encrypted username and password
			EncryptedMessage uname = (EncryptedMessage)input.readObject();
			EncryptedMessage pword = (EncryptedMessage)input.readObject();

			// output encrypted username and password
			addOutput("\nLogin Details Received\n----------------------------------------");
			addOutput("encrypted username : " + uname.getMessage());
			addOutput("encrypted password : " + pword.getMessage());

			// decrypt username and password
			uname.decrypt();
			pword.decrypt();

			// output decrypted username and password
			addOutput("decrypted username : " + uname.getMessage());
			addOutput("decrypted password : " + pword.getMessage());
				
				int pos = -1;
				boolean found = false;
		
				// check to ensure that player is registered to play
				for(int l = 0; l < usernames.length; l++)
				{	if(username.equals(usernames[l]) && password.equals(passwords[l]) && !playerLoggedOn[l])
					{	playerLoggedOn[l] = true;
						found = true;
						pos = l;
					}
				}
				
				if(found)
				{	// send Boolean value  true to client
					output.writeObject(new Boolean(true));
					// send game rules to client
					output.writeObject(gameRules);
					
					/* spawn a new thread, i.e. an instance of class P7_ServerThread
				   	   to run parallel with P7_Server and handle all communications
				   	   with this client */
					ServerThread player = new ServerThread(input, output, usernames[pos], playerCount);
					// add this thread to the array list
					players.add(player);
					// start thread - execution of the thread will begin at method run
					player.start();
					String name = usernames[pos];
					playerNames.add(name);
					playerCount++;
				}
				else
					/* player is not registered therefore write a Boolean value
					   false to the output stream */
					output.writeObject(new Boolean(false));
			}
			catch(IOException e) // thrown by Socket
			{	System.out.println(e);
				System.exit(1);
			}
			catch(ClassNotFoundException e) // thrown by method readObject
			{	System.out.println(e);
				System.exit(1);
			}
		}
		nextPlayer = 0;
		// add message to text output area
		addOutput("All players connected...Begin Game...\n");
	}
	
	void addOutput(String s)
	{	// add message to text output area
		outputArea.append(s + "\n");
		outputArea.setCaretPosition(outputArea.getText().length());
	}
	
	synchronized void setNextPlayer(int player)
	{	// find if all players have finished play 
		boolean b = allPlayersCompleted();
		// if some players are still playing 
		if(!b)
		{	// set variable to point to the next player in turn
			int n = player + 1;
			boolean found = false;
			
			while(!found)
			{	if(n == NO_OF_PLAYERS)
					// if this is the last player go back to first player
					n = 0;
				// if this player is still in play set this player as the next player
				if(!gameCompleted[n])
				{	nextPlayer = n;
					found = true;
				}
				n++;	
			}
		}
	}
	
	synchronized boolean isMyTurn(int player)
	{	// return true if this player is the next player otherwise return false
		return player == nextPlayer;
	}	
	
	synchronized void setGameCompleted(int player)
	{	// set array element to true to indicate that this player has finished play
		gameCompleted[player] = true;			
	}
	
	synchronized boolean allPlayersCompleted()
	{	boolean continueGame = true;
		/* traverse the game completed array and test 
		   values, if all contain true all players are awaiting the results */
		for(int i = 0; i < gameCompleted.length; i++)
		{	if(!gameCompleted[i])
				continueGame = false;
		}
	
		return continueGame; 
	}
	
	synchronized void updateTotal(int player, int[] dice)
	{	for(int i = 0; i < dice.length; i++)
			// add new dice results to current total
			totals[player] += dice[i]; 
	}
	
	synchronized String getMessage(int player)
	{	String str;
		if(totals[player] > 36)
			// if the player has scored more than 36 they immediatly lose the game 
			str = "Your final total of " + totals[player] + " is greater than 36...please wait for results";
		else
			// if the player has scored below 36 they can stand on their score 
			str = "Your total so far is " + totals[player] + " do you wish to stand?";
			
		return str;
	} 
		
	String getResults(ArrayList<Integer> score36, ArrayList<Integer> under36, ArrayList<Integer> over36)
	{	String message = "---------------------------------\nResults\n---------------------------------\n";
		/* a player(s) has scored 36 therefore traverse "score 36" category 
		   and list players with scores */
		for(Integer n : score36)
		{	message += playerNames.get(n) + " scored " + totals[n] + " - "; 
			/* if the array list contains one element there is only 
			   one winner, otherwise there are 2 or more joint winners */
			if(score36.size() == 1)
				message += "WINNER\n";
			else
				message += "JOINT WINNER\n";
		}		
		// traverse "under 36" category and list players with scores	
		for(Integer n : under36)
			message += playerNames.get(n) + " scored " + totals[n] + " - Too Low\n"; 		
		// traverse "over 36" category and list players with scores
		for(Integer n : over36)
			message += playerNames.get(n) + " scored " + totals[n] + " - Too High\n";
			
		return message;
	}
	
	String getResults(ArrayList<Integer> under36, ArrayList<Integer> over36)
	{	// the winner(s) is in the "under 36" category 
		String message = "---------------------------------\nResults\n---------------------------------\n";
		
		/* if the array list contains one element there is only 
		   one winner, otherwise there are 2 or more joint winners */ 
		if(under36.size() == 1) 
			message += playerNames.get(under36.get(0)) + " scored " + totals[under36.get(0)] + " - WINNER\n";
		else
		{	int highest = Integer.MIN_VALUE;
			int win = 0;
			/* there is more than 1 player in the "under 36" category therefore 
			   find score closest to 36 and count number of players with this score */
			for(int n : under36)
			{	if(totals[n] > highest)
				{	highest = totals[n];
					win = 1;
				}
				else
				{	if(totals[n] == highest)
						win++;
				}	
			}
			// traverse "under 36" category and list players with scores
			for(Integer n : under36)
			{	message += playerNames.get(n) + " scored " + totals[n] + " - "; 
				if(totals[n] == highest)
				{	// this player's score matches the winning score
					if(win == 1)
						// there is only 1 player with the winning score
						message += "WINNER\n";
					else
						/* more than 1 player has the winning score, this
						   player is a joint winner */
						message += "JOINT WINNER\n";
				}
				else
					// this player's score does not match the winning score
					message += " Too Low\n";
			}
		}
		// traverse "over 36" category and list players with scores
		for(Integer n : over36)
			message += playerNames.get(n) + " scored " + totals[n] + " - Too High\n";
			
		return message;
	}
	
	String getResults(ArrayList<Integer> over36)
	{	// there are no winners
		String message = "---------------------------------\nResults\n---------------------------------\nThere Are No Winners!\n";
		// traverse "over 36" category and list players with scores
		for(Integer n : over36)
			message += playerNames.get(n) + " scored " + totals[n] + " - Too High\n";
			
		return message;
	}  
		
	synchronized void setResults(int p)
	{	// initialise 3 array lists to store the player numbers 
		ArrayList<Integer> score36 = new ArrayList<Integer>();
		ArrayList<Integer> under36 = new ArrayList<Integer>();
		ArrayList<Integer> over36 = new ArrayList<Integer>();
		/* traverse player totals array and sort player into 
		   categories according to score */
		for(int i = 0; i < totals.length; i++)
		{	if(totals[i] == 36)
				/* if this player has scored 36 add their number 
				   to "score 36" category */
				score36.add(i);
			else
			{	if(totals[i] < 36)
					/* if this player has scored under 36 add their 
					   number to "under 36" category */
					under36.add(i);
				else
					/* if this player has scored more than 36 add their 
					   number to "over 36" category */
					over36.add(i);
			}
		}
		
		if(!score36.isEmpty()) 
			// a player(s) has scored 36 exactly
			results = getResults(score36, under36, over36);
		else
		{	if(!under36.isEmpty()) 
				/* no players have scored 36 exactly therefore the 
				   winner is in the "under 36" category */
				results = getResults(under36, over36);
			else
				/* no players have scored 36 exactly or is in the 
				  "under 36" category, therefore there are no winners */
				results = getResults(over36);
		}
	}		

	// main method of class P7_Server
	public static void main(String args[])
	{	OnlineGameServer gameServer = new OnlineGameServer();
		gameServer.getPlayers();
	}
	
	// beginning of class P7_ServerThread		
	private class ServerThread extends Thread 
	{	ObjectInputStream threadInputStream;
		ObjectOutputStream threadOutputStream;
		String playerName;
		int playerNumber; 

		public ServerThread(ObjectInputStream in, ObjectOutputStream out, String name, int num)
		{	// initialise input stream
			threadInputStream = in;
			// initialise output stream
			threadOutputStream = out;
			// initialise player name
			playerName = name;
			// initialise player number
			playerNumber = num;
  		}
  	
  		void goToSleep()
  		{	try
			{	// this thread will sleep for 1000 milliseconds
				this.sleep(1000);
			}
			catch(InterruptedException e)
			{	System.out.println(e);
				System.exit(1);
			}
  		}
  
  		public void run()
  		{	try
  			{	/* when method start() is called thread execution will begin at  
  		    	   the following statement */
  		    	String broadcastMessage = playerName + " has got a connection.";  	 
  				// add message to server text output area 
  				addOutput(broadcastMessage);
  				// send message to client - for broadcast text output area
  				broadcast(broadcastMessage);
  			
  				if(playerNumber == 0 || playerNumber == 1)
  					// if this is the first or second player, send this message to client
  					threadOutputStream.writeObject("Welcome to the game " + playerName + "...you are waiting for another player(s)...");
				else
					// if this is the third player, send this message to client
					threadOutputStream.writeObject("Welcome to the game " + playerName + "...other player(s) connected...please wait");
				
				String clientMessage;
				boolean play;
				boolean roundOver = false;
				while(!roundOver)
				{	// find if it is this player's turn to play		
					play = isMyTurn(playerNumber);
					while(!play)
					{	// while it is not the player's turn send thread to sleep
						goToSleep();
						// find if it is this player's turn to play	
						play = isMyTurn(playerNumber);
					}
					
					// send message to client
					threadOutputStream.writeObject("Make a roll");
					// read dice values from client
					int[] dice = (int[])threadInputStream.readObject();
					// create message
					broadcastMessage = playerName + " has rolled " + dice[0] + "-" + dice[1] + "-" + dice[2];
					// add message to server text output area
					addOutput(broadcastMessage);
					// send message to client - for broadcast text output area
					broadcast(broadcastMessage);
					// update player's total
					updateTotal(playerNumber, dice);
					// get message to progress game 
					clientMessage = getMessage(playerNumber);
					// send message to client
					threadOutputStream.writeObject(clientMessage);
				
					if(clientMessage.charAt(clientMessage.length()-1) == '?')
					{	/* if the message sent to the client ends with '?' 
				    	   (i.e. is the player going to stand), read reply */
						String reply = (String)threadInputStream.readObject();
						if(reply.equals("yes")) 
						{	addOutput(playerName + " is standing");
							// player is standing, send message to client
							threadOutputStream.writeObject("You have chosen to stand on your score...please wait for results...");
							// set player completed marker
							setGameCompleted(playerNumber);
							// the game is finished, set variable to true to exit loop
							roundOver = true;
							
						}
						else
						{	addOutput(playerName + " is still playing");
							// playing is continuing play, send message to client
							threadOutputStream.writeObject("Please wait for your next turn...");
						}
					}
					else
					{	addOutput(playerName + "'s score has exceeded 36 ... out of game");
						// set player completed marker
						setGameCompleted(playerNumber);
						// the game is finished, set variable to true to exit loop
						roundOver = true;
					}
					// set next player to play
					setNextPlayer(playerNumber);
				} // end while
				
				// find if all players have completed play
				play = allPlayersCompleted();
				while(!play)
				{	// while waiting for other players to finish play send thread to sleep
					goToSleep();
					// find if all players have completed play
					play = allPlayersCompleted();
				}
			
				if(results == null)
					// if the results string has not yet been completed, set results string
					setResults(playerNumber);
				
				addOutput("Sending results to " + playerName);
        		// send results string to client
				threadOutputStream.writeObject(results);
				// send game over command to client	
				threadOutputStream.writeObject("Game Over");	
				// close input stream
				threadInputStream.close();
				// close output stream
				threadOutputStream.close();	
  			}
			catch(IOException e) // thrown by method readObject, writeObject, close
			{	System.out.println(e);
				System.exit(1);
			}
			catch(ClassNotFoundException e) // thrown by method readObject
			{	System.out.println(e);
				System.exit(1);
			}	
   		}
   	
   		protected void broadcast(String str) 
		{	synchronized(players)
			{	// traverse array list of threads
				for (ServerThread sThread : players)
				{	try
					{	// send broadcast message to all players 
						sThread.threadOutputStream.writeObject("B" + str);
					}
					catch(IOException e) // thrown by method writeObject
					{	System.out.println(e);
						System.exit(1);
					}
				}
			}
		}
	} // end of class P7_ServerThread
} // end of class P7_Server



