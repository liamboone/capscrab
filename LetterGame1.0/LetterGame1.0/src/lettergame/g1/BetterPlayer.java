package lettergame.g1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import lettergame.ui.Letter;
import lettergame.ui.LetterGameValues;
import lettergame.ui.Player;
import lettergame.ui.PlayerBids;
import lettergame.ui.SecretState;
import lettergame.ui.Word;

/*
 * Keep in mind that the Player superclass has the following fields: 
 Logger logger
 ArrayList<Character> currentLetters
 int myID
 ArrayList<Word> wordlist
 */

public class BetterPlayer extends Player {

	public final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public int[] bagOfLetters;

	public BetterPlayer() {
		super();
	}

	// for generating random numbers
	private Random random = new Random();

	private int spent;

	private float lastExpected;

	private float ifWonExpected;

	/*
	 * This is called once at the beginning of a Game. The id is what the game
	 * considers to be your unique identifier The number_of_rounds is the total
	 * number of rounds to be played in this game The number_of_players is,
	 * well, the number of players (including you!).
	 */
	public void newGame(int id, int number_of_rounds, int number_of_players) {
		myID = id;
	}

	/*
	 * This method is called at the beginning of a new round. The secretState
	 * contains your current score and the letters that were secretly given to
	 * you in this round The current_round indicates the current round number
	 * (0-based)
	 */
	public void newRound(SecretState secretState, int current_round) {
		spent = 0;
		lastExpected = 6;
		bagOfLetters = new int[LETTERS.length()];
		for (int i = 0; i < LETTERS.length(); i++) {
			bagOfLetters[i] = LetterGameValues.getLetterFrequency(LETTERS
					.charAt(i));
		}

		logger.trace(probability(new Word("POTATO"), bagOfLetters ));
		
		// be sure to reinitialize the list at the start of the round
		currentLetters = new ArrayList<Character>();

		// add any letters from the secret state
		for (Letter l : secretState.getSecretLetters()) {
			// logger.trace("myID = " + myID + " and I'm adding " + l +
			// " from the secret state");
			currentLetters.add(l.getCharacter());
			bagOfLetters[Integer.valueOf(l.getCharacter())
					- Integer.valueOf('A')]--;
		}
	}

	// return the number of letters in w2 not in w1
	private Word extraLetters(Word w1, Word w2) {
		String extras = "";
		for (int i = 0; i < 26; i++) {
			if (w2.countKeep[i] > w1.countKeep[i]) {
				for (int j = 0; j < w2.countKeep[i] - w1.countKeep[i]; j++) {
					extras += LETTERS.charAt(i);
				}
			}
		}
		return new Word(extras);
	}

	private boolean isPossible(Word w1, int[] letters) {
		for (int i = 0; i < 26; i++) {
			if (w1.countKeep[i] > letters[i]) {
				return false;
			}
		}
		return true;
	}

	private boolean canMake(Word w, Word letters, int deadletters) {
		String s = letters.getWord();
		Word extras = extraLetters(letters, w);
		if (deadletters + letters.getLength() + extras.getLength() > 7) {
			return false;
		}
		if (w.contains(letters))
			return true;
		for (int i = 0; i < s.length(); i++) {
			String n = s.substring(0, i) + s.substring(i + 1);
			if (canMake(w, new Word(n), deadletters + 1))
				return true;
		}
		return false;
	}

	private int bestWord(ArrayList<Integer> possible, Word letters, int[] bag) {
		int bestword = -1;

		// iterate through all Words in the list
		// and see which ones we can form
		int currentBest = 0;

		for (int i : possible) {
			Word w = wordlist.get(i);
			if (canMake(w, letters, 0)) {
				int score = w.getScore();
				// don't forget the bonus!
				if (w.getLength() == 7)
					score += 50;
				if (score > currentBest) {
					currentBest = score;
					bestword = i;
				}
			}
		}
		return bestword;
	}

