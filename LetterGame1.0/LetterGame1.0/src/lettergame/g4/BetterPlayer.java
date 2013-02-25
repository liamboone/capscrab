package lettergame.g4;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lettergame.ui.Letter;
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

public class BetterPlayer extends Player {

	public final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public BetterPlayer() {
		super();
	}
	
	// for generating random numbers
	private Random random = new Random();
		

	
    /*
     * This is called once at the beginning of a Game.
     * The id is what the game considers to be your unique identifier
     * The number_of_rounds is the total number of rounds to be played in this game
     * The number_of_players is, well, the number of players (including you!).
     */
	public void newGame(int id, int number_of_rounds, int number_of_players) {
		myID = id;
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
	
	//return the number of letters in w2 not in w1
	private Word extraLetters( Word w1, Word w2 )
	{
		String extras = "";
		for( int i = 0; i < 26; i ++ )
		{
			if( w2.countKeep[i] > w1.countKeep[i] )
			{
				for( int j = 0; j < w2.countKeep[i] - w1.countKeep[i]; j++ )
				{
					extras += LETTERS.charAt(i);
				}
			}
		}
		return new Word(extras);
	}
	
	private boolean canMake( Word w, Word letters, int deadletters )
	{
		String s = letters.getWord();
		Word extras = extraLetters( letters, w );
		if( deadletters + letters.getLength() + extras.getLength() > 7 )
		{
			return false;
		}
		if( w.contains(letters) ) return true;
		for( int i = 0; i < s.length(); i ++ )
		{
			String n = s.substring(0, i) + s.substring(i+1);
			if( canMake( w, new Word( n ), deadletters + 1 ) ) return true;
		}
		return false;
	}

	private Word bestWord(Word letters){
		Word bestword = new Word("");

		// iterate through all Words in the list
		// and see which ones we can form
		int currentBest = 0;
		
		for (Word w : wordlist) 
		{
			if( w.getLength() <= 7 && canMake( w, letters, 0 ) )
			{
				int score = w.getScore();
				// don't forget the bonus!
				if ( w.getLength() == 7 ) score += 50;
				if ( score > currentBest ) 
				{
					currentBest = score;
					bestword = w;
				}
			}
		}
		return bestword; 
	}
	
	public List<Word> possibleWords( Word letters )
	{
		ArrayList<Word> possibilities = new ArrayList<Word>();
		for( Word w : wordlist )
		{
			if( w.getLength() <= 7 && canMake( w, letters, 0 ) )
			{
				possibilities.add( w );
			}
		}
		return possibilities;
	}
	
	public float probablility( Word letters )
	{
		float p = 1.0f;
		
		return p;
	}
	
	/*
	 * This method is called when there is a new letter available for bidding.
	 * bidLetter = the Letter that is being bid on
	 * playerBidList = the list of all previous bids from all players
	 * playerList = the class names of the different players
	 * secretState = your secret state (which includes the score)
	 */
	public int getBid(Letter bidLetter, ArrayList<PlayerBids> playerBidList, ArrayList<String> playerList, SecretState secretState) {
		char c[] = new char[currentLetters.size()+1];
		for (int i = 0; i < currentLetters.size(); i++) {
			c[i+1] = currentLetters.get(i);
		}
		c[0] = bidLetter.getCharacter();
		String s = new String(c);
		Word letters = new Word( s );
		
		ArrayList<Word> p = (ArrayList<Word>) possibleWords( letters );
		
		Word bestword = bestWord( letters );
		int score = ( bestword.getScore() + ( bestword.getLength() == 7 ? 50 : 0 ) );
		Word extras = extraLetters( letters, bestword );
		
		logger.trace( "Best possible word so far: " + bestword.getWord( ) + " for " + score + " with " + extras.getWord() + " of " + p.size() );
		
		return bidLetter.getValue();
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

		int currentBest = 0;
		// iterate through all Words in the list
		// and see which ones we can form
		for (Word w : wordlist) {
			if (ourletters.contains(w)) {
				int score = w.getScore();
				// don't forget the bonus!
				if (w.getLength() == 7) score += 50;
				if (score > currentBest) {
					currentBest = score;
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
