package com.tourism.hesham.rentapp;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import handling.recycler.RecyclerAdapter;
import handling.recycler.Recycler_Listener;

public class SearchActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks ,
        GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient googleApiClient;

    private static final LatLngBounds myBounds = new LatLngBounds(
            new LatLng(-0,0),new LatLng(0,0));

    private RecyclerView mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerAdapter recyclerAdapter;
    private EditText search_editText;
    private ImageView search_img;

    final static String fixedHttp = "https://maps.googleapis.com/maps/api/geocode/json?";
    final static String apiKey = "AIzaSyB9m1fot-VHreEUQxeMNQaF3RJ92VL6f_0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        buildGoogleApiClient();

        Initializing();
        animating();
        AutocompleteApi();





    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting() ){
            googleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
    }

    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(),MapsActivity.class));
    }



    private void Initializing(){
        search_editText = (EditText)findViewById(R.id.search_editText);
        search_img = (ImageView)findViewById(R.id.search_img);
        recyclerAdapter = new RecyclerAdapter(this, R.layout.recycler_row , googleApiClient, myBounds , null);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerId);
        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    private void animating(){

//        search_editText.animate().alpha(1.0f).y(300f).rotation(360).setDuration(3000);
//        search_img.animate().y(310f).setDuration(3000);

    }


    private void  AutocompleteApi(){

        search_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!s.toString().equals("") && googleApiClient.isConnected()){
                    recyclerAdapter.getFilter().filter(s.toString());
                }
                else if(!googleApiClient.isConnected()){
                    Toast.makeText(SearchActivity.this, "Google API Client is not connected", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mRecyclerView.addOnItemTouchListener(

                new Recycler_Listener(this, new Recycler_Listener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view , int position) {
                        final RecyclerAdapter.AT_Place item = recyclerAdapter.getItem(position);
                        final String placeId = String.valueOf(item.placeId);

                        /*
                             Issue a request to the Places Geo Data API to retrieve a Place object with additional details about the place.
                         */

                        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                                .getPlaceById(googleApiClient, placeId);
                        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if(places.getCount()==1){
                                    //Do the things here on Click.....
                                    Toast.makeText(getApplicationContext(), "please click on Search button on the left ..",Toast.LENGTH_SHORT).show();

                                    //LatLng latLng = String.valueOf(places.get(0).getLatLng()) ;

                                    //////////// hena bageb el latitude w el longitude w ab3thom lel map !! aw a7sn a5ally el edit text yeb2a feh el address
                                    search_editText.setText(String.valueOf(places.get(0).getAddress()));
                                    mRecyclerView.setVisibility(View.GONE);

                                }else {
                                    Toast.makeText(getApplicationContext(),"Something went wrong !",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        Log.i("TAG", "Clicked: " + item.description);
                        Log.i("TAG", "Called getPlaceById to get Place details for " + item.placeId);
                    }
                })
        );


    }


    public void findAddress(View view) {

        //hn5ally el keyboard te5tfy b3d ma el user y5allas ketaba

        InputMethodManager inputMethod = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethod.hideSoftInputFromWindow(search_editText.getWindowToken(),0);

        try {

            if (search_editText.length() == 0) {
                Log.i("Empty" , "EditText is Empty");
                Toast.makeText(this, "Please enter the address you want to search for ..", Toast.LENGTH_SHORT).show();
            } else {


                //hn5ally el search editText yeb2a feh + ben kol kelma fl address (n7welha le URL form)

                String encodedAddress = URLEncoder.encode(search_editText.getText().toString(), "UTF-8");
                String  httpWeb = fixedHttp + "address=" + encodedAddress + "&key=" + apiKey;

                Log.i("httpWeb", httpWeb);

                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);

                // I will send the httpWeb which contains the full http site for what the user searchs for ..

                intent.putExtra("httpWeb" , httpWeb);
                search_editText.getText().clear();
                startActivity(intent);
                finish();

            }
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
            Toast.makeText(this, "Please enter the address you want to search for ..", Toast.LENGTH_SHORT).show();
        }


    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
