package mmislideshow;

import java.io.File;
import java.io.FileFilter;

import processing.core.PApplet;
import processing.core.PImage;
import SimpleOpenNI.SimpleOpenNI;

public class MMISlideshow extends PApplet {

	private final static int imgWidth = 900;
	private final static int imgHeight = 600;
	
	private File[] imageFiles;
	private PImage img;
	private int currentImgIdx = 0;
	
	public SimpleOpenNI  context;

	public void setup()
	{
		// context = new SimpleOpenNI(this);
		context = new SimpleOpenNI(this,SimpleOpenNI.RUN_MODE_MULTI_THREADED);

		// enable depthMap generation 
		context.enableDepth();

		context.setMirror(true);
		
		// enable skeleton generation for all joints
		context.enableUser(SimpleOpenNI.SKEL_PROFILE_ALL);
		
		background(180,180,180);

		stroke(0,0,255);
		strokeWeight(3);
		smooth();

		size(imgWidth + context.depthWidth()/2, Math.max(imgHeight,context.depthHeight()/2)); 
		
		loadImageFiles();
		
		//load first img by default
		img = loadImage(imageFiles[currentImgIdx].getAbsolutePath());		
	}

	public void draw()
	{
		
		// update the cam
		context.update();

		// draw depthImageMap
		image(context.depthImage(),imgWidth,0,context.depthWidth()/2,context.depthHeight()/2);
		
		image(img,0,0,imgWidth, imgHeight);
		
		// draw the skeleton if it's available
//		if(context.isTrackingSkeleton(1))
//			drawSkeleton(1);
	}

	public void keyPressed() {
		if(keyCode==LEFT) {
			currentImgIdx = Math.max(0, currentImgIdx-1);
		}
		else if(keyCode==RIGHT) {
			currentImgIdx = Math.min(imageFiles.length-1, currentImgIdx+1);
		}
		img = loadImage(imageFiles[currentImgIdx].getAbsolutePath());
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
	
	
	// draw the skeleton with the selected joints
	private void drawSkeleton(int userId)
	{
		// to get the 3d joint data
		/*
		  PVector jointPos = new PVector();
		  context.getJointPositionSkeleton(userId,SimpleOpenNI.SKEL_NECK,jointPos);
		  println(jointPos);
		 */

		context.drawLimb(userId, SimpleOpenNI.SKEL_HEAD, SimpleOpenNI.SKEL_NECK);
		
		
		context.drawLimb(userId, SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_LEFT_SHOULDER);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER, SimpleOpenNI.SKEL_LEFT_ELBOW);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_ELBOW, SimpleOpenNI.SKEL_LEFT_HAND);

		context.drawLimb(userId, SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_RIGHT_SHOULDER);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_RIGHT_ELBOW);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_ELBOW, SimpleOpenNI.SKEL_RIGHT_HAND);

		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER, SimpleOpenNI.SKEL_TORSO);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_TORSO);

		context.drawLimb(userId, SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_LEFT_HIP);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_HIP, SimpleOpenNI.SKEL_LEFT_KNEE);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_KNEE, SimpleOpenNI.SKEL_LEFT_FOOT);

		context.drawLimb(userId, SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_RIGHT_HIP);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_HIP, SimpleOpenNI.SKEL_RIGHT_KNEE);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_KNEE, SimpleOpenNI.SKEL_RIGHT_FOOT);  
	}

	// -----------------------------------------------------------------
	// SimpleOpenNI events

	public void onNewUser(int userId)
	{
		println("onNewUser - userId: " + userId);
		println("  start pose detection");

		context.startPoseDetection("Psi",userId);
	}

	public void onLostUser(int userId)
	{
		println("onLostUser - userId: " + userId);
	}

	public void onStartCalibration(int userId)
	{
		println("onStartCalibration - userId: " + userId);
	}

	public void onEndCalibration(int userId, boolean successfull)
	{
		println("onEndCalibration - userId: " + userId + ", successfull: " + successfull);

		if (successfull) 
		{ 
			println("  User calibrated !!!");
			context.startTrackingSkeleton(userId); 
		} 
		else 
		{ 
			println("  Failed to calibrate user !!!");
			println("  Start pose detection");
			context.startPoseDetection("Psi",userId);
		}
	}

	public void onStartPose(String pose,int userId)
	{
		println("onStartPose - userId: " + userId + ", pose: " + pose);
		println(" stop pose detection");

		context.stopPoseDetection(userId); 
		context.requestCalibrationSkeleton(userId, true);

	}

	public void onEndPose(String pose,int userId)
	{
		println("onEndPose - userId: " + userId + ", pose: " + pose);
	}

	
	
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { mmislideshow.MMISlideshow.class.getName() });
	}

}
