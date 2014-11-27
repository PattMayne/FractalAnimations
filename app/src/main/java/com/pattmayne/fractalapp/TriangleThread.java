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

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.pattmayne.fractalapp.BranchingFractalView;
import com.pattmayne.fractalapp.TriangleFractalView;

/**
 * I use this Thread to control the animation in the TriangleFractalView class.
 * For every iteration in this Thread, we call the "onDrawSomething" method in the TriangleFractalView,
 * which calculates what needs to be drawn.
 * Created by Matt on 2014-10-11.
 */
public class TriangleThread extends Thread{

    //The surfaceHolder allows this class to use the TriangleFractalView's canvas.
    private SurfaceHolder surfaceHolder;
    private TriangleFractalView triangleFractalView;
    Canvas canvas;


    public TriangleThread(SurfaceHolder surfaceHolder, TriangleFractalView triangleFractalView)
    {
        super();
        this.triangleFractalView = triangleFractalView;
        this.surfaceHolder = surfaceHolder;
    }

    // flag to hold game state
    private boolean running;

    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * This method contains statements which are repeatedly called by the Thread.
     * Those statements initiate each iteration of the animation in the TriangleFractalView.
     */
    @Override
    public void run() {
        while (running) {
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                triangleFractalView.onDrawSomething(canvas);
            }
            else {running=false;}
        }
    }

    public boolean isRunning()
    {
        return running;
    }
}
