package com.icam160.opengl;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.icam160.R;
//import com.icam160.objects.Mallet;
import com.icam160.objects.Puck;
import com.icam160.objects.Table;
import com.icam160.programs.ColorShaderProgram;
import com.icam160.programs.TextureShaderProgram;
import com.icam160.util.MatrixHelper;
import com.icam160.util.TextureHelper;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.widget.Toast;

public class MyGLRenderer implements Renderer {	
	private final Context context;
	
	private final float[] projectionMatrix = new float[16];
	private final float[] modelMatrix = new float[16];
	private final float[] viewMatrix = new float[16];
	private final float[] viewProjectionMatrix = new float[16]; 
	private final float[] modelViewProjectionMatrix = new float[16];

	private Table table;
	private Puck puck;
//	private Mallet mallet;
	
	private TextureShaderProgram textureProgram;
	private ColorShaderProgram colorProgram;
	private int texture;
	
	boolean eli = false;
	int rita = 0;
	float ee = 0.1f;
	float positionY;
	float cameraRotate;
	
	public MyGLRenderer(Context context) { 
    	this.context = context; 
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {	
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		table = new Table();
		puck = new Puck(0.06f, 0.02f, 32);
//		mallet = new Mallet(0.08f, 0.15f, 32);
		
		textureProgram = new TextureShaderProgram(context);
		colorProgram = new ColorShaderProgram(context);
		
		texture = TextureHelper.loadTexture(context, R.drawable.air_hockey_surface);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		glViewport(0, 0, width, height);
		
		MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);
		/*
		setIdentityM(modelMatrix, 0);
		translateM(modelMatrix, 0, 0f, 0f, -2.5f);
		rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);
		
		final float[] temp = new float[16];
		multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0); 
		System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
		*/
		
		/*
		 * setLookAtM( float[] rm, int rmOffset, float eyeX, float eyeY, float eyeZ, float centerX, 
		 *				  			    float centerY, float centerZ, float upX, float upY, float upZ)
		*/
		setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		glClear(GL_COLOR_BUFFER_BIT);
		if(eli){
			System.out.println("eli I'm in");
			setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, cameraRotate, 0f, 0f, 0f, 1f, 0f);
		}
		multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
		
		 // Draw the table.
        positionTableInScene();
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, texture);
        table.bindData(textureProgram);
        table.draw();
/*	    
	 // Draw the mallets.
        positionObjectInScene(0f, mallet.height / 2f, -0.4f);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f);
        mallet.bindData(colorProgram);
        mallet.draw();

        positionObjectInScene(0f, mallet.height / 2f, 0.4f);
        colorProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f);
        // Note that we don't have to define the object data twice -- we just
        // draw the same mallet again but in a different position and with a
        // different color.
        mallet.draw();
	*/    
	    // Draw the puck #1.
        positionObjectInScene(0f, puck.height / 2f, 0f);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 1f);
        puck.bindData(colorProgram);
        puck.draw();
        
        // Draw the puck #2.
        positionObjectInScene(0.3f, puck.height / 2f, 0f);
        colorProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f);
     // Note that we don't have to define the object data twice -- we just
        // draw the same puck again but in a different position and with a
        // different color.
        puck.draw();
        
        if(eli){
        	System.out.println("eli y: " + positionY);
   		 positionObjectInScene(0.1f, positionY, 0f);
   	        colorProgram.setUniforms(modelViewProjectionMatrix, 1f, 1f, 1f);
   	     // Note that we don't have to define the object data twice -- we just
   	        // draw the same puck again but in a different position and with a
   	        // different color.
   	        puck.draw();
        }
	}
	
	private void positionTableInScene() {
		// The table is defined in terms of X & Y coordinates, so we rotate it // 90 degrees to lie flat on the XZ plane.
		setIdentityM(modelMatrix, 0);
		rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f); 
		multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
	}

	private void positionObjectInScene(float x, float y, float z) { 
		setIdentityM(modelMatrix, 0);
		translateM(modelMatrix, 0, x, y, z); 
		multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
	}
	
	public void handleTouchPress(float normalizedX, float normalizedY) {
		System.out.println("eli down");
		eli = true;
	}
	public void handleTouchDrag(float normalizedX, float normalizedY) { 
		//Toast.makeText(this.context, "ddrag",Toast.LENGTH_SHORT).show();
		System.out.println("eli drag");
		positionY = normalizedY;
		cameraRotate = normalizedX;
	}
	
	public void handleTouchUp(float normalizedX, float normalizedY) { 
		//Toast.makeText(this.context, "ddrag",Toast.LENGTH_SHORT).show();
		System.out.println("eli up");
		//eli =+ 1;
	}

}
