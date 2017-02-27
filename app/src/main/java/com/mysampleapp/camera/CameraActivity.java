/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mysampleapp.camera;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.mysampleapp.R;


public class CameraActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //Allow only portrate orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (null == savedInstanceState) {
            try {
                getFragmentManager().beginTransaction()
                        .replace(R.id.container11, Camera2BasicFragment.newInstance())
                        .commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}