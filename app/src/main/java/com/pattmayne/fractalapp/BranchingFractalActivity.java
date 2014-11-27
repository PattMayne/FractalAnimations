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

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * This Activity displays the BranchingFractalView and its animation.
 */
public class BranchingFractalActivity extends ActionBarActivity {

    BranchingFractalView branchingFractalView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.branching_fractal_layout);
        branchingFractalView = (BranchingFractalView) findViewById(R.id.branchingViewCanvas);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.branching_fractal_menu, menu);
        return true;
    }

    /**
     * Menu items for the user to select.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.exit_the_app) {
            finish();
            return true;
        } else if (id == R.id.change_color) {
            branchingFractalView.changeColor(true);
            return true;
        } else if (id == R.id.increase_speed) {
            branchingFractalView.faster();
            return true;
        } else if (id == R.id.decrease_speed) {
            branchingFractalView.slower();
            return true;
        } else if (id == R.id.increase_iterations) {
            branchingFractalView.bigger();
            return true;
        } else if (id == R.id.decrease_iterations) {
            branchingFractalView.smaller();
            return true;
        } else if (id == R.id.shorter_lines) {
            branchingFractalView.shorterLines();
            return true;
        } else if (id == R.id.longer_lines) {
            branchingFractalView.longerLines();
            return true;
        } else if (id == R.id.reset_the_image) {
            branchingFractalView.resetImage();
            return true;
        } else if (id == R.id.rainbow_color) {
            branchingFractalView.setRainbow();
            return true;
        } else if (id == R.id.toggle_music)
        {
            branchingFractalView.toggleMusic();
            return true;
        } else if (id == R.id.next_track)
        {
            branchingFractalView.skipTrack();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
            finish();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRestart() {
        super.onRestart();

    }

    @Override
    public void onStop()
    {
        super.onStop();
        finish();
    }

}