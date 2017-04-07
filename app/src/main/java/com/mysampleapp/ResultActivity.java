package com.mysampleapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.content.ContentItem;
import com.amazonaws.mobile.content.ContentProgressListener;
import com.amazonaws.mobile.content.UserFileManager;
import com.amazonaws.regions.Regions;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.mysampleapp.camera.CameraActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.mysampleapp.R;

import static android.R.attr.width;
import static android.R.color.black;
import static android.R.color.white;

public class ResultActivity extends AppCompatActivity  {

    public static final int REQUEST_IMAGE_CAPTURE = 1;

    /** Class name for log messages. */
    private static final String LOG_TAG = ResultActivity.class.getSimpleName();

    public static final String TAG = "Result Activity";

    public String path;

    private Bitmap mImageBmpOriginal;
    private Bitmap mImageBmp;
    private Bitmap mImageBmpOut;
    private ImageView mImage;
    private TextView mTextViewBf;
    private double mPercentage;
    int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        final Button button = (Button) findViewById(R.id.ReScanButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent2 = getIntent();
                finish();
                startActivity(intent2);
            }
        });


        //Allow only portrate orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        mImage = (ImageView) findViewById(R.id.imageView11);
        mTextViewBf = (TextView) findViewById(R.id.textViewBf);

        //clear the default image
        mImage.setVisibility(View.INVISIBLE);
        mTextViewBf.setVisibility(View.INVISIBLE);

        Bundle extras = getIntent().getExtras();
        if(extras==null) {
            //Starting the camera for result
            startCameraActivity(this.findViewById(android.R.id.content).getRootView());
        }
        else
        {

        }
    }

    public  void startCameraActivity(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register notification receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver,
                new IntentFilter(PushListenerService.ACTION_SNS_NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister notification receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            //converting to bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            mImageBmp = BitmapFactory.decodeFile(extras.get("data").toString(), options);



            //mImageBmp = RotateBitmap(mImageBmp,90);
            //Show Capture in UI
            //mImage.setImageBitmap(mImageBmp);
            //mImage.setVisibility(View.VISIBLE);
            //saving the file path
            SharedPreferences.Editor editor = getSharedPreferences("Bitmap", MODE_PRIVATE).edit();
            editor.putString("path", extras.get("data").toString());
            editor.commit();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // No uplaod so no compression
            mImageBmp.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] bytes = baos.toByteArray();

            //converting to bitmap
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            path = extras.get("data").toString();
            mImageBmp = BitmapFactory.decodeFile(path, options);
            mImageBmp = RotateBitmap(mImageBmp,90);

//
//            //TODO Omer is this garbage from Firebase?
//            FileOutputStream stream = null;
//            try {
//                stream = new FileOutputStream(path);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            try {
//                stream.write(bytes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            /*final ProgressDialog progDailog = ProgressDialog.show(this,
                    "We are working on it",
                    "please wait....", true);
            */




            double BFPesimate  = calculateBFP();


            mImage.setImageBitmap(mImageBmpOriginal);
            mImage.setVisibility(View.VISIBLE);

            // Check if negative or if maxed out
            if(BFPesimate < 0 || BFPesimate > 45)
            {
                mTextViewBf.setText( String.format("%.0f", BFPesimate) + "% (Error)");
            }
            else
            {
                mTextViewBf.setText( String.format("%.0f", BFPesimate) + "%");
            }
            //Percentage from server responses

            mTextViewBf.setVisibility(View.VISIBLE);

        }
        else
        {
            super.onActivityResult(requestCode,resultCode,data);
        }


    }

    public double calculateBFP()
    {

        mImageBmp = RotateBitmap(mImageBmp,-90);
        int we = mImageBmp.getWidth();
        int he = mImageBmp.getHeight();




        // Analysis area:
        // The middle line exists at: 55%
        // The top line exists at: 17%
        double percentageSides = 0.375;
        int top = (int)(0.17*he);
        int bottom = (int)(0.55*he);
        int height = bottom - top;
        int right = (int)(0.5*we);
        int left = right - (int)(percentageSides*height);
        int width = right - left;




        // Note that This begins on the top left and moves right-down
        mImageBmpOriginal = mImageBmp;
        mImageBmp = Bitmap.createBitmap(mImageBmp, left, top, width, height);


        // The bitmap that will be displayed at the end
        mImageBmpOut = Bitmap.createBitmap(mImageBmp.getWidth(), mImageBmp.getHeight(), Bitmap.Config.ARGB_8888);


        //-------------------------------------------------------------------------------
        // We need to convert the part of the image into an array of bytes in grayscale
        //-------------------------------------------------------------------------------
        for (int x = 0; x < mImageBmp.getWidth(); ++x)
        {
            for (int y = 0; y < mImageBmp.getHeight(); ++y)
            {
                // get pixel color
                int pixel = mImageBmp.getPixel(x, y);
                int A = Color.alpha(pixel);
                int R = Color.red(pixel);
                int G = Color.green(pixel);
                int B = Color.blue(pixel);
                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);
                mImageBmpOut.setPixel(x, y, Color.argb(A, gray, gray, gray));
            }
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mImageBmpOut.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] srcData = stream.toByteArray();



        //-------------------------------------------------------------------------------
        // Find the threshold using Utsu's method
        // http://www.labbookpages.co.uk/software/imgProc/otsuThreshold.html
        //-------------------------------------------------------------------------------
        // Calculate histogram
        int threshold = 0;
        int ptr = 0;
        int maxLevelValue = 0;
        int histData[] = new int[256];;
        // Clear histogram data
        // Set all values to zero
        while (ptr < histData.length) histData[ptr++] = 0;


        // Calculate histogram and find the level with the max value
        // Note: the max level value isn't required by the Otsu method
        while (ptr < srcData.length)
        {
            int h = 0xFF & srcData[ptr];
            histData[h] ++;
            if (histData[h] > maxLevelValue) maxLevelValue = histData[h];
            ptr ++;
        }

        // Total number of pixels
        int total = srcData.length;

        float sum = 0;
        for (int t=0 ; t<256 ; t++) sum += t * histData[t];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;

        for (int t=0 ; t<256 ; t++)
        {
            wB += histData[t];					// Weight Background
            if (wB == 0) continue;

            wF = total - wB;						// Weight Foreground
            if (wF == 0) break;

            sumB += (float) (t * histData[t]);

            float mB = sumB / wB;				// Mean Background
            float mF = (sum - sumB) / wF;		// Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }



        //-------------------------------------------------------------------------------
        // Now implement the threshold on the bytes
        //-------------------------------------------------------------------------------
        // TODO Need to go through everything again?
        int white = 0;
        int black = 0;

        for (int x = 0; x < mImageBmpOut.getWidth(); ++x)
        {
            for (int y = 0; y < mImageBmpOut.getHeight(); ++y)
            {
                // get pixel color
                int pixel = mImageBmpOut.getPixel(x, y);
                int A = Color.alpha(pixel);
                int graysc = Color.red(pixel);

                if (graysc > threshold)
                {
                    graysc = 255;
                    white = white + 1;
                }

                else
                {
                    graysc = 0;
                    black = black + 1;
                }

                // set new pixel color to output bitmap (TO DO go back)
                //mImageBmpOut.setPixel(x, y, Color.argb(A, graysc, graysc, graysc));
                mImageBmpOriginal.setPixel(left + x, top + y, Color.argb(A, graysc, graysc, graysc));
            }
            count = white + black;
        }

        //-------------------------------------------------------------------------------
        // Calculate and display percentage
        //-------------------------------------------------------------------------------
        mPercentage = ((double)black / (double)count) * 100;//Integer.parseInt( extras.getString("message") );
        // MATLAB algorithm: Assume men:
        double estimate_male = (1.464*mPercentage) - 106.095;


        return estimate_male;



    }


    public void uploadData() {
        AWSMobileClient.defaultMobileClient()
                .createUserFileManager("myselffitbeta-userfiles-mobilehub-184640436", "", Regions.US_EAST_1, new UserFileManager.BuilderResultHandler() {
                    @Override
                    public void onComplete(final UserFileManager userFileManager) {
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();

                        final File file = new File(String.valueOf(path));
                        userFileManager.uploadContent(file,  AWSMobileClient.defaultMobileClient().getIdentityManager().getUserName() + "_" + ts + "_" + file.getName() , new ContentProgressListener() {

                            @Override
                            public void onSuccess(ContentItem contentItem) {
                                Toast.makeText(getApplicationContext(),"photo saved in server",Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onProgressUpdate(final String fileName, final boolean isWaiting,
                                                         final long bytesCurrent, final long bytesTotal) {
                                // Handle progress update here
                            }

                            @Override
                            public void onError(final String fileName, final Exception ex) {
                                // Handle error case here
                                Toast.makeText(getApplicationContext(),"Error: "+ ex.getMessage(),Toast.LENGTH_LONG);
                            }
                        });
                    }
                });
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Received notification from local broadcast. Display it in a dialog.");

            Bundle data = intent.getBundleExtra(PushListenerService.INTENT_SNS_NOTIFICATION_DATA);
            String message = PushListenerService.getMessage(data);


            //sending to activity
            Intent i = new Intent(context,ResultActivity.class);
            i.putExtra("message",message);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    };

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }




}
