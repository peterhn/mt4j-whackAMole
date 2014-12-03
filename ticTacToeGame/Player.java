package inf133.ticTacToeGame;

public class Player {
	private char playerMarker;
	public Player(char playerChar){
		playerMarker = playerChar;
	}
	
	public char getPlayerMarker(){
		return playerMarker;
	}
}
