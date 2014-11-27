/**
 * Copyright 2014 Matthew Payne

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0
 or in the assets folder of this application.

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.pattmayne.fractalapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;
import java.util.ArrayList;

/**
 * This class creates a canvas and draws a branching fractal animation.
 * The animation consists of lines stemming out from a center point,
 * where each line generates two more lines reaching out toward the edge at a semi-random angle.
 *
 * The user can control certain elements of the animation by pressing buttons in the menu.
 *
 * Created by Matt on 2014-10-04.
 * This class creates a surfaceView,
 */
public class BranchingFractalView extends SurfaceView implements SurfaceHolder.Callback {

    //Variables

    Context thisContext;
    private Random randomizer;

    private Paint paint;

    private float canvasHeight;
    private float canvasWidth;
    private float centerX;
    private float centerY;

    Point centerPoint;
    Point endPoint;

    private ArrayList<Point> startPoints;
    private ArrayList<Point> endPoints;
    private ArrayList<Point> newEndPoints;

    private int iterations = 1;
    private int maxIterations = 4;
    private int speed = 140;
    private int lineLength = 70;
    private int colorTicker = 0;
    private boolean firstTime = true;
    private boolean reset = false;
    private boolean rainbow = false;


    //specific SurfaceHolder variables

    private SurfaceHolder surfaceHolder;
    public BranchingThread thread;

    private Bitmap activeBitmap;
    private Canvas activeCanvas;
    private Matrix identityMatrix;

    //Variables to control the music

    private MusicPlayer musicPlayer;
    private boolean playMusic = false;



    //Constructors


    public BranchingFractalView(Context context) {
        super(context);
        thisContext = context;
        initializeVariables();
    }

