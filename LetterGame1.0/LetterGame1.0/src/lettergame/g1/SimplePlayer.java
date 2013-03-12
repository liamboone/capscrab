package lettergame.g1;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

import lettergame.ui.CSVReader;
import lettergame.ui.Letter;
import lettergame.ui.LetterGame;
import lettergame.ui.Player;
import lettergame.ui.PlayerBids;
import lettergame.ui.SecretState;
import lettergame.ui.Word;


/*
 * This player bids randomly within a certain range
 * based on the letter's value.
 * 
 * Keep in mind that the Player superclass has the following fields: 
	Logger logger
	ArrayList<Character> currentLetters
	int myID
	ArrayList<Word> wordlist
 */

public class SimplePlayer extends Player {

	public SimplePlayer() {
		super();
	}
	
	// for generating random numbers
	private Random random = new Random();
		

	private ArrayList<Integer>[][] bidstrategy;
	
    /*
     * This is called once at the beginning of a Game.
     * The id is what the game considers to be your unique identifier
     * The number_of_rounds is the total number of rounds to be played in this game
     * The number_of_players is, well, the number of players (including you!).
     */
	public void newGame(int id, int number_of_rounds, int number_of_players) {
		myID = id;
		bidstrategy = new ArrayList[7][26];
		for( int i = 0; i < 7; i ++ )
		{
			for( int j = 0; j < 26; j++ )
			{
				bidstrategy[i][j] = new ArrayList<Integer>();
			}
		}
		try
		{
            CSVReader csvreader = new CSVReader(new FileReader("strategy.txt"), ';');
            String[] nextLine;
            int hand = 0;
            while((nextLine = csvreader.readNext()) != null)
            {
            	if (nextLine.length > 1)  
            	{
            		for( int i = 0; i < 26; i ++ )
            		{
            			String[] bids = nextLine[i].split(",");
            			for( String bid : bids )
            			{
            				bidstrategy[hand][i].add( Integer.valueOf( bid ) );
            			}
            		}
           		}
            	hand++;
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
            logger.error("\n Could not load strategy!");
        }
	}


	/*
	 * This method is called at the beginning of a new round.
	 * The secretState contains your current score and the letters that were secretly given to you in this round
	 * The current_round indicates the current round number (0-based)
	 */
	public void newRound(SecretState secretState, int current_round) {

		// be sure to reinitialize the list at the start of the round
		currentLetters = new ArrayList<Character>();
		
		// add any letters from the secret state
		for (Letter l : secretState.getSecretLetters()) {
			//logger.trace("myID = " + myID + " and I'm adding " + l + " from the secret state");
			currentLetters.add(l.getCharacter());
		}
	}
	

	/*
	 * This method is called when there is a new letter available for bidding.
	 * bidLetter = the Letter that is being bid on
	 * playerBidList = the list of all previous bids from all players
	 * playerList = the class names of the different players
	 * secretState = your secret state (which includes the score)
	 */
	public int getBid(Letter bidLetter, ArrayList<PlayerBids> playerBidList, ArrayList<String> playerList, SecretState secretState) {
		
		int bid = random.nextInt(bidstrategy[currentLetters.size()][bidLetter.getCharacter()-'A'].size());
		return bidstrategy[currentLetters.size()][bidLetter.getCharacter()-'A'].get(bid);
	}

	
	/*
	 * This method is called after a bid. It indicates whether or not the player
	 * won the bid and what letter was being bid on, and also includes all the
	 * other players' bids. 
	 */
    public void bidResult(boolean won, Letter letter, PlayerBids bids) {
    	if (won) {
    		//logger.trace("My ID is " + myID + " and I won the bid for " + letter);
    		currentLetters.add(letter.getCharacter());
    	}
    	else {
    		//logger.trace("My ID is " + myID + " and I lost the bid for " + letter);
    	}
    }

    /*
     * This method is called after all the letters have been purchased in the round.
     * The word that you return will be scored for this round.
     */
	public String getWord() {
		char c[] = new char[currentLetters.size()];
		for (int i = 0; i < c.length; i++) {
			c[i] = currentLetters.get(i);
		}
		String s = new String(c);
		//logger.trace("Player " + myID + " letters are " + s);
		Word ourletters = new Word(s);
		Word bestword = new Word("");

		// iterate through all Words in the list
		// and see which ones we can form
		for (Word w : wordlist) {
			if (ourletters.contains(w)) {
				int score = w.getScore();
				// don't forget the bonus!
				if (w.getLength() == 7) score += 50;
				if (score > bestword.getScore()) {
					bestword = w;
				}

			}
		}
		logger.trace("My ID is " + myID + " and my word is " + bestword.getWord());
		
		return bestword.getWord();
	}

	/*
	 * This method is called at the end of the round
	 * The ArrayList contains the scores of all the players, ordered by their ID
	 */
	public void updateScores(ArrayList<Integer> scores) {
		
	}




}
