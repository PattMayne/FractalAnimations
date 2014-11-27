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
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import java.util.Random;
import java.util.ArrayList;

/**
 * Created by Matt on 2014-10-11.
 * This class creates a surface to draw on, then draws triangles-within-triangles which grow larger (or smaller) with each repeating iteration in a running Thread.
 * When the triangles get to a certain size they are released from the ArrayList of triangle-objects, and a new one is created.
 *
 * I've created the options for two different kinds of triangles, with varying speeds and spin-rates, plus weird options for "crazy mode" and "seizure mode."
 *
 * This class contains two inner classes: a RightTriangle class for right-angled triangle objects,
 * and an EquilateralTriangle class for equilateral triangles.
 *
 * The Triangles are drawn by creating circles and calculating three points along those circles which are the corners of the triangles.
 * The animation logic results from incrementing the size of the circles, and altering the angles of the points along the circles.
 */
public class TriangleFractalView extends SurfaceView implements SurfaceHolder.Callback {


    //PERMANENT and universal variables


    //This canvas, bitmap, and Matrix are external to the SurfaceView's automatically created canvas.
    //We need this extra canvas and bitmap to draw on and manipulate, which WON'T be cleared after each loop,
    // and then we paste this bitmap onto the SurfaceView's default canvas after every iteration.
    private Bitmap activeBitmap;
    private Canvas activeCanvas;
    private Matrix identityMatrix;

    private SurfaceHolder surfaceHolder;
    private Random randomizer;
    private TriangleThread thread;

    private float canvasHeight;
    private float canvasWidth;

    private Context thisContext;
    private Paint paint;

    private int[] colors = {0xffe1e1e1, 0xff000000, 0xff635ea7, 0xfff78e00, 0xffff1800, 0xffeae000, 0xffff5a88, 0xff33823a};
    private int[] seizureColors = {Color.YELLOW,Color.YELLOW,0xffFF69B4,0xffFF69B4,0xff6B8E23,0xff6B8E23};

    private int colorTicker = 0;
    private int seizureColorTicker=0;


    //Universal (equilateral OR right-angled) Changeable variables
    //Includes mode-changers


    //these two center variables will always be the center of the circle within which the triangles are drawn.
    private float centerX;
    private float centerY;

    private boolean firstIteration = true;
    private int iterations = 1;

    //The actual spin is decided by dividing the iterations by the "spin" variable... so a lower spin means faster actual spin...
    //For zero spin I use (spin = iterations) in the algorithm.
    private int[] spinPhaseArray = {2, 5, 11, 29, 41, 57};
    private int spinPhaseSelector = 3;
    private double spin = spinPhaseArray[spinPhaseSelector];
    private boolean noSpin = false;
    private boolean resetNoSpin = false;
    private boolean spinChange = false;
    private int newSpin = 0;

    //The "antiSpeed" integer is how long the Thread waits before redrawing the next iteration.
    //So a high antiSpeed integer causes a slow animation (that's why it's called "antiSpeed" instead of simply "speed").
    private int[] antiSpeedPhaseArray = {1, 9, 19, 29, 50, 90, 200, 500};
    private int antiSpeedPhaseSelector = 2;
    private int antiSpeed = antiSpeedPhaseArray[antiSpeedPhaseSelector];

    private boolean eraseCanvas = true;
    private boolean fill = true;

    private boolean crazyMode = false;
    private boolean seizureMode = false;
    private boolean reverse = false;
    private boolean reset = false;
    private boolean equilateral = true;
    private boolean equilateralReset = false;

    private double accumulatedAngle = 1;
    private double newAngle = 0;


    //Changeable variables for the equilateral triangles


    //The three corner-points for every triangle
    private double x1;
    private double y1;

    private double x2;
    private double y2;

    private double x3;
    private double y3;

    private boolean equilateralRatioTicker = false;

    private int baseRadius = 1;

    //There will usually be many triangles on the screen, from smallest to largest. This ArrayList holds them all (if they are equilaterals).
    private ArrayList<EquilateralTriangle> activeEquilateralTriangles;

