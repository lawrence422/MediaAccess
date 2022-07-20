package comp5216.sydney.edu.au.mediaacces;


import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Backup {
    Context context;
    private StorageReference storageReference;

    public Backup(Context context) {
        this.context = context;
    }

    public void backupToFirebase(ArrayList<File> list) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        storageReference = FirebaseStorage.getInstance().getReference(timeStamp);

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null) {

                StorageReference fileReference = storageReference.child(list.get(i).getName());
                fileReference.putFile(Uri.fromFile(list.get(i))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