	private int bestCurrentWord(ArrayList<Integer> possible, Word letters, int[] bag) {
		int bestword = -1;

		// iterate through all Words in the list
		// and see which ones we can form
		int currentBest = 0;

		for (int i : possible) {
			Word w = wordlist.get(i);
			if (letters.contains(w)) {
				int score = w.getScore();
				// don't forget the bonus!
				if (w.getLength() == 7)
					score += 50;
				if (score > currentBest) {
					currentBest = score;
					bestword = i;
				}
			}
		}
		return bestword;
	}

	
	public ArrayList<Integer> possibleWords(Word letters, int[] bag) {
		ArrayList<Integer> possibilities = new ArrayList<Integer>();
		for (int i = 0; i < wordlist.size(); i++) {
			Word w = wordlist.get(i);
			if (w.getLength() <= 7 && isPossible(extraLetters(letters, w), bag)) {
				if (canMake(w, letters, 0)) {
					possibilities.add(i);
				}
			}
		}
		return possibilities;
	}

	/*
	 * Returns the probability of a collection of letters being pulled from the
	 * given bag of letters bag here is a 26 long array of counts ( to represent
	 * multiples of letters easily )
	 */
	public float probability(Word letters, int[] bag) {
		float p = 1.0f;
		int offset = Integer.valueOf('A');
		String s = letters.getWord();

		float bagSize = 0.0f;

		for (int i = 0; i < LETTERS.length(); i++) {
			bagSize += bag[i];
		}

		for( int i = 0; i < LETTERS.length(); i++ )
		{
			if( letters.countKeep[i] > bag[i] ) return 0.0f;
			int f = LetterGameValues.getLetterFrequency((char) ('A'+i));
			for( int j = 0; j < letters.countKeep[i]; j++ )
			{
				p *= f;
				f--;
			}
		}
		
		if (bagSize < 1.0f)
			return 0.0f;
		
		int nCk = 1;
		int k = letters.getLength();
		for( int i = 0; i < k; i++ )
			nCk *= (bagSize-i) / (i+1);
		
/*
		for (int i = 0; i < letters.getLength(); i++) {

			int j = Integer.valueOf(s.charAt(i)) - offset;
			String n = s.substring(0, i) + s.substring(i + 1);

			float p_l = (bag[j] / bagSize);

			bag[j]--;
			p += p_l * probability(new Word(n), bag);
			bag[j]++;
		}
*/
		return p/(float)(nCk);
	}

	/*
	 * This method is called when there is a new letter available for bidding.
	 * bidLetter = the Letter that is being bid on playerBidList = the list of
	 * all previous bids from all players playerList = the class names of the
	 * different players secretState = your secret state (which includes the
	 * score)
	 */
	public int getBid(Letter bidLetter, ArrayList<PlayerBids> playerBidList,
			ArrayList<String> playerList, SecretState secretState) {
		char c[] = new char[currentLetters.size() + 1];
		for (int i = 0; i < currentLetters.size(); i++) {
			c[i + 1] = currentLetters.get(i);
		}
		c[0] = bidLetter.getCharacter();

		bagOfLetters[Integer.valueOf(c[0]) - Integer.valueOf('A')]--;

		String s = new String(c);
		Word prev = new Word(s.substring(1));
		
		Word letters = new Word(s);

		ArrayList<Integer> possible = possibleWords(letters, bagOfLetters);
		int prevwordidx = bestCurrentWord(possible, prev, bagOfLetters);
		Word bestprev = prevwordidx >= 0 ? wordlist.get(prevwordidx)
				: new Word("");
		
		logger.trace("# of possible words: " + possible.size());
		
		possible = prune(possible, letters, bagOfLetters, (bestprev.getScore() + (bestprev.getLength() == 7 ? 50 : 0)));

		logger.trace("# of possible words: " + possible.size());

		float expected = 0.0f;
		float Z = 0.0f;

		for (int wi : possible) {
			Word w = wordlist.get(wi);
			float proba = probability(extraLetters(letters, w), bagOfLetters);
			expected += proba * (w.getScore() + (w.getLength() == 7 ? 50 : 0));
			Z += proba;
		}

		expected /= Z;
		
		logger.trace("Expected score:" + expected + " Z = " + Z );

		float currentBid = bidLetter.getValue();

		ifWonExpected = expected;
		
		if( expected > lastExpected ) currentBid = (int)Math.max(1, bidLetter.getValue()-2+Math.min(5, Math.ceil( expected-lastExpected )));
		if( expected < lastExpected ) currentBid = 0; //+= Math.max(0, 3-Math.ceil( lastExpected-expected ));
		
		
		int bestwordidx = bestWord(possible, letters, bagOfLetters);
		Word bestword = bestwordidx >= 0 ? wordlist.get(bestwordidx)
				: new Word("");

		int score = (bestword.getScore() + (bestword.getLength() == 7 ? 50 : 0));
		Word extras = extraLetters(letters, bestword);

		logger.trace("\n\nBest possible word so far: " + bestword.getWord()
				+ " for " + score + "\nI need "
				+ (extras.getLength() == 0 ? "nothing" : extras.getWord())
				+ " and my chances are: " + 100
				* probability(extras, bagOfLetters) + "%\n");

		spent += currentBid;
		return (int) currentBid;
	}

