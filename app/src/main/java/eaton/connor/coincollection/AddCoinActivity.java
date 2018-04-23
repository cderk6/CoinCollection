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

    private static final int CAMERA_REQUEST_CODE_OBV = 1;
    private static final int CAMERA_REQUEST_CODE_REV = 2;


    private Map<String, Object> user = new HashMap<>();
    private String denom, type, year, mint, grade, barcode, price = "";
    FirebaseFirestore db;

    TextInputEditText SN_input;
    EditText price_input;
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
        SN_input = (TextInputEditText) findViewById(R.id.input_serial_num);
        price_input = (EditText) findViewById(R.id.spinner_price);

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

        String serial_num = getIntent().getStringExtra(SerialNumber);
        String s_denom = getIntent().getStringExtra(Denom);
        String s_price = getIntent().getStringExtra(Price);
        String s_year = getIntent().getStringExtra(Year);
        String s_mint = getIntent().getStringExtra(Mint);
        String s_grade = getIntent().getStringExtra(Grade);
        String s_series = getIntent().getStringExtra(Series);


        SN_input.setText(serial_num);

        handler = new Handler();
        /*
        if (serial_num != null){
            parseCoinInfo(serial_num);
        }
        */


        // Populate these from db later
        HashSet<String> array_denom = new HashSet<String>();
        array_denom.add("$1");
        HashSet<String> array_type = new HashSet<String>();
        array_type.add("Morgan Dollar");
        HashSet<String> array_year = new HashSet<String>();
        array_year.add("1879");
        HashSet<String> array_mint = new HashSet<String>();
        array_mint.add("O");
        HashSet<String> array_grade = new HashSet<String>();
        array_grade.add("MS63");


        if (s_denom != null && !s_denom.equals("")) array_denom.add(s_denom);
        if (s_year != null && !s_year.equals("")) array_year.add(s_year);
        if (s_mint != null) array_mint.add(s_mint);
        if (s_grade != null && !s_grade.equals("")) array_grade.add(s_grade);
        if (s_price != null && !s_price.equals("")) price_input.setText(s_price);
        if (s_series != null && !s_series.equals("")) array_type.add(s_series);

        final Spinner spinner_denom = (Spinner) findViewById(R.id.spinner_denomination);
        ArrayAdapter<String> adapter_denom = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>(array_denom));
        adapter_denom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_denom.setAdapter(adapter_denom);
        if (s_denom != null && !s_denom.equals(""))
            spinner_denom.setSelection(adapter_denom.getPosition(s_denom));
        spinner_denom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                denom = spinner_denom.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Spinner spinner_type = (Spinner) findViewById(R.id.spinner_type);
        ArrayAdapter<String> adapter_type = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>(array_type));
        adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_type.setAdapter(adapter_type);
        if (s_series != null && !s_series.equals(""))
            spinner_type.setSelection(adapter_type.getPosition(s_series));
        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                type = spinner_type.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Spinner spinner_year = (Spinner) findViewById(R.id.spinner_year);
        ArrayAdapter<String> adapter_year = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>(array_year));
        adapter_year.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_year.setAdapter(adapter_year);
        if (s_year != null && !s_year.equals(""))
            spinner_year.setSelection(adapter_year.getPosition(s_year));
        spinner_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                year = spinner_year.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Spinner spinner_mint = (Spinner) findViewById(R.id.spinner_mint);
        ArrayAdapter<String> adapter_mint = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>(array_mint));
        adapter_mint.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mint.setAdapter(adapter_mint);
        if (s_mint != null) spinner_mint.setSelection(adapter_mint.getPosition(s_mint));
        spinner_mint.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mint = spinner_mint.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Spinner spinner_grade = (Spinner) findViewById(R.id.spinner_grade);
        ArrayAdapter<String> adapter_grade = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>(array_grade));
        adapter_grade.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_grade.setAdapter(adapter_grade);
        if (s_grade != null && !s_grade.equals(""))
            spinner_grade.setSelection(adapter_grade.getPosition(s_grade));
        spinner_grade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                grade = spinner_grade.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                startActivity(new Intent(AddCoinActivity.this, HomeActivity.class));
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
        String barcode = SN_input.getText().toString();
        String price = price_input.getText().toString();

        Coin new_coin = new Coin(barcode, denom, type, year, mint, grade, price);

        final String uid = addUser();
        CollectionReference ref = db.collection("users").document(uid).collection("coins");
        ref.add(new_coin.getMap()).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                Toast.makeText(AddCoinActivity.this, "Coin added! " + task.getResult().getId(), Toast.LENGTH_SHORT).show();
                uploadPhotos(task.getResult().getId(), uid);
            }
        });
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

                img.getLayoutParams().height = (int) (getResources().getDisplayMetrics().density * thumbnail.getHeight());
                img.getLayoutParams().width = (int) (getResources().getDisplayMetrics().density * thumbnail.getWidth());
                img.requestLayout();
                img.setImageBitmap(thumbnail);
                txt.getLayoutParams().width = (int) (getResources().getDisplayMetrics().density * thumbnail.getWidth());
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
                    GlideApp.with(getApplicationContext()).download(path_obv);

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
                    GlideApp.with(getApplicationContext()).downloadOnly();

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
