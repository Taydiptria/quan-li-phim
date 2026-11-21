package com.example.quanliphim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanliphim.util.PreferenceManager;

public class ProfileActivity extends AppCompatActivity {

    TextView btnBack, tvProfileDisplayName, tvProfileDisplayEmail, btnChangeAvatar;
    ImageView imgProfileAvatar;
    EditText edtProfileName, edtProfileEmail, edtProfilePhone, edtProfileBio;
    Spinner spinnerProfileFavGenre;
    Button btnSaveProfile, btnNavStats, btnNavFavorites, btnNavWatched;

    RadioGroup rgThemeGroup;
    RadioButton rbThemeDark, rbThemeLight, rbThemeSystem;

    private boolean isInitializingTheme = true;

    private final String[] genreOptions = {
            "Hành động", "Tình cảm", "Hài hước", "Kinh dị",
            "Viễn tưởng", "Hoạt hình", "Tâm lý", "Phiêu lưu",
            "Võ thuật", "Tài liệu", "Gia đình", "Khác"
    };

    private final ActivityResultLauncher<String> pickAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {}

                    String avatarUriStr = uri.toString();
                    PreferenceManager.setUserAvatar(ProfileActivity.this, avatarUriStr);
                    displayAvatar(avatarUriStr);
                    Toast.makeText(ProfileActivity.this, "Đã cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.applySavedTheme(this);
        setContentView(R.layout.activity_profile);

        btnBack = findViewById(R.id.btnBack);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        tvProfileDisplayName = findViewById(R.id.tvProfileDisplayName);
        tvProfileDisplayEmail = findViewById(R.id.tvProfileDisplayEmail);

        edtProfileName = findViewById(R.id.edtProfileName);
        edtProfileEmail = findViewById(R.id.edtProfileEmail);
        edtProfilePhone = findViewById(R.id.edtProfilePhone);
        spinnerProfileFavGenre = findViewById(R.id.spinnerProfileFavGenre);
        edtProfileBio = findViewById(R.id.edtProfileBio);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        rgThemeGroup = findViewById(R.id.rgThemeGroup);
        rbThemeDark = findViewById(R.id.rbThemeDark);
        rbThemeLight = findViewById(R.id.rbThemeLight);
        rbThemeSystem = findViewById(R.id.rbThemeSystem);

        btnNavStats = findViewById(R.id.btnNavStats);
        btnNavFavorites = findViewById(R.id.btnNavFavorites);
        btnNavWatched = findViewById(R.id.btnNavWatched);

        setupGenreSpinner();

        btnBack.setOnClickListener(v -> finish());

        imgProfileAvatar.setOnClickListener(v -> pickAvatarLauncher.launch("image/*"));
        btnChangeAvatar.setOnClickListener(v -> pickAvatarLauncher.launch("image/*"));

        loadUserProfile();
        loadThemeSettings();

        btnSaveProfile.setOnClickListener(v -> saveUserProfile());

        rgThemeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (isInitializingTheme) return;

            int selectedMode;
            if (checkedId == R.id.rbThemeLight) {
                selectedMode = PreferenceManager.THEME_LIGHT;
            } else if (checkedId == R.id.rbThemeSystem) {
                selectedMode = PreferenceManager.THEME_SYSTEM;
            } else {
                selectedMode = PreferenceManager.THEME_DARK;
            }

            PreferenceManager.setThemeMode(ProfileActivity.this, selectedMode);
            Toast.makeText(ProfileActivity.this, "Đã áp dụng giao diện mới", Toast.LENGTH_SHORT).show();
        });

        btnNavStats.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        btnNavFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FavoriteActivity.class);
            startActivity(intent);
        });

        btnNavWatched.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WatchedActivity.class);
            startActivity(intent);
        });
    }

    private void setupGenreSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_theme,
                genreOptions
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_theme);
        spinnerProfileFavGenre.setAdapter(adapter);
    }

    private void loadUserProfile() {
        String name = PreferenceManager.getUserName(this);
        String email = PreferenceManager.getUserEmail(this);
        String phone = PreferenceManager.getUserPhone(this);
        String favGenre = PreferenceManager.getFavoriteGenre(this);
        String bio = PreferenceManager.getUserBio(this);
        String avatar = PreferenceManager.getUserAvatar(this);

        tvProfileDisplayName.setText(name);
        tvProfileDisplayEmail.setText(email);

        edtProfileName.setText(name);
        edtProfileEmail.setText(email);
        edtProfilePhone.setText(phone);
        edtProfileBio.setText(bio);

        if (favGenre != null) {
            for (int i = 0; i < genreOptions.length; i++) {
                if (genreOptions[i].equalsIgnoreCase(favGenre.trim())) {
                    spinnerProfileFavGenre.setSelection(i);
                    break;
                }
            }
        }

        displayAvatar(avatar);
    }

    private void displayAvatar(String avatarUriStr) {
        if (avatarUriStr != null && !avatarUriStr.trim().isEmpty()) {
            try {
                imgProfileAvatar.setImageURI(Uri.parse(avatarUriStr));
                imgProfileAvatar.setImageTintList(null);
                imgProfileAvatar.setPadding(0, 0, 0, 0);
                return;
            } catch (Exception ignored) {}
        }
        imgProfileAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
    }

    private void saveUserProfile() {
        String name = edtProfileName.getText().toString().trim();
        String email = edtProfileEmail.getText().toString().trim();
        String phone = edtProfilePhone.getText().toString().trim();
        String favGenre = spinnerProfileFavGenre.getSelectedItem() != null ? spinnerProfileFavGenre.getSelectedItem().toString() : "Hành động";
        String bio = edtProfileBio.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Họ và tên", Toast.LENGTH_SHORT).show();
            return;
        }

        PreferenceManager.setUserName(this, name);
        PreferenceManager.setUserEmail(this, email);
        PreferenceManager.setUserPhone(this, phone);
        PreferenceManager.setFavoriteGenre(this, favGenre);
        PreferenceManager.setUserBio(this, bio);

        tvProfileDisplayName.setText(name);
        tvProfileDisplayEmail.setText(email.isEmpty() ? "user@example.com" : email);

        Toast.makeText(this, "Đã lưu thông tin cá nhân thành công!", Toast.LENGTH_SHORT).show();
    }

    private void loadThemeSettings() {
        isInitializingTheme = true;
        int mode = PreferenceManager.getThemeMode(this);

        if (mode == PreferenceManager.THEME_LIGHT) {
            rbThemeLight.setChecked(true);
        } else if (mode == PreferenceManager.THEME_SYSTEM) {
            rbThemeSystem.setChecked(true);
        } else {
            rbThemeDark.setChecked(true);
        }
        isInitializingTheme = false;
    }
}
