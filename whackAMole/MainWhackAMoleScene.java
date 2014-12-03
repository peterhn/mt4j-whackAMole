package inf133.whackAMole;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

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

public class MainWhackAMoleScene extends AbstractScene {
	private AbstractMTApplication pa;
	private MoleHole[][] grid = new MoleHole[4][4];
	private PImage moleImage, startButtonImage;
	private float frameWidth, frameHeight;
	private int molesSmashedCount;
	private MTTextArea molesSmashedTextArea;
	private String imagesPath = "inf133" + AbstractMTApplication.separator + "whackAMole" + AbstractMTApplication.separator + "images" + AbstractMTApplication.separator;

	public MainWhackAMoleScene(AbstractMTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.pa = mtApplication;
		
		
		if (!(MT4jSettings.getInstance().isOpenGlMode() && GLFBO.isSupported(pa))){
			System.err.println("Drawing example can only be run in OpenGL mode on a gfx card supporting the GL_EXT_framebuffer_object extension!");
			return;
		}
		this.registerGlobalInputProcessor(new CursorTracer(mtApplication, this));
		
		moleImage = pa.loadImage(imagesPath +"whackamole.png");
		startButtonImage = pa.loadImage(imagesPath + "startButton.png");
		setupGrid();
		
		//Create window frame
        MTRectangle frame = new MTRectangle(pa,0, 0, pa.width, pa.height);
        frame.removeAllGestureEventListeners();
        //frame.setFillColor(MTColor.GREEN);
        this.getCanvas().addChild(frame);
        
        frameWidth = frame.getWidthXY(TransformSpace.LOCAL);
        frameHeight = frame.getHeightXY(TransformSpace.LOCAL);
        
        MTLine verticalLine = new MTLine(pa, new Vertex(frameWidth/4, 0), new Vertex(frameWidth/4, frameHeight));
        verticalLine.removeAllGestureEventListeners();
        verticalLine.setStrokeWeight(5.0f);
        verticalLine.setStrokeColor(MTColor.RED);
        this.getCanvas().addChild(verticalLine);
        
        MTLine verticalLine2 = new MTLine(pa, new Vertex((int)(frameWidth*(.5)),0), new Vertex((int)(frameWidth*(.5)), frameHeight));
        verticalLine2.removeAllGestureEventListeners();
        verticalLine2.setStrokeWeight(5.0f);
        verticalLine2.setStrokeColor(MTColor.RED);
        this.getCanvas().addChild(verticalLine2);
        
        MTLine verticalLine3 = new MTLine(pa, new Vertex((int)(frameWidth*(.75)),0), new Vertex((int)(frameWidth*(.75)), frameHeight));
        verticalLine3.removeAllGestureEventListeners();
        verticalLine3.setStrokeWeight(5.0f);
        verticalLine3.setStrokeColor(MTColor.RED);
        this.getCanvas().addChild(verticalLine3);
        
        MTLine horizontalLine = new MTLine(pa, new Vertex(0,(int)(frameHeight/4)), new Vertex(frameWidth, (int)(frameHeight/4)));
        horizontalLine.removeAllGestureEventListeners();
        horizontalLine.setStrokeWeight(5.0f);
        horizontalLine.setStrokeColor(MTColor.RED);
        this.getCanvas().addChild(horizontalLine);
        
        MTLine horizontalLine2 = new MTLine(pa, new Vertex(0,(int)(frameHeight*(.5))), new Vertex(frameWidth, (int)(frameHeight*(.5))));
        horizontalLine2.removeAllGestureEventListeners();
        horizontalLine2.setStrokeWeight(5.0f);
        horizontalLine2.setStrokeColor(MTColor.RED);
        this.getCanvas().addChild(horizontalLine2);
        
        MTLine horizontalLine3 = new MTLine(pa, new Vertex(0,(int)(frameHeight*(.75))), new Vertex(frameWidth, (int)(frameHeight*(.75))));
        horizontalLine3.removeAllGestureEventListeners();
        horizontalLine3.setStrokeWeight(5.0f);
        horizontalLine3.setStrokeColor(MTColor.RED);
        this.getCanvas().addChild(horizontalLine3);
       
        final MTImageButton startButton = new MTImageButton(pa, startButtonImage);
        startButton.setPositionGlobal(new Vector3D(frameWidth/2, frameHeight/2));
        startButton.removeAllGestureEventListeners();
        
        startButton.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					startButton.removeFromParent();
					runGame();
				}
				return true;
			}
        });
        
        /*
        startButton.addInputListener(new IMTInputEventListener(){

			@Override
			public boolean processInputEvent(MTInputEvent inEvt) {
				if(inEvt instanceof AbstractCursorInputEvt){
					final AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
					
					if(posEvt.getId() == AbstractCursorInputEvt.INPUT_ENDED){
						startButton.removeFromParent();
						runGame();
					}
					return true;
        		}
				return false;
			}
  
        });
        */
        this.getCanvas().addChild(startButton);   
	}
	
	public void runGame()
	{
		//insert random 5 moles into grid
		molesSmashedCount = 0;
		molesSmashedTextArea = new MTTextArea(pa, frameWidth - 200, 10, 200, 50);
		molesSmashedTextArea.setText("Moles whacked: " + molesSmashedCount);
		this.getCanvas().addChild(molesSmashedTextArea);
		
		for(int i = 0; i < 5; i ++)
		{
			//insert image at
			MoleHole moleHole = findRandomOpenHole();
			drawMole(moleHole.x, moleHole.y);
		}
		//anytime you tap a mole, randomly add a new one where molehole.isOccupied==false
		
		
		
	}
	
	public MoleHole findRandomOpenHole()
	{
		Vector<MoleHole> emptyHoles = new Vector<MoleHole>();
		for(int i = 0; i < grid.length; i++)
		{
			for(int j = 0; j < grid.length; j++)
			{
				if(!(grid[i][j].isOccupied))
				{
					emptyHoles.add(grid[i][j]);
				}
			}
		}
		int randomNumber = (int) (Math.random()*(emptyHoles.size()));
		return emptyHoles.get(randomNumber);
	}
	
	public void drawMole(final int x, final int y)
	{
		final MTImageButton mole = new MTImageButton(pa, moleImage);
		float boxXCenter = (float) ((frameWidth/8.0) + ((float)frameWidth*.25)*(float)x);
		float boxYCenter = (float) ((frameHeight/8.0) + ((float)frameHeight*.25)*(float)y);
		grid[x][y].isOccupied = true;
		mole.setPositionGlobal(new Vector3D(boxXCenter, boxYCenter));
		mole.removeAllGestureEventListeners();
		this.getCanvas().addChild(mole);
		
		/*
		mole.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					removeMole(mole, x,y);
					MoleHole moleHole = findRandomOpenHole();
					drawMole(moleHole.x, moleHole.y);
					molesSmashedCount++;
					molesSmashedTextArea.setText("Moles whacked: " + molesSmashedCount);
				}
				return true;
			}
        });
		*/
		
		mole.addInputListener(new IMTInputEventListener(){
			@Override
			public boolean processInputEvent(MTInputEvent inEvt) {
				if(inEvt instanceof AbstractCursorInputEvt){
					final AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
					
					if(posEvt.getId() != AbstractCursorInputEvt.INPUT_ENDED){
						MoleHole moleHole = findRandomOpenHole();
						removeMole(mole, x,y);
						System.out.println("Mole removed at: (" + x + "," + y + ")");
						drawMole(moleHole.x, moleHole.y);
						molesSmashedCount++;
						molesSmashedTextArea.setText("Moles whacked: " + molesSmashedCount);
					}
					return true;
        		}
				return false;
			}
  
        });
        
	}
	
	public void removeMole(MTImageButton mole, int x, int y){
		grid[x][y].isOccupied = false;
		mole.removeFromParent();
	}
	
	private void setupGrid(){
		for(int i = 0; i< grid.length; i++)
		{
			for(int j = 0; j < grid.length; j++)
			{
				grid[i][j] = new MoleHole(i, j);
			}
		}
	}
	
	/*
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
	*/
	
	/*
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
				//System.out.print(tttGrid[i][j].getBoxOwner().getPlayerMarker());
			}
			System.out.println();
		}
		System.out.println();
	}
	
	private void switchPlayers(){
		if(currentPlayer == player1){
			currentPlayer = player2;
		}
		else{
			currentPlayer = player1;
		}
	}
	*/
	public void onEnter() {}
	
	public void onLeave() {	}
	
}