    public BranchingFractalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        thisContext = context;
        initializeVariables();
    }

    public BranchingFractalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        thisContext = context;
        initializeVariables();
    }

    //SurfaceHolder methods.
    //The SurfaceHolder allows us to access and manipulate the SurfaceView's default canvas.


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
        makeThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try{
                thread.join();
                retry = false;
            } catch (InterruptedException e){}
        }
    }

    /**
     * Here we create a thread from our own dedicated BranchingThread class.
     * This thread controls the timing of the animation and calls each iteration.
     */
    public void makeThread() {

        activeBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        activeCanvas = new Canvas();
        activeCanvas.setBitmap(activeBitmap);
        identityMatrix = new Matrix();

        thread = new BranchingThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }


    private void initializeVariables()
    {
        startPoints = new ArrayList<Point>();
        endPoints = new ArrayList<Point>();
        newEndPoints = new ArrayList<Point>();
        paint = new Paint();
        randomizer = new Random();
        paint.setColor(0xffe1e1e1);
        paint.setStrokeWidth(3);
        rainbow = false;

        musicPlayer = new MusicPlayer(thisContext);

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
    }



    /**
     * This Point class defines each point at the beginning and end of every line.
     * It also includes information for which direction new points should aim when they are generated from this Point.
     * Each point contains two new directions for two new points.
     */
    public class Point
    {
        //
        public float pointX;
        public float pointY;
        public float direction1;
        public float direction2;

        public Point(float newPointX, float newPointY, float newDirection1, float newDirection2)
        {
            pointX = newPointX;
            pointY = newPointY;
            direction1 = newDirection1;
            direction2 = newDirection2;
        }

        public void setPoints(float newPointX, float newPointY, float newDirection1, float newDirection2) {
            pointX = newPointX;
            pointY = newPointY;
            direction1 = newDirection1;
            direction2 = newDirection2;
        }
    }



    //The animation methods

    /**
     * This method is called for each iteration of the animation.
     * @param canvas
     */
    protected void onDrawSomething(Canvas canvas) {

        //There are a few conditional functions which need to be checked each iteration.
        //I put them in a separate method to maintain readability.
        loopConditionals(canvas);

        //The drawLines method takes the startPoints and calculates new endPoints for each one,
        //then draws the actual lines on an arbitrary canvas (activeCanvas) which we provide.
            for (int i = 0; i < startPoints.size(); i++) {
                drawLines(activeCanvas, i);
                }

        //Remove all the current "startPoints,"
        // and then turn each new "endPoint" into a "startPoint" for the next iteration.
            startPoints.clear();

            for (int i = 0; i < endPoints.size(); i++) {
                startPoints.add(endPoints.get(i));
            }

            endPoints.clear();
            iterations++;

        // Now that we've drawn all the lines to an arbitrary canvas (activeCanvas),
        // we take that activeCanvas and draw it's information onto the SurfaceView's default canvas to be displayed.
            canvas.drawBitmap(activeBitmap, identityMatrix, null);
            surfaceHolder.unlockCanvasAndPost(canvas);
            try {thread.sleep(speed);} catch (InterruptedException e) {}
    }

    /**
     * For every iteration, check all these conditions to see if certain functions should be performed.
     * @param canvas
     */
    private void loopConditionals(Canvas canvas)
    {

        if (firstTime==true) {

            doFirstTimeStuff(canvas);
        }

        if (rainbow)
        {changeColor(false);}

        if (iterations > maxIterations) {
            clearIterations();
        }

        if (startPoints.size() == 0) {
            startPoints.add(new Point(centerX, centerY, getDirection(), getDirection()));
        }

    }

    /**
     * Set up the conditions for the first iteration,
     * or recreate those conditions.
     * @param canvas
     */
    private void doFirstTimeStuff(Canvas canvas)
    {
        canvasHeight = canvas.getHeight();
        canvasWidth = canvas.getWidth();

        centerPoint = new Point((canvasWidth / 2), (canvasHeight / 2), 0, 0);
        endPoint = new Point((canvasWidth / 2), (canvasHeight / 2), 0, 0);

        if (reset==false) {
            //centerX and centerY will be the center of the fractal.
            //starts in the center of the canvas,
            //but onTouch can move it
            centerX = canvasWidth / 2;
            centerY = canvasHeight / 2;
        }

        clearIterations();

        activeCanvas.drawColor(0xff0066ff);
        firstTime=false;
        reset=false;
    }

    /**
     * This method is called for every single Point object in the "startPoints" ArrayList.
     * It draws two lines branching out from a single point,
     * headed in the two directions that are contained in the Point object.
     *
     * @param canvas
     * @param i
     */
    private void drawLines(Canvas canvas, int i)
    {
        newEndPoints.clear();
        //The newEndPoints will be two new Point objects created at locations defined by the values contained in the startPoint's direction variables.
        createNewEndPoints(i);

        //Draw lines from the startPoint to the two newEndPoints
            canvas.drawLine(startPoints.get(i).pointX, startPoints.get(i).pointY, newEndPoints.get(0).pointX, newEndPoints.get(0).pointY, paint);
            canvas.drawLine(startPoints.get(i).pointX, startPoints.get(i).pointY, newEndPoints.get(1).pointX, newEndPoints.get(1).pointY, paint);

        //Add the newEndPoints to the endPoints ArrayList, so that later on we can add all the endPoints to the startPoints ArrayList for the next iteration.
        endPoints.add(newEndPoints.get(0));
        endPoints.add(newEndPoints.get(1));
    }

    /**
     * This method creates two new Points objects at locations defined
     * by the values contained in a startPoint's direction variables.
     *
     * The specified startPoint is supplied by providing this method with an index (int i)
     * which refers to a Point object in the startPoints ArrayList.
     * @param i
     */
    private void createNewEndPoints(int i)
    {
        double xx1=(centerX)+(lineLength*iterations*1.55) * Math.sin(Math.PI*startPoints.get(i).direction1);
        double yy1=(centerY)+(lineLength*iterations*1.55) * Math.cos(Math.PI*startPoints.get(i).direction1);
        double xx2=(centerX)+(lineLength*iterations*1.55) * Math.sin(Math.PI*startPoints.get(i).direction2);
        double yy2=(centerY)+(lineLength*iterations*1.55) * Math.cos(Math.PI*startPoints.get(i).direction2);

        float x1 = (float)xx1;
        float y1 = (float)yy1;
        float x2 = (float)xx2;
        float y2 = (float)yy2;

        newEndPoints.add(new Point(x1,y1,getNewDirection(startPoints.get(i).direction1),getNewDirection(startPoints.get(i).direction1)));
        newEndPoints.add(new Point(x2,y2,getNewDirection(startPoints.get(i).direction2),getNewDirection(startPoints.get(i).direction2)));
    }


    /**
     * This method generates and returns a random number between 0 and 2
     * to plug into a formula which decides the location around a circumference
     * where new Points should be generated.
     *
     * This method is used during a "first iteration," when the primary center Point generates two new Points.
     * The two new Points are free to aim in any direction around the circle.
     * @return float newDirection
     */
    private float getDirection()
    {
        return (randomizer.nextFloat()*2);
    }

    /**
     * This method provides Point objects with a new direction which is based on the direction
     * which created this Point object's location.
     *
     * This new location will be random within a certain range,
     * to make sure that the lines travel generally outward from the center,
     * rather than turning backwards towards the center.
     *
     * @param oldDirection
     * @return
     */
    private float getNewDirection(float oldDirection)
    {
        double directionVariation = oldDirection + randomizer.nextFloat()/(5+iterations)*getSign();
        float newDirection = (float)directionVariation;
        return newDirection;
    }

    /**
     * generates and returns either (1) or (-1) to give a random direction, either left or right along the circumference
     * @return int theSign
     */
    public int getSign()
    {
        int negativityDecider = randomizer.nextInt(2);
        int theSign = 1;

        if(negativityDecider == 1)
        {
            theSign = (-1);
        }
        return theSign;
    }

    /**
     * This method starts the animation back at the center.
     * Otherwise the animation would keep growing, and wreak havoc on the user's computer.
     */
    private void clearIterations()
    {
        iterations = 1;
        startPoints.clear();
        endPoints.clear();
    }


    //Stuff to call from the Activity to effect the animation


    /**
     * When the user touches the screen, this method is called.
     * We use the point that the user touched to move the center of the animation.
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
        }
        changeCenter(x, y);
        return true;
    }

    /**
     * Creates a new center for the animation,
     * based on information fed in through parameters.
     * @param nx
     * @param ny
     */
    public void changeCenter(int nx, int ny)
    {
        centerX += (nx-centerX)/13.1;
        centerY += (ny-centerY)/13.1;
    }


    /**
     * This method is either called when the user uses the menu to request a color change (masterColor == true),
     * or this method is called on every iteration when rainbow is set to true for a constantly changing color pattern (masterColor == false).
     *
     * @param masterColor
     */
    public void changeColor(boolean masterColor){
        if (masterColor) {rainbow=false;}

        colorTicker++;

        if(colorTicker==1){
        paint.setColor(Color.WHITE);}
        else if(colorTicker==2){
            paint.setColor(0xfff71300);}
        else if(colorTicker==3){
            paint.setColor(0xffeae000);}
        else if (colorTicker==4){
            paint.setColor(0xff006a28);}
        else if(colorTicker==5){
            paint.setColor(0xffff65a3);}
        else if(colorTicker==6){
            paint.setColor(Color.BLACK);}
        else if(colorTicker==7){
            paint.setColor(0xff009d0e);}
        else if(colorTicker==8){
            paint.setColor(0xff8fbbff);}
        else if(colorTicker==9){
            paint.setColor(0xffd59200);}
        else if (colorTicker==10) {
            paint.setColor(0xff4100ff);}
        else {
            paint.setColor(0xffe1e1e1);
            colorTicker=0;}
    }

    /**
     * This method is called when the user requests more iterations in the animation from the menu.
     */
    public void bigger(){
        maxIterations ++;
        if(maxIterations>11)
        {maxIterations=12;}
    }

    /**
     * This method is called when the user requests fewer iterations in the animation from the menu.
     */
    public void smaller(){
        maxIterations --;
        if(maxIterations<3)
        {maxIterations=3;}
    }

    /**
     * This method is called when the user requests a faster animation from the menu.
     */
    public void faster() {
        speed-=45;
        if (speed<7)
        {speed=7;}
    }

    /**
     * This method is called when the user requests a slower animation from the menu.
     */
    public void slower() {
        speed += 65;
        if (speed>1000)
        {speed=1000;}
    }

    /**
     * This method is called when the user requests longer lines from the menu.
     */
    public void longerLines()
    {
        lineLength += 9;
        if (lineLength > 370)
        {lineLength = 370;}
    }

    /**
     * This method is called when the user requests shorter lines from the menu.
     */
    public void shorterLines()
    {
        lineLength -= 9;
        if (lineLength < 15)
        {lineLength = 10;}
    }

    /**
     * This method is called when the user requests for the animation to be reset.
     */
    public void resetImage() {
        firstTime=true;
        reset=true;
            }

    /**
     * This method sets the rainbow variable to true,
     * so each iteration will have a different color.
     */
    public void setRainbow() {
        rainbow=true;
    }


    //The next few methods control the music player.
    //I created a dedicated MusicPlayer class to control all the audio.


    /**
     * When the user requests to skip the current audio track.
     */
    public void skipTrack()
    {
        if (playMusic) {
            musicPlayer.skipTrack();
        }
    }

    /**
     * When the user requests for music to be turned on or off.
     */
    public void toggleMusic()
    {
        if (playMusic)
        {
            stopMusic();
        }
        else
        {
            playMusic = true;
            musicPlayer.shuffleTracks();
            musicPlayer.playTrack();
        }
    }

    public void stopMusic()
    {
        playMusic = false;
        musicPlayer.stopMusic();
    }

}