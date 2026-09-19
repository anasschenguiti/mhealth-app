package com.example.application_final.patient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.application_final.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class HospitalsMapActivity extends AppCompatActivity {

    private MapView map = null;
    private MyLocationNewOverlay locationOverlay;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<GeoPoint> hospitalLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize osmdroid configuration
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));

        setContentView(R.layout.activity_hospitals_map);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Vérifier et demander les permissions de localisation
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            initializeMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else {
                Toast.makeText(this, "Permission de localisation refusée. La carte utilisera une position par défaut.", Toast.LENGTH_LONG).show();
                // Position par défaut si permission refusée
                GeoPoint defaultPoint = new GeoPoint(48.8566, 2.3522);
                map.getController().setZoom(15.0);
                map.getController().setCenter(defaultPoint);
            }
        }
    }

    private void initializeMap() {
        // Créer l'overlay de localisation
        locationOverlay = new MyLocationNewOverlay(
                new org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);

        // Attendre que la position soit disponible
        locationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Location myLocation = locationOverlay.getLastFix();
                        if (myLocation != null) {
                            GeoPoint myPosition = new GeoPoint(myLocation.getLatitude(), myLocation.getLongitude());
                            map.getController().setZoom(15.0);
                            map.getController().setCenter(myPosition);
                            
                            // Ajouter un marqueur pour la position de l'utilisateur
                            Marker userMarker = new Marker(map);
                            userMarker.setPosition(myPosition);
                            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            userMarker.setTitle("Votre position");
                            map.getOverlays().add(userMarker);
                            
                            // Chercher les hôpitaux proches
                            findNearbyHospitals(myPosition);
                        } else {
                            // Si pas de position, utiliser Paris par défaut
                            GeoPoint defaultPoint = new GeoPoint(48.8566, 2.3522);
                            map.getController().setZoom(15.0);
                            map.getController().setCenter(defaultPoint);
                            findNearbyHospitals(defaultPoint);
                        }
                    }
                });
            }
        });
    }

    private void findNearbyHospitals(GeoPoint userLocation) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Utiliser Overpass API pour trouver les hôpitaux dans un rayon de 5km
                    double lat = userLocation.getLatitude();
                    double lon = userLocation.getLongitude();
                    double radius = 5000; // 5km en mètres
                    
                    String overpassUrl = "https://overpass-api.de/api/interpreter?data=" +
                            "[out:json][timeout:25];" +
                            "(" +
                            "  node[\"amenity\"=\"hospital\"](around:" + radius + "," + lat + "," + lon + ");" +
                            "  way[\"amenity\"=\"hospital\"](around:" + radius + "," + lat + "," + lon + ");" +
                            "  relation[\"amenity\"=\"hospital\"](around:" + radius + "," + lat + "," + lon + ");" +
                            ");" +
                            "out center meta;";
                    
                    URL url = new URL(overpassUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray elements = jsonResponse.getJSONArray("elements");
                    
                    hospitalLocations.clear();
                    
                    for (int i = 0; i < elements.length() && i < 10; i++) { // Limiter à 10 hôpitaux
                        JSONObject element = elements.getJSONObject(i);
                        
                        double hospitalLat, hospitalLon;
                        String name = "Hôpital";
                        
                        if (element.has("center")) {
                            JSONObject center = element.getJSONObject("center");
                            hospitalLat = center.getDouble("lat");
                            hospitalLon = center.getDouble("lon");
                        } else {
                            hospitalLat = element.getDouble("lat");
                            hospitalLon = element.getDouble("lon");
                        }
                        
                        if (element.has("tags")) {
                            JSONObject tags = element.getJSONObject("tags");
                            if (tags.has("name")) {
                                name = tags.getString("name");
                            }
                        }
                        
                        hospitalLocations.add(new GeoPoint(hospitalLat, hospitalLon));
                        
                        // Ajouter le marqueur sur le thread UI
                        final GeoPoint hospitalPoint = new GeoPoint(hospitalLat, hospitalLon);
                        final String hospitalName = name;
                        
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Marker hospitalMarker = new Marker(map);
                                hospitalMarker.setPosition(hospitalPoint);
                                hospitalMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                hospitalMarker.setTitle(hospitalName);
                                // Utiliser l'icône par défaut du marqueur (croix rouge)
                                map.getOverlays().add(hospitalMarker);
                                map.invalidate();
                            }
                        });
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HospitalsMapActivity.this, 
                                    "Erreur lors de la recherche d'hôpitaux", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }
}
