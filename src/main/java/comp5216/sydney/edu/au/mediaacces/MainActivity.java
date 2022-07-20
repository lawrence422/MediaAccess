package comp5216.sydney.edu.au.mediaacces;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public String photoFileName = "photo.jpg";
    public String videoFileName = "video.mp4";
    //request codes
    private static final int MY_PERMISSIONS_REQUEST_OPEN_CAMERA = 101;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_VIDEO = 103;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    Button takePictureOrVideo;
    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    private List<String> mList;
    private RecyclerView recyclerView;
    private final int backupTime = 1;
    private final int resetTime = 0;
    private boolean isBackup = false;
    private final Backup backup = new Backup(MainActivity.this);


    @Override
    @RequiresApi(api = Build.VERSION_CODES.P)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerViewId);
        CheckUserPermsions();
        mList = new ArrayList<>();
        takePictureOrVideo = (Button) findViewById(R.id.recordvideo);


        takePictureOrVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTakePhotoClick();
            }
        });

        takePictureOrVideo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onRecordVideoClick();
                return false;
            }
        });

        if (CheckTime.checkBackupTime(backupTime) && !isBackup && CheckNetwork.isConnectedWifi(this)) {
            backup.backupToFirebase(findImage(Environment.getExternalStorageDirectory(), true));
        }
        if (isBackup) {
            isBackup = CheckTime.checkResetTime(resetTime);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (CheckTime.checkBackupTime(backupTime) && !isBackup && CheckNetwork.isConnectedWifi(this)) {
            backup.backupToFirebase(findImage(Environment.getExternalStorageDirectory(), true));
        }
        if (isBackup) {
            isBackup = CheckTime.checkResetTime(resetTime);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    void CheckUserPermsions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        display();  // init the contact list
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                display();  // init the contact list
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private ArrayList<File> findImage(File file, Boolean video) {

        ArrayList<File> imageList = new ArrayList<>();
        File[] imageFile = file.listFiles();

        for (File i : imageFile) {
            if (i.isDirectory() && !i.isHidden()) {
                imageList.addAll(findImage(i, video));

            } else {
                if (!video) {
                    if (i.getName().endsWith(".jpg") ||
                            i.getName().endsWith(".webp")) {
                        imageList.add(i);
                    }
                } else {
                    if (i.getName().endsWith(".jpg") ||
                            i.getName().endsWith(".webp") ||
                            i.getName().endsWith(".mp4")) {
                        imageList.add(i);
                    }
                }
            }
        }
        return imageList;
    }


    private void display() {
        ArrayList<File> mediaFile = findImage(Environment.getExternalStorageDirectory(), false);

        for (int j = 0; j < mediaFile.size(); j++) {
            mList.add(String.valueOf(mediaFile.get(j)));
            PhotoAdapter photoAdapter = new PhotoAdapter(mList);
            recyclerView.setAdapter(photoAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        }
    }


    public void onBackUpClick(View view) {
        if (CheckNetwork.isConnected(this)) {
            if (!CheckNetwork.isConnectedWifi(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Warning!!")
                        .setMessage("You are not connected to WiFi. Would you like to proceed?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                backup.backupToFirebase(findImage(Environment.getExternalStorageDirectory(), true));
                                isBackup = true;
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Nothing happens
                    }
                });
                builder.create().show();
            } else {
                backup.backupToFirebase(findImage(Environment.getExternalStorageDirectory(), true));
                isBackup = true;
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onTakePhotoClick() {
        // Check permissions
        if (marshmallowPermission.checkPermissionForCamera()
                || marshmallowPermission.checkPermissionForExternalStorage()) {
            marshmallowPermission.requestPermissionForCamera();
        } else {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // set file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            photoFileName = "IMG_" + timeStamp + ".png";

            if (getApplicationContext().getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_ANY)) {
                // this device has a camera
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_OPEN_CAMERA);
            }
            // no camera on this device
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onRecordVideoClick() {
        // Check permissions
        if (marshmallowPermission.checkPermissionForCamera()
                || marshmallowPermission.checkPermissionForExternalStorage()) {
            marshmallowPermission.requestPermissionForCamera();
        } else {
            // create Intent to capture a video and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // set file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            videoFileName = "VIDEO_" + timeStamp + ".mp4";

            startActivityForResult(intent, MY_PERMISSIONS_REQUEST_RECORD_VIDEO);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_PERMISSIONS_REQUEST_OPEN_CAMERA) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                getImageUri(photo);
                saveChange();
            }

        } else if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_VIDEO) {
            if (resultCode == RESULT_OK) {
                Uri videoUri = data.getData();

                Bitmap photo = ThumbnailUtils.createVideoThumbnail(getRealPathFromUrI(videoUri), MediaStore.Video.Thumbnails.MINI_KIND);

                getImageUri(photo);
                saveChange();
            }
        }
    }


    private void getImageUri(Bitmap photo) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.WEBP, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), photo, photoFileName, "");
        Uri.parse(path);
    }


    private String getRealPathFromUrI(Uri contentUri) {
        String result;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            // Source is local file path
            result = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }


    private void saveChange() {
        mList.clear();
        display();
    }
}