    //Equilateral ratioList.
    //There are only two numbers, because these equilateral triangles only have two opposite phases... north and south.
    //So each new EquilateralTriangle object can be upside down relative to the two triangles preceding and succeeding it.
    private double[] equilateralRatioList = {2.0/3.0, 1.0/3.0};


    //Changeable variables for the right-angled triangles


    //There will usually be many triangles on the screen, from smallest to largest. This ArrayList holds them all (if they are right-angled).
    private ArrayList<RightTriangle> activeRightTriangles;

    //Four variables for four phases of the right-angled triangles... east, west, south, north.
    private double[] rightRatioList = {0.5, 1.0, 1.5, 2.0};
    private int rightRatioTicker=0;

    //Three corner-points for the triangle
    private float rx1;
    private float ry1;

    private float rx2;
    private float ry2;

    private float rx3;
    private float ry3;

    //Variables to control the music

    private MusicPlayer musicPlayer;
    private boolean playMusic = false;


//Multiple constructors:

    public TriangleFractalView(Context context) {
        super(context);
        thisContext = context;
        initializeVariables();
    }

    public TriangleFractalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        thisContext = context;
        initializeVariables();
    }

    public TriangleFractalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        thisContext = context;
        initializeVariables();
    }


    //SurfaceHolder methods
    //The SurfaceHolder gives us more control over the animation, allowing us to pass the SurfaceView's canvas around

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
     * This creates the thread which runs the triangle animation. It also sets up the canvas, Matrix, and bitmaps.
     * This method is initially called by the SurfaceHolder's onCreate method.
     */
    public void makeThread() {
        activeBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        //activeCanvas is NOT the SurfaceView's default canvas,
        //it is the canvas which we will manipulate and draw on.
        activeCanvas = new Canvas();
        activeCanvas.setBitmap(activeBitmap);
        identityMatrix = new Matrix();

        thread = new TriangleThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    /**
     * This method is called when a TriangleFractalView is first created.
     * It sets up many of the objects required for the animation-logic.
     */
    private void initializeVariables()
    {
        musicPlayer = new MusicPlayer(thisContext);
        randomizer = new Random();

        paint = new Paint();
        paint.setStrokeWidth(155);

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);

        activeRightTriangles = new ArrayList<RightTriangle>();
        activeRightTriangles.add(new RightTriangle(baseRadius));

        activeEquilateralTriangles = new ArrayList<EquilateralTriangle>();
        activeEquilateralTriangles.add(new EquilateralTriangle(equilateralRatioTicker, getEquilateralRatio(), baseRadius));
    }


    //These two inner-classes create objects which store vital information about individual triangles.
    //I created them within the java file because they're so small, and they're only used locally.


    public class EquilateralTriangle
    {
        public double piRatio;
        public double radius;
        public int color;
        public boolean whichRatio;
        //whichRatio refers to this Triangle's ratio's position in the equilateralRatioTicker
        //This allows me to easily access the next appropriate ratio to follow any existing Triangle

        public EquilateralTriangle(boolean whichRatio, double piRatio, double radius)
        {
            this.piRatio = piRatio;
            this.radius = radius;
            this.color = colors[colorTicker];
            this.whichRatio = whichRatio;
            increaseColorTicker();
        }

        public void setColor(int newColor)
        {
            this.color=newColor;
        }
    }

    /**
     * This right-angled triangle works a little differently, carrying around his three piRatio numbers,
     * to show which direction to draw the hypotenuse.
     */
    public class RightTriangle
    {
        public double radius;
        public int color;
        public double[] piRatio = {0,0,0};
        public int whichRatio;

        public RightTriangle(double radius)
        {
            this.radius = radius;
            this.color = colors[colorTicker];
            setPiRatios();

            increaseColorTicker();
        }

        public void setPiRatios()
        {
            this.whichRatio = rightRatioTicker;
            double x = getRightRatio();
            double y = getRightRatio();
            double z = getRightRatio();
            piRatio = new double[]{x,y,z};
        }
    }


    /**
     * This is the primary method of the animation. It is repeatedly called by the Thread.
     * This method calls for triangles to be drawn, and initiates calculations for where they should be drawn.
     * The animation logic descends from this method.
     * @param canvas
     */
    public void onDrawSomething(Canvas canvas)
    {
        //There are so many conditional/optional functions for each iteration,
        //that I decided to create a separate method to contain them, for the sake of readability.
        //(some conditionals remain in the onDrawSomething method, because of their importance).
        loopConditionals(canvas);

        //Which kind of Triangle are we drawing?
        if(equilateral) {
            setupEquilaterals();
        } else {
            setupRights();
        }

        iterations++;

        //The previous method calls have drawn triangles onto the activeCanvas.
        //Now we need to transfer that information onto the SurfaceView's (TriangleFractalView's) default canvas.
        canvas.drawBitmap(activeBitmap, identityMatrix, null);
        surfaceHolder.unlockCanvasAndPost(canvas);

        try {thread.sleep(antiSpeed);} catch (InterruptedException e) {}

        if(equilateralReset)
        {
            //if the user has selected to change between right/equilateral
            doEquilateralReset();
        }
    }

    //These next few methods draw the triangles, then calculate their new locations for the next iteration.

    /**
     * If we're dealing with equilateral triangles, this method directs the algorithm towards logic that
     * either makes the triangles bigger or smaller.
     */
    public void setupEquilaterals()
    {
        if (reverse == false) {
            drawForwardEquilaterals();
        } else {
            drawReverseEquilaterals();
        }
    }

    /**
     * If we're dealing with right-angle triangles, this method directs the algorithm towards logic that
     * either makes the triangles bigger or smaller.
     */
    public void setupRights()
    {
        if (reverse == false) {
            drawForwardRights();
        } else {
            drawReverseRights();
        }
    }

    /**
     * This method takes the ArrayList of Active Equilateral Triangles, draws each one, and changes them for the next iteration.
     */
    private void drawForwardEquilaterals()
    {
        for (int i=0; i<activeEquilateralTriangles.size(); i++)
        {
            EquilateralTriangle currentTriangle = activeEquilateralTriangles.get(i);
            drawOneEquilateralTriangle(currentTriangle.radius, Math.PI * currentTriangle.piRatio, currentTriangle.color);
            changeEquilateralTriangle(currentTriangle);
        }

        //Delete triangles that are out of the scope of the screen, and create new ones on the other end of the size-spectrum.
        if (activeEquilateralTriangles.get(activeEquilateralTriangles.size()-1).radius >= 2)
        {
            activeEquilateralTriangles.add(new EquilateralTriangle(equilateralRatioTicker, getEquilateralRatio(), activeEquilateralTriangles.get(activeEquilateralTriangles.size() - 1).radius / 2));
        }

        if (activeEquilateralTriangles.get(0).radius>canvasWidth*5 && activeEquilateralTriangles.get(0).radius>canvasHeight*4)
        {
            activeEquilateralTriangles.remove(0);
        }
    }

    /**
     * This method takes the ArrayList of Active Equilateral Triangles, draws each one, and changes them for the next iteration.
     */
    private void drawReverseEquilaterals()
    {
        for (int i=0; i<activeEquilateralTriangles.size(); i++)
        {
            EquilateralTriangle currentTriangle = activeEquilateralTriangles.get(i);
            drawOneEquilateralTriangle(currentTriangle.radius, Math.PI * currentTriangle.piRatio, currentTriangle.color);
            reverseChangeEquilateralTriangle(currentTriangle);
        }

        //Delete triangles that are out of the scope of the screen, and create new ones on the other end of the size-spectrum.
        if (activeEquilateralTriangles.get(activeEquilateralTriangles.size()-1).radius <= 1)
        {
            activeEquilateralTriangles.remove(activeEquilateralTriangles.size()-1);
        }

        if (activeEquilateralTriangles.get(0).radius<canvasWidth*3 && activeEquilateralTriangles.get(0).radius<canvasHeight*3)
        {
            activeEquilateralTriangles.add(0, new EquilateralTriangle(equilateralRatioTicker, getEquilateralRatio(),activeEquilateralTriangles.get(0).radius*2));
        }
    }

    /**
     * This method takes the ArrayList of Active Right-Angled Triangles, draws each one, and changes them for the next iteration.
     */
    private void drawForwardRights()
    {
        for (int i=0; i<activeRightTriangles.size(); i++)
        {
            RightTriangle currentTriangle = activeRightTriangles.get(i);
            drawOneRightTriangle(currentTriangle);
            changeRightTriangle(currentTriangle);
        }

        //Delete triangles that are out of the scope of the screen, and create new ones on the other end of the size-spectrum.
        if (activeRightTriangles.get(activeRightTriangles.size()-1).radius >=2)
        {
            activeRightTriangles.add(new RightTriangle(activeRightTriangles.get(activeRightTriangles.size()-1).radius / 1.5));
        }

        if (activeRightTriangles.get(0).radius > (canvasWidth+canvasHeight)*4)
        {
            activeRightTriangles.remove(0);
        }
    }

    /**
     * This method takes the ArrayList of Active Right-Angled Triangles, draws each one, and changes them for the next iteration.
     */
    private void drawReverseRights()
    {
        for (int i=0; i<activeRightTriangles.size(); i++)
        {
            RightTriangle currentTriangle = activeRightTriangles.get(i);
            drawOneRightTriangle(currentTriangle);
            reverseChangeRightTriangle(currentTriangle);
        }

        //Delete triangles that are out of the scope of the screen, and create new ones on the other end of the size-spectrum.
        if (activeRightTriangles.get(activeRightTriangles.size()-1).radius <= 1)
        {
            activeRightTriangles.remove(activeRightTriangles.size() - 1);
        }

        if (activeRightTriangles.get(0).radius < canvasWidth*3 && activeRightTriangles.get(0).radius < canvasHeight*3)
        {
            activeRightTriangles.add(0, new RightTriangle(canvasHeight * 3));
        }
    }

    /**
     * This method is called for every individual Right-Angled triangle that needs to be drawn.
     * So this method will be called a number of times for every iteration (if the equilateral boolean is false).
     *
     * Here, we use a diameter to define a circle. Then we create three points along that circle which become the triangle's corners.
     * The diameter and phase (pole) of each triangle is contained in the triangle object itself.
     * This method retrieves that information and calculates where the triangle should be drawn on the canvas.
     *
     * This method is called regardless of whether the triangles are growing smaller or larger.
     * @param thisTriangle
     */
    private void drawOneRightTriangle(RightTriangle thisTriangle)
    {
        double diameter = thisTriangle.radius * 2.0;
        int triangleColor = thisTriangle.color;

        if(!noSpin)
        {
            //newAngle represents how much to spin the triangle.
            newAngle = iterations/spin;
        }

        double ratio1 = thisTriangle.piRatio[0];
        double ratio2 = thisTriangle.piRatio[1];
        double ratio3 = thisTriangle.piRatio[2];

        // The vital code.
        // Calculating the x,y position for each point of right angled triangles:
        // The three points of the triangle are a certain distance (diameter) from the arbitrary "center,"
        // at a certain angle which is based on the piRatio, spin, and accumulated spin.
        if(!crazyMode) {
            rx1 = (float) (centerX + diameter * Math.sin(Math.PI * (ratio1) - accumulatedAngle - newAngle));
            ry1 = (float) (centerY + diameter * Math.cos(Math.PI * (ratio1) - accumulatedAngle - newAngle));

            rx2 = (float) (centerX + diameter * Math.sin(Math.PI * (ratio2) - accumulatedAngle - newAngle));
            ry2 = (float) (centerY + diameter * Math.cos(Math.PI * (ratio2) - accumulatedAngle - newAngle));

            rx3 = (float) (centerX + diameter * Math.sin(Math.PI * (ratio3) - accumulatedAngle - newAngle));
            ry3 = (float) (centerY + diameter * Math.cos(Math.PI * (ratio3) - accumulatedAngle - newAngle));

        } else {
            //Crazy-Mode is just weird logic, which is probably not fractal, but which makes a cool animation.
            rx1 = (float) (centerX + diameter * Math.sin(Math.PI * (ratio1 + newAngle)));
            ry1 = (float) (centerY + diameter * Math.cos(Math.PI * (ratio1/1.1) - (newAngle-1)/randomizer.nextInt()));

            rx2 = (float) (centerX + diameter * Math.sin(Math.PI * (ratio2/iterations) - accumulatedAngle + newAngle));
            ry2 = (float) (centerY + diameter * Math.cos(Math.PI * (ratio2/(newAngle+1))));

            rx3 = (float) (centerX + diameter * Math.sin(Math.PI * (ratio3) - ((newAngle=0.25) / randomizer.nextInt())));
            ry3 = (float) (centerY + diameter * Math.cos(Math.PI * (ratio3/iterations - accumulatedAngle - newAngle)));
          }

        paint.setColor(triangleColor);
        paint.setStrokeWidth(1);

        //The logic is done. Now simply draw the triangles.
        Path visibleTriangle = new Path();
        visibleTriangle.moveTo(rx1,ry1);
        visibleTriangle.lineTo(rx2,ry2);
        visibleTriangle.lineTo(rx3,ry3);
        visibleTriangle.lineTo(rx1,ry1);
        visibleTriangle.close();

        activeCanvas.drawPath(visibleTriangle, paint);

        if(spinChange)
        {
            //onSpinChange() saves the current orientation of the triangle inside its circle, so the spin speed can change smoothly
            //(saves the orientation to "accumulatedAngle")
            onSpinChange();
        }

        //newAngle must always be reset to zero so it can be calculated fresh for each iteration.
        newAngle=0;
    }

    /**
     * This method is called for every individual equilateral triangle that needs to be drawn.
     * So this method will be called a number of times for every iteration (if the equilateral boolean is false).
     *
     * Here, we use a diameter to define a circle. Then we create three points along that circle which become the triangle's corners.
     * The diameter and phase (pole) of each triangle is contained in the triangle object itself.
     * This method retrieves that information and calculates where the triangle should be drawn on the canvas.
     *
     * This method is called regardless of whether the triangles are growing smaller or larger.
     * @param currentRadius
     * @param currentAngle
     * @param triangleColor
     */
    private void drawOneEquilateralTriangle(double currentRadius, double currentAngle, int triangleColor)
    {
        double radius = currentRadius;
        double diameter = radius*2;

        paint.setStrokeWidth(1);

        if(!noSpin)
        {
            //newAngle represents how much to spin the triangle.
            newAngle = iterations/spin;
        }

        // The vital code.
        // Calculating the x,y position for each point of equilateral triangles.
        // The three points of the triangle are a certain distance (diameter) from the arbitrary "center,"
        // at a certain angle which is based on the piRatio, spin, and accumulated spin.
            x1 = centerX + diameter * Math.sin(Math.PI * (2.0 / 3.0) + accumulatedAngle + newAngle + currentAngle);
            y1 = centerY + diameter * Math.cos(Math.PI * (2.0 / 3.0) + accumulatedAngle + newAngle + currentAngle);

            x2 = centerX + diameter * Math.sin(Math.PI * (4.0 / 3.0) + accumulatedAngle + newAngle + currentAngle);
            y2 = centerY + diameter * Math.cos(Math.PI * (4.0 / 3.0) + accumulatedAngle + newAngle + currentAngle);

            x3 = centerX + diameter * Math.sin(Math.PI * (2) + accumulatedAngle + newAngle + currentAngle);
            y3 = centerY + diameter * Math.cos(Math.PI * (2) + accumulatedAngle + newAngle + currentAngle);

             if(crazyMode) {
                    //As I tried to find the logic for a right-angle triangle I accidentally created this weird animation.
                    //I'm keeping it for the final product because it's fun.
                    //
                x1 = x1 + diameter * Math.sin(Math.PI * (1.0 / 3.0));
                y1 = y1 + diameter * Math.cos(Math.PI * (1.0 / 3.1));

                x2 = x2 + diameter * Math.sin(Math.PI * (4.3 / 3.0));
                y2 = y2 + diameter * Math.cos(Math.PI * (4.0 / iterations));

                x3 = x3 + diameter * Math.sin(Math.PI * (2.1));
                y3 = y3 + diameter * Math.cos(Math.PI * (1.9));
             }

        if(spinChange)
        {
            //onSpinChange() saves the current orientation of the triangle inside its circle, so the spin speed can change smoothly.
            //(saves the orientation to "accumulatedAngle")
            onSpinChange();
        }

        //newAngle must always be reset to zero so it can be calculated fresh for each iteration.
        newAngle=0;

        //Convert the doubles into floats which can be drawn onto the canvas.
        float tx1 = (float) x1;
        float ty1 = (float) y1;

        float tx2 = (float) x2;
        float ty2 = (float) y2;

        float tx3 = (float) x3;
        float ty3 = (float) y3;

        paint.setColor(triangleColor);

        if(seizureMode)
        {
            //Seizure Mode is a horrible visual experience.
            paint.setColor(colors[colorTicker]);
            increaseColorTicker();
        }

        //Paints the actual three lines of the triangle based on the preceding calculations.
        Path thisTriangle = new Path();
        thisTriangle.moveTo(tx1,ty1);
        thisTriangle.lineTo(tx2,ty2);
        thisTriangle.lineTo(tx3,ty3);
        thisTriangle.lineTo(tx1,ty1);
        thisTriangle.close();

        activeCanvas.drawPath(thisTriangle, paint);
    }



    //These next few methods are conditionals, called from somewhere in the loop when they are needed

    /**
     * Every time the onDrawSomething method is called, there are several conditional functions that need to be checked.
     * This method contains most of those functions.
     *
     * If the user presses a button which requests an alteration to the animation,
     * the method which alters the animation will not be called immediately because that could crash the algorithm.
     * Certain conditions must remain the same for the life of an iteration.
     * Instead, when a user requests a change, a boolean is immediately set so that on the beginning of the next iteration,
     * these conditionals will make the appropriate change before the calculations and drawings begin.
     * @param canvas
     */
    public void loopConditionals(Canvas canvas)
    {
        //If the user has requested that the canvas be reset.
        if(reset==true)
        {
            resetConditions();
        }

        //If it's the first iteration of the animation, or if we want to re-create the conditions of a first iteration.
        if(firstIteration)
        {
            firstIteration(canvas);
        }

        if (eraseCanvas == true && seizureMode==false) {
            activeCanvas.drawColor(0xff1e90ff);
        }

        if (seizureMode==true)
        {
            doSeizureStuff();
        }

        if(fill==false)
        {
            paint.setStyle(Paint.Style.STROKE);
        }
        else
        {
            paint.setStyle(Paint.Style.FILL);
        }
    }

    /**
     * If the user has requested to reset the canvas, this method will be called.
     */
    private void resetConditions()
    {
        if(reverse)
        {
            resetCanvas();
        }
        else
        {
            if(equilateral) {
                getCorrectEquilateralRatio();
            }
        }
        reset=false;
        iterations=1;
        firstIteration=true;
    }

    /**
     * This method is called to change the speed of the spin.
     * This method sets the accumulatedAngle variable so the triangle will keep its location.
     */
    private void onSpinChange()
    {
        if (!resetNoSpin)
        {
            accumulatedAngle += iterations/spin;
        }

        iterations = 0;
        spin = newSpin;
        spinChange = false;
        resetNoSpin = false;
    }

    /**
     * This method sets the triangle's center to the middle of the screen and erases anything that has been drawn.
     * This method is called upon the very first iteration of the animation,
     * and any time we want to recreate the conditions of the first iteration.
     * @param canvas
     */
    private void firstIteration(Canvas canvas)
    {
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        centerX = canvasWidth/2;
        centerY = canvasHeight/2;
        firstIteration = false;
        eraseCanvas = true;
    }

    /**
     * If the user has selected Seizure Mode, this method will be called during every iteration
     * to display the animation in a disconcerting sequence of colours.
     * Seizure Mode is a horrible visual experience where everything flashes a lot.
     */
    private void doSeizureStuff()
    {
        activeCanvas.drawColor(seizureColors[seizureColorTicker]);
        seizureColorTicker++;
        if(seizureColorTicker>=seizureColors.length)
        {
            seizureColorTicker=0;
        }
        toggleFill();
    }


    //End of conditional methods


    /**
     * This method chooses the color for the next triangle.
     */
    private void increaseColorTicker()
    {
        colorTicker++;
        if (colorTicker >= colors.length)
        {
            colorTicker = 0;
        }
    }

    /**
     *This method flips the pole (phase/angle) of the next equilateral triangle.
     * @return
     */
    private double getEquilateralRatio()
    {
        equilateralRatioTicker = !equilateralRatioTicker;

        if(equilateralRatioTicker) {
            return equilateralRatioList[0];
        }
        else
        {
            return equilateralRatioList[1];
        }
    }

    /**
     * This method finds out the angle of the last right-angled triangle,
     * so the next right-angle triangle can use the next angle in the sequence.
     * @return
     */
    private double getRightRatio()
    {
        double newRatio = rightRatioList[rightRatioTicker];
        rightRatioTicker++;
        if (rightRatioTicker >= rightRatioList.length)
        {
            rightRatioTicker=0;
        }
        return newRatio;
    }

    /**
     * This method is for when the animation is changing from reverse to !reverse,
     * and we need to fill the center with one giant full-screen triangle.
     */
    private void getCorrectEquilateralRatio()
    {
        equilateralRatioTicker = !activeEquilateralTriangles.get(activeEquilateralTriangles.size()-1).whichRatio;
    }

    //These next four methods increase or decrease the radius of the circle upon which the triangle is drawn.
    //They receive a Triangle object as a parameter and perform a simple calculation to adjust its radius.


    /**
     * Increase the size of an equilateral triangle.
     * @param becomingTriangle
     */
    public void changeEquilateralTriangle(EquilateralTriangle becomingTriangle)
    {
        becomingTriangle.radius = becomingTriangle.radius*1.04;
    }

    /**
     * Decrease the size of an equilateral triangle.
     * @param becomingTriangle
     */
    public void reverseChangeEquilateralTriangle(EquilateralTriangle becomingTriangle)
    {
        becomingTriangle.radius = becomingTriangle.radius*0.95;
    }

    /**
     * Increase the size of a right-angled triangle.
     * @param becomingTriangle
     */
    public void changeRightTriangle(RightTriangle becomingTriangle)
    {
        becomingTriangle.radius = becomingTriangle.radius *1.04;
    }

    /**
     * Decrease the size of a right-angled triangle.
     * @param becomingTriangle
     */
    public void reverseChangeRightTriangle(RightTriangle becomingTriangle)
    {
        becomingTriangle.radius = becomingTriangle.radius *0.95;
    }



      //The following methods are called from the menu to alter the animation.

      //These methods do not directly alter the animation because that could crash the algorithm.
      //Certain conditions must remain the same for the life of an iteration.
      //Instead, these methods set a boolean so that on the beginning of the next iteration,
      //the appropriate alterations can be made before the calculations and drawings begin.



    public void toggleErase()
    {
        eraseCanvas = !eraseCanvas;
    }

    public void toggleFill()
    {
        fill = !fill;
    }


    public void toggleReverse()
    {
        if (reverse==true)
        {
            reverse=false;
        }
        else
        {
            if(equilateral) {
                reset = true;
            }
            reverse=true;
        }
    }


    /**
     * This method tells the next iteration to switch between equilateral and right triangles.
     */
    public void toggleEquilateral()
    {
        reset = true;
        equilateralReset = true;
    }

    /**
     * Switching between equilateral and right triangles requires careful calculations
     * to make sure nothing crashes. This method is part of that process.
     */
    private void doEquilateralReset()
    {
        resetConditions();
        if (equilateral==true)
        {
            equilateral=false;
        }
        else
        {
            equilateral=true;
        }
        reset = true;
        equilateralReset = false;
    }

    /**
     * Crazy Mode creates weird looking animations instead of the proper triangles.
     */
    public void toggleCrazy()
    {
        if (crazyMode==true)
        {
            crazyMode=false;
        }
        else
        {
            crazyMode=true;
        }
    }

    /**
     * Seizure Mode is a horrible visual experience.
     */
    public void toggleSeizureMode()
    {
        if (seizureMode==true)
        {
            seizureMode=false;
            eraseCanvas =false;
            activeCanvas.drawColor(0xff1e90ff);
            fill=false;
        }
        else
        {
            seizureMode=true;
            eraseCanvas =true;
            antiSpeedPhaseSelector = 2;
            antiSpeed = antiSpeedPhaseArray[antiSpeedPhaseSelector];
        }
    }

    /**
     * Speed up the animation by decreasing the wait time (int antiSpeed)
     * between iterations.
     */
    public void faster()
    {
        antiSpeedPhaseSelector--;

        if (antiSpeedPhaseSelector < 0)
        {
            antiSpeedPhaseSelector = 0;
        }

        antiSpeed = antiSpeedPhaseArray[antiSpeedPhaseSelector];
    }

    /**
     * Slow down the animation by increasing the wait time (int antiSpeed)
     * between iterations.
     */
    public void slower()
    {
        antiSpeedPhaseSelector++;

        if (antiSpeedPhaseSelector >= antiSpeedPhaseArray.length)
        {
            antiSpeedPhaseSelector = antiSpeedPhaseArray.length - 1;
        }

        antiSpeed = antiSpeedPhaseArray[antiSpeedPhaseSelector];
    }


    /**
     * This method resets the canvas.
     * It is called directly from the menu,
     * and will not crash the algorithm, even in mid-iteration.
     */
    public void resetCanvas()
    {
        activeCanvas.drawColor(0xff1e90ff);

        if(equilateral)
        {
           resetEquilateral();
        }
        else
        {
            resetRight();
        }
    }

    /**
     * Descending from resetCanvas()
     * This method resets the animation for equilateral triangles.
     */
    private void resetEquilateral()
    {

        if (reverse == false) {
            activeEquilateralTriangles.clear();
            activeEquilateralTriangles.add(new EquilateralTriangle(equilateralRatioTicker, getEquilateralRatio(), baseRadius));
        } else {
            activeEquilateralTriangles.add(new EquilateralTriangle(equilateralRatioTicker, getEquilateralRatio(), canvasHeight * 4));
            activeEquilateralTriangles.get(0).setColor(0x1E90FF);
        }

    }

    /**
     * Descending from resetCanvas()
     * This method resets the animation for right-angled triangles.
     */
    private void resetRight()
    {
        activeRightTriangles.clear();
        if (reverse == false) {
            activeRightTriangles.add(new RightTriangle(baseRadius));
        } else {
            activeRightTriangles.add(new RightTriangle(canvasHeight * 4));
            activeRightTriangles.get(0).color = (0x1E90FF);
        }
    }

    /**
     * Make the triangles spin faster.
     */
    public void moreSpin()
    {
        spinPhaseSelector--;

        if (spinPhaseSelector < 0)
        {
            spinPhaseSelector = 0;
        }
        else
        {
            spinChange = true;
            newSpin = spinPhaseArray[spinPhaseSelector];
        }

        if(noSpin)
        {
            noSpin = false;
            resetNoSpin=true;
        }
    }

    /**
     * Slow down the triangles' spin.
     */
    public void lessSpin() {

        if (!noSpin)
        {
            spinPhaseSelector++;

            if (spinPhaseSelector >= spinPhaseArray.length)
            {
                spinPhaseSelector = spinPhaseArray.length - 1;
                noSpin = true;
            }

            newSpin = spinPhaseArray[spinPhaseSelector];
            spinChange = true;
        }
    }


    /**
     * This method is called when the user touches the screen.
     * The purpose is to change the center location for the circles within which the triangles are drawn.
     *
     * Instead of moving the circle's center directly to the place where the user touched,
     * this method calls another method to move the center more slowly and smoothly towards the user's finger.
     * This creates a more pleasant visual experience.
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int xTouched = (int)event.getX();
        int yTouched = (int)event.getY();
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
        }
        changeCenter(xTouched, yTouched);
        return true;
    }

    /**
     * Changes the "center" of the animation... called after touchscreen is touched.
     * Places the new center somewhere between the old center and the user's finger.
     * @param xTouched
     * @param yTouched
     */
    public void changeCenter(int xTouched, int yTouched)
    {
        centerX += (xTouched-centerX)/10.1;
        centerY += (yTouched-centerY)/10.1;
    }


    //The next few methods control the music player.
    //I created a dedicated MusicPlayer class to control all the audio.

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
