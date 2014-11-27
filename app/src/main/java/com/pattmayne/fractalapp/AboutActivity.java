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
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * This Activity displays some information about the application and its creator.
 */
public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_layout);
    }

    /**
     * Load my website.
     * @param view
     */
    public void loadSite(View view)
    {
        Intent internet = new Intent();
        internet.setAction(Intent.ACTION_VIEW);
        internet.addCategory(Intent.CATEGORY_BROWSABLE);
        internet.setData(Uri.parse("http://www.pattmayne.com"));
        startActivity(internet);
    }

    /**
     * Close this Activity and go back to the previous screen.
     * @param view
     */
    public void okayButton(View view)
    {
        finish();
    }
}
