package inf133.ticTacToeGame;

import org.mt4j.MTApplication;

public class StartTicTacToe extends MTApplication{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public static void main(String args[]){
		initialize();
	}
	
	//@Override
	public void startUp(){
		this.addScene(new MainTicTacToeScene(this, "Main drawing scene"));
//		DrawingScene scene = new DrawingScene(this, "scene");
//		scene.setClear(false);
//		this.addScene(scene);
//		this.frameRate(50);
	}
	
}