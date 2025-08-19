package com.s23010738.lifelog;



import android.content.Intent;
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

            // Hide system navigation bar for immersive fullscreen
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            );
            // Bottom navigation setup
            findViewById(R.id.navLocation).setOnClickListener(v -> {
                startActivity(new Intent(MapActivity.this, DashboardActivity.class));
            });
            findViewById(R.id.goal).setOnClickListener(v -> {
                startActivity(new Intent(MapActivity.this, ActivityGoal.class));
            });
            findViewById(R.id.navProfile).setOnClickListener(v -> {
                startActivity(new Intent(MapActivity.this, MapActivity.class));
            });
            findViewById(R.id.navScan).setOnClickListener(v -> {
                startActivity(new Intent(MapActivity.this, CalendarActivity.class));
            });
            findViewById(R.id.navSetting).setOnClickListener(v -> {
                startActivity(new Intent(MapActivity.this, YearReviewActivity.class));
            });

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
            // Colombo
            allPlaces.add(new Place(new LatLng(6.927079, 79.861244), "Colombo Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.927497, 79.864136), "Savoy Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.927265, 79.861451), "Cafe Kumbuk", "Cafes"));
            allPlaces.add(new Place(new LatLng(6.9214, 79.8612), "National Library of Sri Lanka", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.9272, 79.8650), "Liberty Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.9278, 79.8615), "The Commons Coffee House", "Cafes"));
            // Nawala (sub-city)
            allPlaces.add(new Place(new LatLng(6.8855, 79.9022), "Nawala Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.8861, 79.9027), "Regal Cinema Nawala", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.8867, 79.9018), "Java Lounge Nawala", "Cafes"));
            allPlaces.add(new Place(new LatLng(6.8870, 79.9030), "Nawala Community Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.8858, 79.9025), "Cafe Nawala", "Cafes"));
            // Kandy
            allPlaces.add(new Place(new LatLng(7.2936, 80.6411), "Kandy Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(7.2926, 80.6387), "Cine Star Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(7.2906, 80.6337), "Cafe Walk", "Cafes"));
            allPlaces.add(new Place(new LatLng(7.2940, 80.6415), "Central Library Kandy", "Libraries"));
            allPlaces.add(new Place(new LatLng(7.2920, 80.6380), "Kandy City Centre Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(7.2910, 80.6340), "Cafe Aroma Inn", "Cafes"));
            // Galle
            allPlaces.add(new Place(new LatLng(6.0373, 80.2170), "Galle Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.0351, 80.2170), "Regal Cinema Galle", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.0330, 80.2170), "Pedlar's Inn Cafe", "Cafes"));
            allPlaces.add(new Place(new LatLng(6.0360, 80.2175), "Galle Fort Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.0340, 80.2180), "Fortaleza Cafe", "Cafes"));
            // Jaffna
            allPlaces.add(new Place(new LatLng(9.6615, 80.0255), "Jaffna Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(9.6680, 80.0100), "Cineplex Jaffna", "Cinemas"));
            allPlaces.add(new Place(new LatLng(9.6680, 80.0200), "Rio Ice Cream Cafe", "Cafes"));
            allPlaces.add(new Place(new LatLng(9.6618, 80.0258), "Jaffna Central Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(9.6620, 80.0260), "Malayan Cafe", "Cafes"));
            // Negombo
            allPlaces.add(new Place(new LatLng(7.2075, 79.8380), "Negombo Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(7.2256, 79.8417), "Negombo Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(7.2095, 79.8412), "Cafe J", "Cafes"));
            // Matara
            allPlaces.add(new Place(new LatLng(5.9549, 80.5549), "Matara Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(5.9485, 80.5353), "Regal Cinema Matara", "Cinemas"));
            allPlaces.add(new Place(new LatLng(5.9487, 80.5355), "The Dutchman's Street Cafe", "Cafes"));
            // Kurunegala
            allPlaces.add(new Place(new LatLng(7.4863, 80.3647), "Kurunegala Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(7.4863, 80.3627), "Kurunegala Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(7.4870, 80.3620), "Cafe 80", "Cafes"));
            // Anuradhapura
            allPlaces.add(new Place(new LatLng(8.3114, 80.4037), "Anuradhapura Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(8.3120, 80.4030), "Anuradhapura Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(8.3125, 80.4040), "Ceylon Cafe", "Cafes"));
            // Batticaloa
            allPlaces.add(new Place(new LatLng(7.7102, 81.6924), "Batticaloa Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(7.7105, 81.6930), "Batticaloa Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(7.7108, 81.6935), "Cafe Chill", "Cafes"));
            // Dehiwala
            allPlaces.add(new Place(new LatLng(6.8659, 79.8997), "Dehiwala Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.8665, 79.9002), "Dehiwala Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.8670, 79.9007), "Cafe on the Fifth", "Cafes"));
            // Moratuwa
            allPlaces.add(new Place(new LatLng(6.7730, 79.8816), "Moratuwa Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.7735, 79.8820), "Moratuwa Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.7740, 79.8825), "Cafe Moratuwa", "Cafes"));
            // Ratnapura
            allPlaces.add(new Place(new LatLng(6.6828, 80.4037), "Ratnapura Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.6830, 80.4040), "Ratnapura Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.6832, 80.4043), "Gem Cafe", "Cafes"));
            // Dambulla
            allPlaces.add(new Place(new LatLng(7.8731, 80.6511), "Dambulla Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(7.8740, 80.6520), "Dambulla Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(7.8750, 80.6530), "Cafe Dambulla", "Cafes"));
            // Unawatuna
            allPlaces.add(new Place(new LatLng(6.0182, 80.2444), "Unawatuna Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.0190, 80.2450), "Unawatuna Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.0200, 80.2460), "Kingfisher Cafe", "Cafes"));
            // Wellawatte
            allPlaces.add(new Place(new LatLng(6.8770, 79.8600), "Wellawatte Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.8780, 79.8610), "Wellawatte Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.8790, 79.8620), "Cafe Wellawatte", "Cafes"));
            // Hikkaduwa
            allPlaces.add(new Place(new LatLng(6.1390, 80.1037), "Hikkaduwa Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.1400, 80.1040), "Hikkaduwa Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.1410, 80.1050), "Salty Swamis Cafe", "Cafes"));
            // Trincomalee
            allPlaces.add(new Place(new LatLng(8.5874, 81.2152), "Trincomalee Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(8.5880, 81.2160), "Trincomalee Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(8.5890, 81.2170), "Cafe Trinco", "Cafes"));
            // Maharagama
            allPlaces.add(new Place(new LatLng(6.8451, 79.9244), "Maharagama Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.8460, 79.9250), "Maharagama Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.8470, 79.9260), "Cafe Maharagama", "Cafes"));
            // Kadugannawa
            allPlaces.add(new Place(new LatLng(7.2513, 80.3489), "Kadugannawa Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(7.2520, 80.3495), "Kadugannawa Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(7.2530, 80.3500), "Cafe Kadugannawa", "Cafes"));
            // Panadura
            allPlaces.add(new Place(new LatLng(6.7131, 79.9020), "Panadura Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.7140, 79.9030), "Panadura Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.7150, 79.9040), "Cafe Panadura", "Cafes"));
            // Peradeniya
            allPlaces.add(new Place(new LatLng(7.2715, 80.5981), "Peradeniya Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(7.2720, 80.5990), "Peradeniya Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(7.2730, 80.6000), "Cafe Peradeniya", "Cafes"));
            // Bambalapitiya
            allPlaces.add(new Place(new LatLng(6.9000, 79.8550), "Bambalapitiya Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(6.9010, 79.8560), "Bambalapitiya Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(6.9020, 79.8570), "Cafe Bambalapitiya", "Cafes"));
            // Mawanella
            allPlaces.add(new Place(new LatLng(7.3397, 80.3867), "Mawanella Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(7.3400, 80.3870), "Mawanella Cinema", "Cinemas"));
            allPlaces.add(new Place(new LatLng(7.3410, 80.3880), "Cafe Mawanella", "Cafes"));
        }

        private void addCityPlaces(double lat, double lng, String city) {
            allPlaces.add(new Place(new LatLng(lat, lng), city + " Public Library", "Libraries"));
            allPlaces.add(new Place(new LatLng(lat + 0.005, lng + 0.005), city + " Cinema Hall", "Cinemas"));
            allPlaces.add(new Place(new LatLng(lat - 0.005, lng - 0.005), city + " Cafe", "Cafes"));
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
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                    // Show only places within 7km of searched city
                    addNearbyCategoryMarkers(latLng, selectedCategory, 7000);
                    // Show search marker
                    searchMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(query));
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