	private ArrayList<Integer> prune(ArrayList<Integer> possible, Word letters, int[] bag, int score) {
		// TODO prune out words that are useless
		
		// Tracks the potential word indices, and the indicies of the words to remove
		ArrayList<Integer> currWords = new ArrayList<Integer>();
		HashSet<Integer> toRemove = new HashSet<Integer>();
		
		for( int i : possible ){
			
			Word w = wordlist.get(i);
			int wscore = (w.getScore() + (w.getLength() == 7 ? 50 : 0));
			
			if(letters.contains(w)){
				
				currWords.add(i);
				
			}
			
			if( wscore < score )
			{
				toRemove.add(i);
			}
			
		}
		
		for( int i : currWords ){
			Word w = wordlist.get(i);
			int wscore = (w.getScore() + (w.getLength() == 7 ? 50 : 0));
			if( wscore >= score )
			{
				for( int j : currWords ){
					
					if( j != i && !toRemove.contains(i) ){
	
						if( wordlist.get(i).contains(wordlist.get(j)) ){
							
							toRemove.add(j);
								
						}// end containment check
						
					}// end equiv check
					
				}// end inner loop
			}
			
		}// end outer loop
		
		// removes all redundancies
		possible.removeAll(toRemove);
		
		return possible;
		
	}// end prune method

	/*
	 * This method is called after a bid. It indicates whether or not the player
	 * won the bid and what letter was being bid on, and also includes all the
	 * other players' bids.
	 */
	public void bidResult(boolean won, Letter letter, PlayerBids bids) {
		if (won) {
			// logger.trace("My ID is " + myID + " and I won the bid for " +
			// letter);
			currentLetters.add(letter.getCharacter());
			lastExpected = ifWonExpected;
		} else {
			// logger.trace("My ID is " + myID + " and I lost the bid for " +
			// letter);
		}
	}

	/*
	 * This method is called after all the letters have been purchased in the
	 * round. The word that you return will be scored for this round.
	 */
	public String getWord() {
		char c[] = new char[currentLetters.size()];
		for (int i = 0; i < c.length; i++) {
			c[i] = currentLetters.get(i);
		}
		String s = new String(c);
		// logger.trace("Player " + myID + " letters are " + s);
		Word ourletters = new Word(s);
		Word bestword = new Word("");

		int currentBest = 0;
		// iterate through all Words in the list
		// and see which ones we can form
		for (Word w : wordlist) {
			if (ourletters.contains(w)) {
				int score = w.getScore();
				// don't forget the bonus!
				if (w.getLength() == 7)
					score += 50;
				if (score > currentBest) {
					currentBest = score;
					bestword = w;
				}
			}
		}
		logger.trace("My ID is " + myID + " and my word is "
				+ bestword.getWord());
		return bestword.getWord();
	}

	/*
	 * This method is called at the end of the round The ArrayList contains the
	 * scores of all the players, ordered by their ID
	 */
	public void updateScores(ArrayList<Integer> scores) {

	}

}
