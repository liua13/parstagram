package com.annie.parstagram;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;

public class PhotoActivity extends AppCompatActivity {
    public static final String TAG = "PhotoActivity";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;

    private EditText etCaption;
    private Button btnTakePhoto;
    private ImageView ivPhoto;
    private Button btnPost;
    private File photoFile;
    private String photoFileName = "photo.jpg";

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        etCaption = findViewById(R.id.etCaption);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        ivPhoto = findViewById(R.id.ivPhoto);
        btnPost = findViewById(R.id.btnPost);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                startLoginActivity();
            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String caption = etCaption.getText().toString();
                if (caption.isEmpty()){
                    Toast.makeText(PhotoActivity.this, "Caption cannot be empty", Toast.LENGTH_SHORT).show();
                    return ;
                }
                if (photoFile == null || ivPhoto.getDrawable() == null){
                    Toast.makeText(PhotoActivity.this, "Image cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser user = ParseUser.getCurrentUser();
                savePost(user, caption, photoFile);
            }
        });
        
//        queryPosts();
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(PhotoActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ivPhoto.setImageBitmap(takenImage);
            } else {
                Toast.makeText(this, "Could not take picture", Toast.LENGTH_SHORT).show();
                return;
            }

        }
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if(e != null){
                    Log.e(TAG, "Could not query Parse database", e);
                    return;
                }
                for (Post post: posts){
                    Log.e(TAG, "FFF" + post.getCaption());
                }
            }
        });
    }

    private void savePost(ParseUser user, String caption, File photoFile) {
        Post post = new Post();
        post.setUser(user);
        post.setCaption(caption);
        post.setImage(new ParseFile(photoFile));
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if( e!= null){
                    Log.e(TAG, "Could not save post", e);
                    return;
                }
                etCaption.setText("");
                ivPhoto.setImageResource(0);
            }
        });
    }

    private void startLoginActivity(){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}