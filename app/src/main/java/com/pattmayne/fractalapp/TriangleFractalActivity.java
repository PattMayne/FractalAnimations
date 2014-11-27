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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * This Activity contains some of the interface to display and manipulate the Triangle Fractal Animation,
 * the logic for which is contained in the TriangleFractalView class.
 * Created by Matt on 2014-10-11.
 */
public class TriangleFractalActivity extends ActionBarActivity {

    TriangleFractalView triangleFractalView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.triangle_fractal_layout);
        triangleFractalView = (TriangleFractalView) findViewById(R.id.triangleViewCanvas);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.triangle_fractal_menu, menu);
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
            triangleFractalView.stopMusic();
            finish();
            return true;
        } else if (id == R.id.toggle_triangle_fill) {
            triangleFractalView.toggleFill();
            return true;
        } else if (id == R.id.increase_triangle_speed) {
            triangleFractalView.faster();
            return true;
        } else if (id == R.id.decrease_triangle_speed) {
            triangleFractalView.slower();
            return true;
        } else if (id == R.id.toggle_triangle_persistent) {
            triangleFractalView.toggleErase();
            return true;
        } else if (id == R.id.reverse_triangle) {
            triangleFractalView.toggleReverse();
            return true;
        }else if (id == R.id.reset_triangle) {
            triangleFractalView.resetCanvas();
            return true;
        } else if (id == R.id.triangle_seizure_mode) {
            triangleFractalView.toggleSeizureMode();
            return true;
        } else if (id == R.id.increase_triangle_spin) {
            triangleFractalView.moreSpin();
            return true;
        } else if (id == R.id.decrease_triangle_spin) {
            triangleFractalView.lessSpin();
            return true;
        } else if (id == R.id.triangle_crazy_mode) {
            triangleFractalView.toggleCrazy();
            return true;
        } else if (id == R.id.triangle_toggle_equilateral) {
            triangleFractalView.toggleEquilateral();
            return true;
        } else if (id == R.id.toggle_music)
        {
            triangleFractalView.toggleMusic();
            return true;
        } else if (id == R.id.next_track)
        {
            triangleFractalView.skipTrack();
            return true;
        }
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        triangleFractalView.stopMusic();
        finish();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        triangleFractalView.stopMusic();
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
        triangleFractalView.stopMusic();
        finish();
    }
}
