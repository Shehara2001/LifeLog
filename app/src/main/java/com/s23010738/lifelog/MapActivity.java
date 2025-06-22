package com.s23010738.lifelog;



import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

        private GoogleMap mMap;
        private EditText etSearch;
        private Button btnAll, btnLibraries, btnCinemas, btnCafes, btnSearch;
        private String selectedCategory = "All";
        private Marker searchMarker = null;
        private boolean isSearchActive = false;
        private LatLng lastSearchLatLng = null;

        // Place data structure
        private static class Place {
            LatLng latLng;
            String title;
            String category;
            Place(LatLng latLng, String title, String category) {
                this.latLng = latLng;
                this.title = title;
                this.category = category;
            }
        }

        private final List<Place> allPlaces = new ArrayList<>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map);

            initViews();
            populateSamplePlaces();
            setupMapFragment();
            setupClickListeners();
        }

        private void initViews() {
            etSearch = findViewById(R.id.etSearch);
            btnAll = findViewById(R.id.btnAll);
            btnLibraries = findViewById(R.id.btnLibraries);
            btnCinemas = findViewById(R.id.btnCinemas);
            btnCafes = findViewById(R.id.btnCafes);
            btnSearch = findViewById(R.id.btnSearch);
        }

        private void populateSamplePlaces() {
            allPlaces.clear();
            // Libraries
            allPlaces.add(new Place(new LatLng(1.2966, 103.7764), "National Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(1.3048, 103.8318), "Central Library", "Libraries"));
            // Cinemas
            allPlaces.add(new Place(new LatLng(1.3644, 103.9915), "Changi City Point Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(1.2833, 103.8607), "Marina Bay Cinema", "Cinemas"));
            // Cafes
            allPlaces.add(new Place(new LatLng(1.3138, 103.8159), "Orchard Road Cafe", "Cafes"));
            allPlaces.add(new Place(new LatLng(1.2675, 103.8014), "Chinatown Cafe", "Cafes"));
        }

        private void setupMapFragment() {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapFragment);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }

        private void setupClickListeners() {
            btnAll.setOnClickListener(v -> selectCategory("All", btnAll));
            btnLibraries.setOnClickListener(v -> selectCategory("Libraries", btnLibraries));
            btnCinemas.setOnClickListener(v -> selectCategory("Cinemas", btnCinemas));
            btnCafes.setOnClickListener(v -> selectCategory("Cafes", btnCafes));

            btnSearch.setOnClickListener(v -> performSearch());
        }

        private void selectCategory(String category, Button selectedButton) {
            selectedCategory = category;

            // Reset all buttons to unselected state
            resetButtonStyles();

            // Set selected button style
            selectedButton.setBackgroundResource(R.drawable.button_selected);
            selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));

            // If search is active, keep search marker and show filtered places near search
            // If not, just show all places for the category
            updateMapMarkers();
        }

        private void resetButtonStyles() {
            Button[] buttons = {btnAll, btnLibraries, btnCinemas, btnCafes};

            for (Button button : buttons) {
                button.setBackgroundResource(R.drawable.button_unselected);
                button.setTextColor(ContextCompat.getColor(this, R.color.button_text_unselected));
            }
        }

        private void updateMapMarkers() {
            if (mMap == null) return;
            mMap.clear();

            if (isSearchActive && lastSearchLatLng != null) {
                // Show search marker
                searchMarker = mMap.addMarker(new MarkerOptions().position(lastSearchLatLng).title(etSearch.getText().toString().trim()));
                // Show only filtered places near searched location (3km)
                addNearbyCategoryMarkers(lastSearchLatLng, selectedCategory, 3000);
            } else {
                // Show all places for the selected category
                addCategoryMarkers(selectedCategory);
            }
        }

        private void addCategoryMarkers(String category) {
            for (Place place : allPlaces) {
                if (category.equals("All") || place.category.equals(category)) {
                    mMap.addMarker(new MarkerOptions().position(place.latLng).title(place.title));
                }
            }
        }

        private void addNearbyCategoryMarkers(LatLng center, String category, double radiusMeters) {
            for (Place place : allPlaces) {
                // Only show places matching the selected category (or all if "All" is selected)
                if ((category.equals("All") || place.category.equals(category)) &&
                    distanceBetween(center, place.latLng) <= radiusMeters) {
                    mMap.addMarker(new MarkerOptions().position(place.latLng).title(place.title));
                }
            }
        }

        // Haversine formula for distance in meters
        private double distanceBetween(LatLng a, LatLng b) {
            double earthRadius = 6371000; // meters
            double dLat = Math.toRadians(b.latitude - a.latitude);
            double dLng = Math.toRadians(b.longitude - a.longitude);
            double sindLat = Math.sin(dLat / 2);
            double sindLng = Math.sin(dLng / 2);
            double va = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                    * Math.cos(Math.toRadians(a.latitude)) * Math.cos(Math.toRadians(b.latitude));
            double vc = 2 * Math.atan2(Math.sqrt(va), Math.sqrt(1 - va));
            return earthRadius * vc;
        }

        private void filterMapMarkers(String category) {
            updateMapMarkers();
        }

        private void performSearch() {
            String searchQuery = etSearch.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                searchPlaces(searchQuery);
            }
        }

        private void searchPlaces(String query) {
            if (mMap == null) return;

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(query, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    lastSearchLatLng = latLng;
                    isSearchActive = true;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    updateMapMarkers();
                } else {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(this, "Geocoding failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // Set default location (Singapore as per user location)
            LatLng singapore = new LatLng(1.3521, 103.8198);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 12));

            updateMapMarkers();

            // Enable map controls
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
        }
    }