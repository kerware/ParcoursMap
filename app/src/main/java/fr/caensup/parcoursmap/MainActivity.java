package fr.caensup.parcoursmap;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etLatitude;
    private EditText etLongitude;
    private Button btRafraichir;

    private CheckBox cbParcours;
    private TextView tvDistance;
    private MapView map;

    private boolean modeParcours = false;

    private List<GeoPoint> listeGeoPoints;
    private Polyline parcours;
    private MapEventsOverlay mapEvent;
    private static final double  ZOOM_FACTOR=17.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load osmdroid configuration
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));

        // Set preferences for OpenStreetMap
        Configuration.getInstance().setUserAgentValue(getPackageName()); // Recommended to avoid server blocking
        Configuration.getInstance().setOsmdroidBasePath(new File(getCacheDir(), "osmdroid")); // Define cache path
        Configuration.getInstance().setOsmdroidTileCache(new File(getCacheDir(), "osmdroid/tiles"));


        etLatitude = findViewById( R.id.etLatitude );
        etLongitude = findViewById( R.id.etLongitude);
        btRafraichir = findViewById( R.id.btRafraichir );
        cbParcours = findViewById( R.id.cbParcours );
        tvDistance = findViewById( R.id.tvDistance );
        map = findViewById( R.id.map );

        btRafraichir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 double lat = Double.parseDouble(String.valueOf(etLatitude.getText()));
                 double lon = Double.parseDouble(String.valueOf(etLongitude.getText()));
                 setMapToLocation( lat , lon );
            }
        });

        // Gestion du clic sur le case a cocher
        cbParcours.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                modeParcours = isChecked;
                if ( modeParcours ) {
                    listeGeoPoints = new ArrayList<>();
                    parcours = new Polyline();
                    parcours.getOutlinePaint().setColor(Color.BLUE);
                    parcours.getOutlinePaint().setStrokeWidth( 8.0f);
                    map.getOverlayManager().add( parcours );
                }
            }
        });

        mapEvent = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (modeParcours) {
                    listeGeoPoints.add( p );
                    parcours.setPoints( listeGeoPoints );
                    map.invalidate();
                    // Calcul de la nouvelle distance en m
                    tvDistance.setText("Distance en m : " + calculDistanceParcours());
                }
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });

        map.getOverlays().add( mapEvent );
    }

    private void setMapToLocation( double lat, double lon ) {
        map.setTileSource(TileSourceFactory.MAPNIK);  // MAPNIK est la source standard d'OSM
        // Activer le contrôle de zoom
        map.setMultiTouchControls( true );
        GeoPoint point = new GeoPoint( lat, lon );
        map.getController().setCenter( point );
        map.getController().setZoom( ZOOM_FACTOR );
        map.invalidate();
    }

    private double calculDistanceParcours() {
        double distance = 0.0;
        GeoPoint dernierPoint = listeGeoPoints.get(0);
        for( GeoPoint p : listeGeoPoints ) {
            distance += dernierPoint.distanceToAsDouble( p );
            dernierPoint = p;
        }
        return distance;
    }

}