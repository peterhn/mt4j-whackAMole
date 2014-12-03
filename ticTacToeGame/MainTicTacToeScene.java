package inf133.ticTacToeGame;

import inf133.ticTacToeGame.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.mt4j.AbstractMTApplication;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.MTEllipse;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.shapes.MTRoundRectangle;
import org.mt4j.components.visibleComponents.widgets.MTColorPicker;
import org.mt4j.components.visibleComponents.widgets.MTSceneTexture;
import org.mt4j.components.visibleComponents.widgets.MTSlider;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.components.visibleComponents.shapes.MTLine;
import org.mt4j.components.visibleComponents.widgets.buttons.MTImageButton;
import org.mt4j.input.IMTInputEventListener;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.sceneManagement.IPreDrawAction;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.opengl.GLFBO;

import processing.core.PImage;

public class MainTicTacToeScene extends AbstractScene {
	private AbstractMTApplication pa;
	private GridBox[][] tttGrid = new GridBox[3][3];
	final int maxMoves = 9;
	private int currentMoves;
	private Player currentPlayer, winningPlayer;
	private Player player1, player2;
	private PImage xImage, oImage;
	
	private String imagesPath = "inf133" + AbstractMTApplication.separator + "ticTacToeGame" + AbstractMTApplication.separator + "images" + AbstractMTApplication.separator;

	
	public MainTicTacToeScene(AbstractMTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.pa = mtApplication;
		
		
		if (!(MT4jSettings.getInstance().isOpenGlMode() && GLFBO.isSupported(pa))){
			System.err.println("Drawing example can only be run in OpenGL mode on a gfx card supporting the GL_EXT_framebuffer_object extension!");
			return;
		}
		this.registerGlobalInputProcessor(new CursorTracer(mtApplication, this));
		
		player1 = new Player('X');
		player2 = new Player('O');
		currentPlayer = player1;
		
		currentMoves = 0;
		setupGrid();
		
		xImage = pa.loadImage(imagesPath +"x-image.png");
		oImage = pa.loadImage(imagesPath +"o-image.png");
		
		
		//Create window frame
        MTRectangle frame = new MTRectangle(pa,0, 0, pa.width, pa.height);
        frame.removeAllGestureEventListeners();
        this.getCanvas().addChild(frame);
        
        MTLine line = new MTLine(pa, new Vertex(pa.width/3,0), new Vertex(pa.width/3, pa.height));
        line.removeAllGestureEventListeners();
        line.setStrokeWeight(5.0f);
        line.setStrokeColor(MTColor.RED);
        this.getCanvas().addChild(line);
        
        MTLine line2 = new MTLine(pa, new Vertex((int)(pa.width*(2.0/3.0)),0), new Vertex((int)(pa.width*(2.0/3.0)), pa.height));
        line2.removeAllGestureEventListeners();
        line2.setStrokeWeight(5.0f);
        line2.setStrokeColor(MTColor.RED);
        this.getCanvas().addChild(line2);
        
        MTLine line3 = new MTLine(pa, new Vertex(0,(int)(pa.height*(1.0/3.0))), new Vertex(pa.width, (int)(pa.height*(1.0/3.0))));
        line3.removeAllGestureEventListeners();
        line3.setStrokeWeight(5.0f);
        line3.setStrokeColor(MTColor.RED);
        this.getCanvas().addChild(line3);
        
        MTLine line4 = new MTLine(pa, new Vertex(0,(int)(pa.height*(2.0/3.0))), new Vertex(pa.width, (int)(pa.height*(2.0/3.0))));
        line4.removeAllGestureEventListeners();
        line4.setStrokeWeight(5.0f);
        line4.setStrokeColor(MTColor.RED);
        this.getCanvas().addChild(line4);
        
        this.getCanvas().addInputListener(new IMTInputEventListener(){

			@Override
			public boolean processInputEvent(MTInputEvent inEvt) {
				if(inEvt instanceof AbstractCursorInputEvt){
					final AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
					
					if(posEvt.getId() == AbstractCursorInputEvt.INPUT_ENDED){
					makeMove(posEvt.getX(), posEvt.getY());
					}
					return true;
        		}
				return false;
			}
  
        });
		
	}
	
	
	
	public void makeMove(float x, float y){
		GridBox selectedGridBox = getBoxInRegion(x,y);
		if(currentMoves < 9 && !selectedGridBox.isOccupied()){
			selectedGridBox.setBoxOwner(currentPlayer);
			currentMoves++;
			MTRectangle playerMarker;
			if(currentPlayer.getPlayerMarker() == 'X'){
				playerMarker = new MTRectangle(pa, xImage);
				
			}
			else{
				playerMarker = new MTRectangle(pa, oImage);
			}
			playerMarker.setPositionGlobal(selectedGridBox.boxCenter);
			
			this.getCanvas().addChild(playerMarker);
			
			if(isWinner()){
				currentMoves = 9;
				MTTextArea textArea = new MTTextArea(pa.width/2 - 150, pa.height/2, 300, 100, pa);
				textArea.setText("THE WINNER IS PLAYER " + winningPlayer.getPlayerMarker());
				
				this.getCanvas().addChild(textArea);
				System.out.println("Winner");
			}
		}
		this.switchPlayers();
		System.out.println(x + "," + y);
		printBoard();
	}
	
