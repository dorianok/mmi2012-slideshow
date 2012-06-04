package mmislideshow;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

public class MMISlideshow extends PApplet {

	private final static int canvasWidth = 900;
	private final static int canvasHeight = 600;
	private final static int statusBarHeight = 25;
	private final static float scalingIncrement = 0.25f;
	private final static float maxScalingFactor = 3.0f;
	private final static float translationIncrement = 0.05f;
	private final static int minTimeBetweenActions = 750; //in ms
	
	private Color background = Color.BLACK;
	private float imgRatio = (float)canvasWidth/canvasHeight;
	private float rotationAngle = 0;
	private float scalingFactor = 1.0f;
	private float translationX, translationY;
	private long timeLastAction;
	
	private boolean inputActivated = false;
	
	private enum InteractionMode
	{
	    KEYBOARD,
	    GESTURE,
	    SPEECH,
	    GESTURE_AND_SPEECH;
	}
	private InteractionMode mode = InteractionMode.KEYBOARD; 
	
	private File[] imageFiles;
	private PImage img;
	private int currentImgIdx = 0;
	
	public void setup()
	{

		size(900, 600+25); 
			 
		smooth();

		loadImageFiles();
		
		//load first img by default
		img = loadImage(imageFiles[currentImgIdx].getAbsolutePath());		
	}

	public void draw()
	{
		background(background.getRGB());
		
		pushMatrix(); 
		
		translate(canvasWidth/2, canvasHeight/2);
		translate(translationX, translationY);
		rotate(rotationAngle);
		scale(scalingFactor);
		//System.out.println(rotationAngle + " " + rotationAngle*(180/PI));

		float sizeX, sizeY;
		if(round(rotationAngle/(PI/2))%2==0) { //rotated by 0¡, +-180¡, +- 360¡, etc 
			if(img.width > img.height) { //landscape image 
				sizeX = canvasWidth;
				sizeY = canvasWidth/imgRatio;
			}
			else { //portrait image
				sizeX = canvasHeight/imgRatio;
				sizeY = canvasHeight;
			}
		}
		else { //rotated by +-45¡, +- 135¡, etc 
			if(img.width > img.height) { //landscape image 
				sizeX = canvasHeight;
				sizeY = canvasHeight/imgRatio;
			}
			else { //portrait image
				sizeX = canvasWidth/imgRatio;
				sizeY = canvasWidth;
			}
		}
		
		
		
		image(img, -sizeX/2, -sizeY/2, sizeX, sizeY);
		
		popMatrix(); 
		
		drawStatusBar();
		
		// draw the skeleton if it's available
//		if(context.isTrackingSkeleton(1))
//			drawSkeleton(1);
	}


	private void drawStatusBar() {		
		if(inputActivated) {
			fill(0,255,0);
			rect(0,canvasHeight,canvasWidth,statusBarHeight);
			fill(0,0,0);
		}
		else {
			fill(background.getRGB());
			rect(0,canvasHeight,canvasWidth,statusBarHeight);
			fill(255,255,255);
		}
		textSize(18);
		text("Image " + (currentImgIdx+1) + "/" + imageFiles.length  , 20, canvasHeight + statusBarHeight/2 + 5);
	}
	
