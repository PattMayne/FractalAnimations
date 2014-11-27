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

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.Color;
import android.view.View;

/**
 * This Activity displays a title screen
 * and buttons so the user can choose which animation to generate.
 */
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View branchingFractalView = new BranchingFractalView(this);
        View triangleFractalView = new TriangleFractalView(this);
        setContentView(R.layout.activity_main);
        branchingFractalView.setBackgroundColor(Color.WHITE);
        triangleFractalView.setBackgroundColor(Color.WHITE);
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
        if (id == R.id.exit_the_app) {
            finish();
            return true;
        } else if (id == R.id.options_menu_about) {
            loadAboutView(findViewById(R.id.options_menu_about));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method starts the Branching Fractal animation.
     * @param view
     */
    public void loadBranchingFractalView(View view) {
        Intent getNameScreenIntent = new Intent(this, BranchingFractalActivity.class);
        final int result = 1;
        startActivityForResult(getNameScreenIntent, result);
    }

    /**
     * This method starts the Triangle Fractal animation.
     * @param view
     */
    public void loadTriangleFractalView(View view) {
        Intent getNameScreenIntent = new Intent(this, TriangleFractalActivity.class);
        final int result = 1;
        startActivityForResult(getNameScreenIntent, result);
     }

    /**
     * This method displays the About screen.
     * @param view
     */
    public void loadAboutView(View view) {
        Intent getNameScreenIntent = new Intent(this, AboutActivity.class);
        final int result = 1;
        startActivityForResult(getNameScreenIntent, result);
    }
}