	public GridBox getBoxInRegion(float x, float y){
		if(x>=0.0 && x<=((float)pa.width)/3.0 && y>=0.0 && y<=((float)pa.height)/3.0)
		{
			tttGrid[0][0].boxCenter = new Vector3D((float)(pa.width/6.0), (float)(pa.height/6.0));
			return tttGrid[0][0];
		}
		else if(x>((float)pa.width)/3.0 && x<=(((float)pa.width)/3.0)*2 && y>=0.0 && y<=((float)pa.height)/3.0)
		{
			tttGrid[0][1].boxCenter = new Vector3D((float)(pa.width/6.0)*3, (float)(pa.height/6.0));
			return tttGrid[0][1];
		}
		else if(x>(((float)pa.width)/3.0)*2 && x<=(float)pa.width && y>=0.0 && y<=((float)pa.height)/3.0)
		{
			tttGrid[0][2].boxCenter = new Vector3D((float)(pa.width/6.0)*5, (float)(pa.height/6.0));
			return tttGrid[0][2];
		}
		else if(x>=0 && x<=((float)pa.width)/3.0 && y>((float)pa.height)/3.0 && y<=(((float)pa.height)/3.0)*2)
		{
			tttGrid[1][0].boxCenter = new Vector3D((float)(pa.width/6.0), (float)(pa.height/6.0)*3);
			return tttGrid[1][0];
		}
		else if(x>((float)pa.width)/3.0 && x<=(((float)pa.width)/3.0)*2 && y>((float)pa.height)/3.0 && y<=(((float)pa.height)/3.0)*2)
		{
			tttGrid[1][1].boxCenter = new Vector3D((float)(pa.width/6.0)*3, (float)(pa.height/6.0)*3);
			return tttGrid[1][1];
		}
		else if(x>(((float)pa.width)/3.0)*2 && x<=(float)pa.width && y>((float)pa.height)/3.0 && y<=(((float)pa.height)/3.0)*2)
		{
			tttGrid[1][2].boxCenter = new Vector3D((float)(pa.width/6.0)*5, (float)(pa.height/6.0)*3);
			return tttGrid[1][2];
		}
		else if(x>=0 && x<=((float)pa.width)/3.0 && y>(((float)pa.height)/3.0)*2 && y<=(float)pa.height)
		{
			tttGrid[2][0].boxCenter = new Vector3D((float)(pa.width/6.0), (float)(pa.height/6.0)*5);
			return tttGrid[2][0];
		}
		else if(x>((float)pa.width)/3.0 && x<=(((float)pa.width)/3.0)*2 && y>(((float)pa.height)/3.0)*2 && y<=(float)pa.height)
		{
			tttGrid[2][1].boxCenter = new Vector3D((float)(pa.width/6.0)*3, (float)(pa.height/6.0)*5);
			return tttGrid[2][1];
		}
		else
		{
			tttGrid[2][2].boxCenter = new Vector3D((float)(pa.width/6.0)*5, (float)(pa.height/6.0)*5);
			return tttGrid[2][2];
		}
	}
	
	public boolean isWinner(){
		//boolean isWinner = false;
		//Checks for winner in horizontal row
		for(int i = 0; i < tttGrid.length; i++){
			Player[] playersInRow = new Player[3];
			for(int j = 0; j< tttGrid.length; j++){
				playersInRow[j] = tttGrid[i][j].getBoxOwner();
			}
			//checks if same box owner
			if(playersInRow[0].equals(playersInRow[1]) && playersInRow[0].equals(playersInRow[2]) && playersInRow[1].equals(playersInRow[2])){
				winningPlayer = playersInRow[0];
				return true;
			}
		}
		//Checks winner in vertical row
		for(int i = 0; i < tttGrid.length; i++){
			Player[] playersInCol= new Player[3];
			for(int j = 0; j< tttGrid.length; j++){
				playersInCol[j] = tttGrid[j][i].getBoxOwner();
			}
			//checks if same box owner
			if(playersInCol[0].equals(playersInCol[1]) && playersInCol[0].equals(playersInCol[2]) && playersInCol[1].equals(playersInCol[2])){
				winningPlayer = playersInCol[0];
				return true;
			}
		}
		
		//checks winner in diagonals
		Player[] playersInDiag = new Player[3];
		for(int i = 0; i < tttGrid.length; i++){
			playersInDiag[i] = tttGrid[i][i].getBoxOwner();
		}
		if(playersInDiag[0].equals(playersInDiag[1]) && playersInDiag[0].equals(playersInDiag[2]) && playersInDiag[1].equals(playersInDiag[2])){
			winningPlayer = playersInDiag[0];
			return true;
		}
		
		//checks winner in reverse diag
		int reverseDiagCount = 2;
		for(int i = 0; i < tttGrid.length; i++){
			playersInDiag[i] = tttGrid[i][reverseDiagCount].getBoxOwner();
			reverseDiagCount--;
		}
		if(playersInDiag[0].equals(playersInDiag[1]) && playersInDiag[0].equals(playersInDiag[2]) && playersInDiag[1].equals(playersInDiag[2])){
			winningPlayer = playersInDiag[0];
			return true;
		}
		
		return false;
	}
	
	public void printBoard()
	{
		for(int i = 0; i< tttGrid.length; i++)
		{
			for(int j = 0; j < tttGrid.length; j++)
			{
				System.out.print(tttGrid[i][j].getBoxOwner().getPlayerMarker());
			}
			System.out.println();
		}
		System.out.println();
	}
	
	private void setupGrid(){
		for(int i = 0; i< tttGrid.length; i++)
		{
			for(int j = 0; j < tttGrid.length; j++)
			{
				tttGrid[i][j] = new GridBox();
			}
		}
	}
	
	private void switchPlayers(){
		if(currentPlayer == player1){
			currentPlayer = player2;
		}
		else{
			currentPlayer = player1;
		}
	}
	
	public void onEnter() {}
	
	public void onLeave() {	}
	
}
