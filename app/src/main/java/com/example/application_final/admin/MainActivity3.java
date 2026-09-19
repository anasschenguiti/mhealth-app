package com.example.application_final.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class MainActivity3 extends AppCompatActivity {

    TextInputEditText inputNom, inputEmail, inputPhone, inputDob, inputAddress, inputPassword, inputConfirmPassword;
    Spinner spinnerSexe, spinnerRole;
    Button btnSave;
    ImageView ivUserPhoto;
    Button btnAddPhoto;
    DatabaseHelper db;
    String userId = null;
    String currentPhotoUri = "";
    String originalRole = "";
    String originalSex = "";
    
    // ActivityResultLauncher pour sélectionner une image
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        db = DatabaseHelper.getInstance(this);

        inputNom = findViewById(R.id.input_nom);
        inputEmail = findViewById(R.id.input_email);
        inputPhone = findViewById(R.id.input_phone);
        inputDob = findViewById(R.id.input_date_naissance);
        inputAddress = findViewById(R.id.input_adresse);
        inputPassword = findViewById(R.id.input_password);
        inputConfirmPassword = findViewById(R.id.input_confirm_password);
        spinnerSexe = findViewById(R.id.spinner_sexe);
        spinnerRole = findViewById(R.id.spinner_role);
        btnSave = findViewById(R.id.btn_save_user);
        ivUserPhoto = findViewById(R.id.iv_user_photo);
        btnAddPhoto = findViewById(R.id.btn_add_photo);

        // Initialiser le launcher pour sélectionner une image
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                // Sauvegarder l'image dans le cache de l'application
                                String savedPath = saveImageToInternalStorage(selectedImageUri);
                                if (savedPath != null) {
                                    currentPhotoUri = savedPath;
                                    // Afficher l'image
                                    Bitmap bitmap = BitmapFactory.decodeFile(savedPath);
                                    if (bitmap != null) {
                                        ivUserPhoto.setImageBitmap(bitmap);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity3.this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        );

        // Configurer le bouton pour importer une photo
        btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        if (getIntent().hasExtra("USER_ID")) {
            userId = getIntent().getStringExtra("USER_ID");
            loadUserData(userId);
        }

        inputDob.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveUser());
    }

    private void loadUserData(String id) {
        Cursor c = db.getUserById(id);
        if (c != null && c.moveToFirst()) {
            inputNom.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME)));
            inputEmail.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)));
            inputPhone.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PHONE)));
            inputDob.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_DOB)));
            
            // Address
            int addrIndex = c.getColumnIndex(DatabaseHelper.COL_USER_ADDRESS);
            if (addrIndex != -1)
                inputAddress.setText(c.getString(addrIndex));
            
            // Charger le rôle original et le pré-sélectionner dans le spinner
            int roleIndex = c.getColumnIndex(DatabaseHelper.COL_USER_ROLE);
            if (roleIndex != -1) {
                originalRole = c.getString(roleIndex);
                setSpinnerSelection(spinnerRole, originalRole);
            }
            
            // Charger le sexe original et le pré-sélectionner dans le spinner
            int sexIndex = c.getColumnIndex(DatabaseHelper.COL_USER_SEX);
            if (sexIndex != -1) {
                originalSex = c.getString(sexIndex);
                setSpinnerSelection(spinnerSexe, originalSex);
            }
            
            // Charger la photo si elle existe
            int photoIndex = c.getColumnIndex(DatabaseHelper.COL_USER_PHOTO);
            if (photoIndex != -1) {
                String photoUri = c.getString(photoIndex);
                if (photoUri != null && !photoUri.isEmpty()) {
                    currentPhotoUri = photoUri;
                    loadPhotoFromUri(photoUri);
                }
            }

            c.close();
        }
    }
    
    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null && value != null) {
            // Mapper les valeurs de la DB aux valeurs du spinner si nécessaire
            String mappedValue = mapRoleToSpinnerValue(value);
            
            for (int i = 0; i < adapter.getCount(); i++) {
                String item = adapter.getItem(i).toString();
                // Comparaison insensible à la casse
                if (item.equalsIgnoreCase(mappedValue) || item.equalsIgnoreCase(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }
    
    private String mapRoleToSpinnerValue(String dbRole) {
        // Mapper les rôles de la DB aux valeurs du spinner
        if (dbRole == null) return "";
        
        // Les valeurs dans le spinner sont : Administrateur, Médecin, Patient, Secrétaire
        // Les valeurs dans la DB peuvent être : Admin, Médecin, Patient, Secrétaire
        if (dbRole.equalsIgnoreCase("Admin") || dbRole.equalsIgnoreCase("Administrateur")) {
            return "Administrateur";
        } else if (dbRole.equalsIgnoreCase("Médecin") || dbRole.equalsIgnoreCase("Medecin")) {
            return "Médecin";
        } else if (dbRole.equalsIgnoreCase("Patient")) {
            return "Patient";
        } else if (dbRole.equalsIgnoreCase("Secrétaire") || dbRole.equalsIgnoreCase("Secretaire")) {
            return "Secrétaire";
        }
        return dbRole;
    }
    
    private void loadPhotoFromUri(String uriString) {
        try {
            if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                Uri uri = Uri.parse(uriString);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                if (bitmap != null) {
                    ivUserPhoto.setImageBitmap(bitmap);
                }
            } else {
                // C'est un chemin de fichier
                File imgFile = new File(uriString);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    if (bitmap != null) {
                        ivUserPhoto.setImageBitmap(bitmap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;
            
            // Créer un fichier dans le cache de l'application
            File cacheDir = getCacheDir();
            File imageFile = new File(cacheDir, "user_photo_" + System.currentTimeMillis() + ".jpg");
            
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> inputDob.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveUser() {
        String name = inputNom.getText().toString();
        String email = inputEmail.getText().toString();
        String phone = inputPhone.getText().toString();
        String dob = inputDob.getText().toString();
        String address = inputAddress.getText().toString();
        String role = spinnerRole.getSelectedItem().toString();
        String sex = spinnerSexe.getSelectedItem().toString();
        
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        if (userId == null) {
            // New user: validation required
            if (password.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un mot de passe", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                return;
            }
            
            db.addUser(name, email, phone, role, sex, dob, address, password, currentPhotoUri);
            Toast.makeText(this, "Utilisateur ajouté", Toast.LENGTH_SHORT).show();
        } else {
            // Update user: validation if password is changed
            if (!password.isEmpty() && !password.equals(confirmPassword)) {
                 Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                 return;
            }
            
            // Préserver le rôle original lors de la modification (le rôle ne doit pas changer)
            String roleToUse = (!originalRole.isEmpty()) ? originalRole : role;
            
            db.updateUser(userId, name, email, phone, roleToUse, sex, dob, address, password, currentPhotoUri);
            Toast.makeText(this, "Utilisateur modifié", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
