package eaton.connor.coincollection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputValidation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class AddCoinActivity extends AppCompatActivity {

    public static final String SerialNumber = "SerialNumber";
    public static final String Year = "Year";
    public static final String Mint = "Mint";
    public static final String Denom = "Denom";
    public static final String Grade = "Grade";
    public static final String Price = "Price";
    public static final String Series = "Series";
    public static final String CoinID = "CoinID";


    private static final int CAMERA_REQUEST_CODE_OBV = 1;
    private static final int CAMERA_REQUEST_CODE_REV = 2;


    private Map<String, Object> user = new HashMap<>();
    private String denom, type, year, mint, grade, barcode, price = "";
    FirebaseFirestore db;

    EditText SN_input;
    EditText price_input;
    EditText year_input;
    EditText denom_input;
    EditText mint_input;
    EditText series_input;
    EditText grade_input;

    RelativeLayout btn_obverse;
    RelativeLayout btn_reverse;
    ImageView img_obverse;
    ImageView img_reverse;
    TextView txt_obverse;
    TextView txt_reverse;

    private Handler handler;
    private StorageReference mStorage;
    private String mCurrentPhotoPathObv;
    private String mCurrentPhotoPathRev;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_coin);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        SN_input = (EditText) findViewById(R.id.input_serial_number);
        price_input = (EditText) findViewById(R.id.input_price);
        year_input = (EditText) findViewById(R.id.input_year);
        denom_input = (EditText) findViewById(R.id.input_denom);
        mint_input = (EditText) findViewById(R.id.input_mint);
        series_input = (EditText) findViewById(R.id.input_series);
        grade_input = (EditText) findViewById(R.id.input_grade);

        btn_obverse = (RelativeLayout) findViewById(R.id.relLayoutObv);
        img_obverse = (ImageView) findViewById(R.id.imageViewObv);
        txt_obverse = (TextView) findViewById(R.id.textViewObv);
        btn_reverse = (RelativeLayout) findViewById(R.id.relLayoutRev);
        img_reverse = (ImageView) findViewById(R.id.imageViewRev);
        txt_reverse = (TextView) findViewById(R.id.textViewRev);


        mStorage = FirebaseStorage.getInstance().getReference();

        btn_obverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(CAMERA_REQUEST_CODE_OBV);
            }
        });

        btn_reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(CAMERA_REQUEST_CODE_REV);
            }
        });

        String coin_id = getIntent().getStringExtra(CoinID);
        String serial_num = getIntent().getStringExtra(SerialNumber);
        String s_denom = getIntent().getStringExtra(Denom);
        String s_price = getIntent().getStringExtra(Price);
        String s_year = getIntent().getStringExtra(Year);
        String s_mint = getIntent().getStringExtra(Mint);
        String s_grade = getIntent().getStringExtra(Grade);
        String s_series = getIntent().getStringExtra(Series);

        if (serial_num != null && !serial_num.equals("")) SN_input.setText(serial_num);
        if (s_denom != null && !s_denom.equals("")) denom_input.setText(s_denom);
        if (s_year != null && !s_year.equals("")) year_input.setText(s_year);
        if (s_mint != null) mint_input.setText(s_mint);
        if (s_grade != null && !s_grade.equals("")) grade_input.setText(s_grade);
        if (s_price != null && !s_price.equals("")) price_input.setText(s_price);
        if (s_series != null && !s_series.equals("")) series_input.setText(s_series);

        if (coin_id != null) {
            final StorageReference path_obv = mStorage.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Obverses").child(coin_id);
            final StorageReference path_rev = mStorage.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Reverses").child(coin_id);

            GlideApp.with(this).load(path_obv).into(img_obverse);
            GlideApp.with(this).load(path_rev).into(img_reverse);
            txt_obverse.setText("Tap to change obverse");
            txt_obverse.setBackgroundColor(Color.rgb(100,100,100));
            txt_reverse.setText("Tap to change reverse");
            txt_reverse.setBackgroundColor(Color.rgb(100,100,100));

        }
        db = FirebaseFirestore.getInstance();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_coin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_coin:
                // Submit coin data to db
                addCoin();
                //Toast.makeText(getApplicationContext(), "Coin added!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddCoinActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                return true;
            case R.id.action_cancel:
                // Discard data. Switch to HomeActivity
                startActivity(new Intent(AddCoinActivity.this, HomeActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addCoin() {
        barcode = SN_input.getText().toString();
        denom = denom_input.getText().toString();
        type = series_input.getText().toString();
        year = year_input.getText().toString();
        mint = mint_input.getText().toString();
        grade = grade_input.getText().toString();
        price = price_input.getText().toString();
        final String s_coin_id = getIntent().getStringExtra(CoinID);


        Coin new_coin = new Coin(barcode, denom, type, year, mint, grade, price);

        final String uid = addUser();
        CollectionReference ref = db.collection("users").document(uid).collection("coins");
        if(s_coin_id != null){
            ref.document(s_coin_id).set(new_coin.getMap()).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    Toast.makeText(AddCoinActivity.this, "Coin edited! " + s_coin_id, Toast.LENGTH_SHORT).show();
                    uploadPhotos(s_coin_id, uid);
                }
            });
        }else {
            ref.add(new_coin.getMap()).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    Toast.makeText(AddCoinActivity.this, "Coin added! " + task.getResult().getId(), Toast.LENGTH_SHORT).show();
                    uploadPhotos(task.getResult().getId(), uid);
                }
            });
        }
    }

    private String addUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        user.put("user_id", uid);

        db.collection("users").document(uid)
                .set(user, SetOptions.merge());
        return uid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE_OBV || requestCode == CAMERA_REQUEST_CODE_REV) {
                ImageView img;
                TextView txt;
                String type;
                String photo_path;
                if (requestCode == CAMERA_REQUEST_CODE_OBV) {
                    img = img_obverse;
                    txt = txt_obverse;
                    type = "obverse";
                    photo_path = mCurrentPhotoPathObv;
                } else {
                    img = img_reverse;
                    txt = txt_reverse;
                    type = "reverse";
                    photo_path = mCurrentPhotoPathRev;

                }

                Bitmap imageBitmap = BitmapFactory.decodeFile(photo_path);

                Bitmap thumbnail = Bitmap.createScaledBitmap(imageBitmap, (int)(imageBitmap.getWidth() * 0.05), (int)(imageBitmap.getHeight() * 0.05), false);

                img.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                img.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                img.requestLayout();
                img.setImageBitmap(thumbnail);
                txt.requestLayout();
                txt.setText("Tap to change " + type);
                txt.setBackgroundColor(Color.rgb(100,100,100));
            }
        }
    }

    private void uploadPhotos(String id, String uid) {
        if(mCurrentPhotoPathObv != null){
            final StorageReference path_obv = mStorage.child("Users").child(uid).child("Obverses").child(id);

            Bitmap bmp_obv = BitmapFactory.decodeFile(mCurrentPhotoPathObv);

            ByteArrayOutputStream bos_obv = new ByteArrayOutputStream();
            bmp_obv.compress(Bitmap.CompressFormat.JPEG, 70, bos_obv);
            byte[] bitmapdata_obv = bos_obv.toByteArray();

            path_obv.putBytes(bitmapdata_obv).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Cache image after it's uploaded
                    GlideApp.with(getApplicationContext()).download(path_obv).signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));

                    Toast.makeText(AddCoinActivity.this, "Uploaded!", Toast.LENGTH_LONG).show();


                }
            });
        }
         if(mCurrentPhotoPathRev != null) {
            final StorageReference path_rev = mStorage.child("Users").child(uid).child("Reverses").child(id);

            Bitmap bmp_rev = BitmapFactory.decodeFile(mCurrentPhotoPathRev);

            ByteArrayOutputStream bos_rev = new ByteArrayOutputStream();
            bmp_rev.compress(Bitmap.CompressFormat.JPEG, 70, bos_rev);
            byte[] bitmapdata_rev = bos_rev.toByteArray();

            path_rev.putBytes(bitmapdata_rev).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Cache image after it's uploaded

                    GlideApp.with(getApplicationContext()).download(path_rev).signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));

                    Toast.makeText(AddCoinActivity.this, "Uploaded!", Toast.LENGTH_LONG).show();

                }
            });
        }
    }

    private File createImageFile(int CODE) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        if(CODE == CAMERA_REQUEST_CODE_OBV){
            mCurrentPhotoPathObv = image.getAbsolutePath();
        } else {
            mCurrentPhotoPathRev = image.getAbsolutePath();
        }
        return image;
    }
    private void dispatchTakePictureIntent(int CODE) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(CODE);
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CODE);
            }
        }
    }
}