	public void keyPressed() {
		
		int timeBetweenLastAction = (int)(System.currentTimeMillis()-timeLastAction);
		
		if(keyCode==KeyEvent.VK_0) {
			System.out.println("gesture mode ACTIVATED");
			inputActivated = true;
			return;
		}
		System.out.println("keyPressed: " + keyCode + " " + inputActivated);
		System.out.println(timeBetweenLastAction);
		if(!inputActivated)
			return;
		
		if(keyCode==KeyEvent.VK_P) {
			if(timeBetweenLastAction > minTimeBetweenActions) {
				currentImgIdx = Math.max(0, currentImgIdx-1);
				img = loadImage(imageFiles[currentImgIdx].getAbsolutePath());
				scalingFactor = 1.0f;
				translationX = translationY = rotationAngle = 0;
			}
			timeLastAction = System.currentTimeMillis();
		}
		else if(keyCode==KeyEvent.VK_N) {
			if(timeBetweenLastAction > minTimeBetweenActions) {
				currentImgIdx = Math.min(imageFiles.length-1, currentImgIdx+1);
				img = loadImage(imageFiles[currentImgIdx].getAbsolutePath());
				scalingFactor = 1.0f;
				translationX = translationY = rotationAngle = 0;
			}
			timeLastAction = System.currentTimeMillis();
		}
		else if(keyCode==KeyEvent.VK_R) {
			if(timeBetweenLastAction > minTimeBetweenActions) {
				rotationAngle += PI/2;
				scalingFactor = 1.0f;
				translationX = translationY = 0;
			}
			timeLastAction = System.currentTimeMillis();
		}
		else if(keyCode==KeyEvent.VK_L) {
			if(timeBetweenLastAction > minTimeBetweenActions) {
				rotationAngle -= PI/2;
				scalingFactor = 1.0f;
				translationX = translationY = 0;
			}
			timeLastAction = System.currentTimeMillis();
		}
		else if(keyCode==KeyEvent.VK_J) {
			scalingFactor *= (1+scalingIncrement);
			scalingFactor = Math.min(scalingFactor, maxScalingFactor);
			//System.out.println(translationX + " " + getCurrentImageWidth() + " " + scalingFactor);
		}
		else if(keyCode==KeyEvent.VK_K) {
			scalingFactor /= (1+scalingIncrement);
			scalingFactor = Math.max(scalingFactor, 1.0f);
			
			float imgW = getCurrentImageWidth();
			float imgH = getCurrentImageHeight();
			if(imgW > canvasWidth) {
				translationX = Math.min(translationX, ((imgW - canvasWidth)/2));
				translationX = Math.max(translationX, -((imgW - canvasWidth)/2));
			}
			else {
				translationX = 0;
			}
			translationY = Math.min(translationY, ((imgH - canvasHeight)/2));
			translationY = Math.max(translationY, -((imgH - canvasHeight)/2));
			
			//System.out.println(translationX + " " + getCurrentImageWidth() + " " + scalingFactor);
		}
		else if(keyCode==KeyEvent.VK_A) {
			float imgW = getCurrentImageWidth();
			if(imgW > canvasWidth) {
				translationX += canvasWidth*translationIncrement;
				translationX = Math.min(translationX, ((imgW - canvasWidth)/2));
			}
			//System.out.println(translationX + " " + getCurrentImageWidth() + " " + scalingFactor);
		}
		else if(keyCode==KeyEvent.VK_D) {
			float imgW = getCurrentImageWidth();
			if(imgW > canvasWidth) {
				translationX -= canvasWidth*translationIncrement;
				translationX = Math.max(translationX, -((imgW - canvasWidth)/2));
			}
		}
		else if(keyCode==KeyEvent.VK_W) {
			translationY += canvasHeight*translationIncrement;
			translationY = Math.min(translationY, ((getCurrentImageHeight() - canvasHeight)/2));
			//System.out.println(translationX + " " + canvasWidth*scalingFactor + " " + scalingFactor);
		}
		else if(keyCode==KeyEvent.VK_S) {
			translationY -= canvasHeight*translationIncrement;
			translationY = Math.max(translationY, -((getCurrentImageHeight() - canvasHeight)/2));
		}
		
		

	}
	
	@Override
	public void keyReleased() {
		if(keyCode==KeyEvent.VK_0) {
			System.out.println("gesture mode DEACTIVATED");
			inputActivated = false;
		}
	}


	private float getCurrentImageWidth() {
		if(isImageInPortraitOrientation())
			return (canvasHeight/imgRatio) * scalingFactor;
		else 
			return canvasWidth*scalingFactor;
	}
	private float getCurrentImageHeight() {
			return canvasHeight*scalingFactor;
	}
	
	private boolean isImageInPortraitOrientation() {
		if(round(rotationAngle/(PI/2))%2==0 && img.width > img.height)
			return false;
		return true;
	}
	
	
	private void loadImageFiles() {
		File imageDir = new File(dataPath("images/"));
		imageFiles = imageDir.listFiles( new FileFilter() {
		    @Override
		    public boolean accept( File pathname ) {
		        String name = pathname.getName();
		        return name.endsWith( ".png" ) || name.endsWith( ".jpg" );
		    }
		} );
		
	}
	
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { mmislideshow.MMISlideshow.class.getName() });
	}

}
