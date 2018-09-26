package id.slametriyadi.ojekbeneran;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.slametriyadi.ojekbeneran.helper.GPSTracker;
import id.slametriyadi.ojekbeneran.helper.HeroHelper;
import id.slametriyadi.ojekbeneran.model.google.Distance;
import id.slametriyadi.ojekbeneran.model.google.Duration;
import id.slametriyadi.ojekbeneran.model.google.LegsItem;
import id.slametriyadi.ojekbeneran.model.google.ResponseGoogeDirections;
import id.slametriyadi.ojekbeneran.model.google.RoutesItem;
import id.slametriyadi.ojekbeneran.network.InitRetrofit;
import id.slametriyadi.ojekbeneran.network.RestApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static id.slametriyadi.ojekbeneran.helper.MyContants.LOKASIAWAL;
import static id.slametriyadi.ojekbeneran.helper.MyContants.LOKASITUJUAN;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    @BindView(R.id.locawal)
    TextView locawal;
    @BindView(R.id.locakhir)
    TextView locakhir;
    @BindView(R.id.btnOrder)
    Button btnOrder;
    @BindView(R.id.spinner)
    MaterialSpinner spinner;
    @BindView(R.id.btnHistory)
    Button btnHistory;
    @BindView(R.id.txtHarga)
    TextView txtHarga;
    @BindView(R.id.txtJarak)
    TextView txtJarak;
    @BindView(R.id.txtDuration)
    TextView txtDuration;
    @BindView(R.id.txtTujuan)
    TextView txtTujuan;
    @BindView(R.id.btnOrderNow)
    Button btnOrderNow;

    private GoogleMap mMap;
    GPSTracker gpsTracker;
    private double latawal, latakhir, lonawal, lonakhir;
    String namelocationawal, namelocationakhir;
    LatLng lokasiAwal, lokasiakhir;
    private String points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // TODO Cek Gps Nyala/Tidak
        checkGps();

        spinner.setItems("Normal", "Hybrid", "Terrain");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                switch (position) {
                    case 0:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 2:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                }
            }
        });
    }

    private void checkGps() {
        final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Gps Already Enabled", Toast.LENGTH_SHORT).show();
        }
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Gps Not Enabled", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        gpsTracker = new GPSTracker(this);

        // TODO Cek Permission >= Marshmellow
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                }, 110);
                return;
            }
        }

        if (gpsTracker.canGetLocation()) {
            latawal = gpsTracker.getLatitude();
            lonawal = gpsTracker.getLongitude();
            namelocationawal = myLocation(latawal, lonawal);
        }

        // Add a marker in Sydney and move the camera
        LatLng myLocation = new LatLng(latawal, lonawal);
        locawal.setText(namelocationawal);
        mMap.addMarker(new MarkerOptions().position(myLocation).title(namelocationawal));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setPadding(30, 80, 30, 80);
    }

    private String myLocation(double latawal, double lonawal) {
        namelocationawal = null;
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> list = geocoder.getFromLocation(latawal, lonawal, 1);
            if (list != null && list.size() != 0) {
                namelocationawal = list.get(0).getAddressLine(0) + "" + list.get(0).getCountryName();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return namelocationawal;
    }

    @OnClick({R.id.locawal, R.id.locakhir, R.id.btnOrder, R.id.spinner})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.locawal:
                setLokasi(LOKASIAWAL);
                break;
            case R.id.locakhir:
                setLokasi(LOKASITUJUAN);
                break;
            case R.id.btnOrder:
                break;
            case R.id.spinner:
                break;
        }
    }

    private void setLokasi(int lokasi) {
        AutocompleteFilter regionFilter = new AutocompleteFilter
                .Builder().setCountry("ID").build();

        Intent autoComplete = null;
        try {
            autoComplete = new PlaceAutocomplete.
                    IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(regionFilter)
                    .build(MapsActivity.this);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
        startActivityForResult(autoComplete, lokasi);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOKASIAWAL) {
            if (resultCode == RESULT_OK && data != null) {
                Place p = PlaceAutocomplete.getPlace(this, data);
                latawal = p.getLatLng().latitude;
                lonawal = p.getLatLng().longitude;

                lokasiAwal = new LatLng(latawal, lonawal);
//                lokasiakhir = new LatLng(lonawal, lonakhir);
                namelocationawal = p.getAddress().toString();

                mMap.clear();
                mMap.addMarker(new MarkerOptions().title(namelocationawal).position(lokasiAwal));
//                mMap.addMarker(new MarkerOptions().title(namelocationakhir).position(lokasiakhir));
                mMap.setPadding(100, 290, 100, 100);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiAwal, 16));
                locawal.setText(namelocationawal);
            }
        } else if (requestCode == LOKASITUJUAN) {
            if (resultCode == RESULT_OK && data != null) {
                Place p = PlaceAutocomplete.getPlace(this, data);
                latakhir = p.getLatLng().latitude;
                lonakhir = p.getLatLng().longitude;

                lokasiakhir = new LatLng(latakhir, lonakhir);
                lokasiAwal = new LatLng(latawal, lonawal);
                namelocationakhir = p.getAddress().toString();

                mMap.clear();
                mMap.addMarker(new MarkerOptions().title(namelocationakhir).position(lokasiakhir));
                mMap.addMarker(new MarkerOptions().title(namelocationawal).position(lokasiAwal));
                mMap.setPadding(100, 290, 100, 100);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiakhir, 16));
                locakhir.setText(namelocationakhir);
                createRoute();
            }
        }
    }

    private void createRoute() {
        String origin = String.valueOf(latawal) + "," + String.valueOf(lonawal);
        String destination = String.valueOf(latakhir) + "," + String.valueOf(lonakhir);

        LatLngBounds.Builder bound = LatLngBounds.builder();
        bound.include(new LatLng(latawal, lonawal));
        bound.include(new LatLng(latakhir, lonakhir));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bound.build(), 16));

        RestApi api = InitRetrofit.getInstanceGoogle();
        Call<ResponseGoogeDirections> call = api.getRouteLocation(origin, destination);
        call.enqueue(new Callback<ResponseGoogeDirections>() {
            @Override
            public void onResponse(Call<ResponseGoogeDirections> call, Response<ResponseGoogeDirections> response) {
                if (response.isSuccessful()) {
                    if (response.body().getStatus().equals("OK")) {
                        List<RoutesItem> routes = response.body().getRoutes();
                        List<LegsItem> legs = routes.get(0).getLegs();
                        Distance distance = legs.get(0).getDistance();
                        Duration duration = legs.get(0).getDuration();

                        txtJarak.setText(distance.getText());
                        txtDuration.setText(duration.getText());
                        txtTujuan.setText(namelocationakhir);

                        double nilaiJarak = Double.valueOf(distance.getValue());
                        double harga = Math.ceil(nilaiJarak / 1000);
                        double total = harga * 2000;

                        txtHarga.setText("Rp. " + HeroHelper.toRupiahFormat2(String.valueOf(total)));
                        points = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseGoogeDirections> call, Throwable t) {

            }
        });
    }

    @OnClick(R.id.btnOrderNow)
    public void onViewClicked() {
    }
}
