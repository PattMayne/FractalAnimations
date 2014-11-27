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

/**
 * This Thread class continuously calls the onDrawSomething method of the BranchingFractalView class.
 * Created by Matt on 2014-10-07.
 */
public class BranchingThread extends Thread {

    private SurfaceHolder surfaceHolder;
    private BranchingFractalView branchingFractalView;
    Canvas canvas;


    public BranchingThread(SurfaceHolder surfaceHolder, BranchingFractalView branchingFractalView)
    {
        super();
        this.branchingFractalView = branchingFractalView;
        this.surfaceHolder = surfaceHolder;
    }

// flag to hold game state
	    private boolean running;

	    public void setRunning(boolean running) {
	        this.running = running;
	    }

	    @Override
	    public void run() {
	        while (running) {
                    canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    branchingFractalView.onDrawSomething(canvas);
                }
                else {running=false;}
                }
	    }

    public boolean isRunning()
    {
        return running;
    }

}
