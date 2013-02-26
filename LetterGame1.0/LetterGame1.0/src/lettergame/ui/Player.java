package lettergame.ui;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 *
 * @author Chris Murphy
 */
public abstract class Player {

    public abstract void newGame(int id, int number_of_rounds, int number_of_players);
    
    public abstract void newRound(SecretState secretState, int current_round);

    public abstract int getBid(Letter bidLetter, ArrayList<PlayerBids> PlayerBidList, ArrayList<String> PlayerList, SecretState secretstate);
    
    public abstract void bidResult(boolean won, Letter letter, PlayerBids bids);

    public abstract String getWord();
    
    public abstract void updateScores(ArrayList<Integer> scores);
    
	// for logging
	protected Logger logger = Logger.getLogger(this.getClass());

	// the set of letters that this player currently has
	protected ArrayList<Character> currentLetters;
	
	// unique ID
	protected int myID;
	
	// the list of all the words in the dictionary
	protected static ArrayList<Word> wordlist = new ArrayList<Word>(267000);
	
	private static boolean didreadthelist = false;
	
	protected Player() {
    	if ( didreadthelist ) return;
    	didreadthelist = true;
        try{
            CSVReader csvreader = new CSVReader(new FileReader(LetterGame.DICTIONARY_FILE));
            String[] nextLine;
            while((nextLine = csvreader.readNext()) != null)
            {
            	if (nextLine.length > 1)  {
            		String word = nextLine[1];
            		wordlist.add(new Word(word));
           		}

            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("\n Could not load dictionary!");
        }

	}
    
}

