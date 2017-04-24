package com.sendingmail;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.sendingmail.customeViews.PreferenceServices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.regex.Pattern;

import static com.sendingmail.R.id.buttonSend;

public class Home extends AppCompatActivity implements View.OnClickListener {

    private static final int TAKE_PICTURE = 22;
    private static final int CAMERA_CODE = 11;
    private static final int AccountCode = 1;
    private static String ImagePath = "";
    public static String Username="";

    Dialog dialog;

    //Declaring EditText
    private EditText editTextEmail;
    private EditText editTextSubject;
    private EditText editTextMessage;
    // private TextView txtpath;

    //for attachment
    ImageView img_attchement;

    //Send button
    private Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Initializing the views
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextSubject = (EditText) findViewById(R.id.editTextSubject);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        // txtpath=(TextView)findViewById(R.id.path);

        img_attchement = (ImageView) findViewById(R.id.takeimage);

        buttonSend = (Button) findViewById(R.id.buttonSend);

        //Adding click listener
        buttonSend.setOnClickListener(this);

        img_attchement.setOnClickListener(this);

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)) {
                //If the user has denied the permission previously your code will come to this block
                //Here you can explain why you need this permission
                //Explain here why you need this permission
            }

            //And finally ask for the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, AccountCode);
            return;
        }
//        Account[] accounts = AccountManager.get(this).getAccounts();
//        for (Account account : accounts) {
//            if (emailPattern.matcher(account.name).matches()) {
//                String possibleEmail = account.name;
//               //Toast.makeText(Home.this,possibleEmail,Toast.LENGTH_SHORT).show();
//                Log.e("EmailNames",possibleEmail);
//            }
//        }

        try {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
            startActivityForResult(intent, AccountCode);
        } catch (ActivityNotFoundException e) {
            // TODO
        }
    }

    private void sendEmail() {
        //Getting content for email
        String email = editTextEmail.getText().toString().trim();
        String subject = editTextSubject.getText().toString().trim();
        String message = editTextMessage.getText().toString().trim();

        Log.e("SendingBody", message);

        //Creating SendMail object
        SendMail sm = new SendMail(this, email, subject, message, ImagePath);

        //Executing sendmail to send email
        sm.execute();

        //clear the data
        clear();
    }
    private void clear(){
        editTextEmail.setText("");
        editTextSubject.setText("");
        editTextMessage.setText("");
        img_attchement.setImageResource(R.drawable.ic_add_a_photo_white_24dp);
    }

    //when click on take photo this method call
    private void takephoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        //File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //cheking the request code and result code
        //when it proper setting the bitmap on the imageView
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            img_attchement.setImageBitmap(photo);

            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            Uri tempUri = getImageUri(getApplicationContext(), photo);

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            File finalFile = new File(getRealPathFromURI(tempUri));

            //  txtpath.setText(String.valueOf(finalFile));

            ImagePath = String.valueOf(finalFile);
            //System.out.println(mImageCaptureUri);
        } else if (requestCode == AccountCode && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            //String UID = data.getStringExtra(AccountManager.KEY_CALLER_UID);
            String password = data.getStringExtra(AccountManager.KEY_PASSWORD);
            Username=accountName;

            OTPDialog();
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSend:
                sendEmail();
                break;

            case R.id.takeimage:
                if (!isCameraAccesAllow()) {
                    takingcamerapermission();
                } else {
                    takephoto();
                }
                break;
        }
    }

    //We are calling this method to check the permission status
    private boolean isCameraAccesAllow() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    private void takingcamerapermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
    }

    //after register click otp dialog apear for taking otp
    private void OTPDialog() {
        dialog=new Dialog(Home.this);
        //setting custome view
        dialog.setContentView(R.layout.otpdialoglayout);
        dialog.setCancelable(false);
        //making the background transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        ImageView imageView = (ImageView) dialog.findViewById(R.id.otpclosedialog);
        Button btnSubmit = (Button) dialog.findViewById(R.id.btnOtpSubmit);
        final EditText edtpassword = (EditText) dialog.findViewById(R.id.edtpassword);
        final EditText edtusername = (EditText) dialog.findViewById(R.id.edtusername);

        edtusername.setText(Username);
        // Button btnCancel=(Button)dialog.findViewById(R.id.btnOtpCancel);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //cheking otp firld blank or not
                if (edtpassword.getText().toString().length() == 0 &&
                        edtpassword.getText().toString().equals("")) {
                    edtpassword.setError("Please Enter OTP");
                } else {

                   // new Config(edtusername.getText().toString(),edtpassword.getText().toString());
                    PreferenceServices.getInstance(Home.this).Settingdata(edtusername.getText().toString(),edtpassword.getText().toString());
                    //email.setText(accountName);
                    Log.e("Emailid", edtusername.getText().toString());
                    // Log.e("UID",UID);
                    Log.e("password", edtpassword.getText().toString());

                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }
}
