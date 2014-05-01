package com.icam160.opengl;

import com.icam160.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity {
	
	private GLSurfaceView glSurfaceView;
	private boolean rendererSet = false;
	
	private Camera mCamera;
    private CameraPreview mPreview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		glSurfaceView = new GLSurfaceView(this);
		
		setContentView(R.layout.fragment_main);
		
		//create an instance of Camera
      	mCamera = getCameraInstance();
      //create our Preview view and set it as the content of our activity
      	mPreview = new CameraPreview(this, mCamera); 	
      	FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
      	preview.addView(glSurfaceView);
   		preview.addView(mPreview);
   		setCameraDisplayOrientation(this, 0, mCamera);
		
		//the next two lines is how to set the background transparent
		glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		
		// Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(this.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        
        final MyGLRenderer mRenderer = new MyGLRenderer(this);
        
        if (supportsEs2)
        {
        	// Request an OpenGL ES 2.0 compatible context.
        	glSurfaceView.setEGLContextClientVersion(2);

        	// Assign our renderer. 
        	glSurfaceView.setRenderer(mRenderer);
        	rendererSet = true;
        }
        else
        {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
        	Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
        	Toast.LENGTH_LONG).show();
            return;
        }
        
        glSurfaceView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {           
                    // Convert touch coordinates into normalized device
                    // coordinates, keeping in mind that Android's Y
                    // coordinates are inverted.
                    final float normalizedX = 
                        (event.getX() / (float) v.getWidth()) * 2 - 1;
                    final float normalizedY = 
                        -((event.getY() / (float) v.getHeight()) * 2 - 1);
                    
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                            	mRenderer.handleTouchPress(
                                    normalizedX, normalizedY);
                            }
                        });
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                            	mRenderer.handleTouchDrag(
                                    normalizedX, normalizedY);
                            }
                        });
                    } 
                    else if (event.getAction() == MotionEvent.ACTION_UP) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                            	mRenderer.handleTouchUp(
                                    normalizedX, normalizedY);
                            }
                        });
                    }

                    return true;                    
                } else {
                    return false;
                }
            }
        });
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    if (rendererSet) { 
	    	glSurfaceView.onPause();
	    } 
	    
	    releaseCamera();
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    if (rendererSet) { 
	    	glSurfaceView.onResume();
	    } 
	    
	    if (mCamera == null){
        	mCamera = getCameraInstance();
        	mPreview = new CameraPreview(this, mCamera);
        	FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        	//preview.removeAllViews();
        	preview.addView(mPreview);
        	preview.addView(glSurfaceView);
        	setCameraDisplayOrientation(this, 0, mCamera);
        }
	}
	
	//get an instance of the Camera object
  	public static Camera getCameraInstance() {
  		Camera c = null;
  		try {
  			c = Camera.open(0); //try to get the back camera
  		}
  		catch(Exception e){
  			//Camera is not available
  		}
  		return c; //returns null if camera is unavailable
  	}
  	
  	 private void releaseCamera(){
 	    if (mCamera != null){
 	        mCamera.release();        // release the camera for other applications
 	        mCamera = null;
 	    }
 	}
  	
  	public static void setCameraDisplayOrientation(Activity activity,
            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


}
