package inf133.ticTacToeGame;

import org.mt4j.util.math.Vector3D;

import inf133.ticTacToeGame.Player;

public class GridBox {
	
	private boolean isOccupied;
	private Player boxOwner;
	public Vector3D boxCenter;
	
	public GridBox(){
		isOccupied = false;
		boxOwner = new Player('-');
	}
	
	public boolean isOccupied(){
		return isOccupied;
	}
	
	public void setBoxOwner(Player player){
		boxOwner = player;
		isOccupied = true;
	}
	
	public Player getBoxOwner(){
		return boxOwner;
	}
}
