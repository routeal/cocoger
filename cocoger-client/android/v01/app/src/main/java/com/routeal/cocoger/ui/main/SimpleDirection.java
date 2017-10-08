package com.routeal.cocoger.ui.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nabe on 9/24/17.
 */

class SimpleDirection {
    private final static String TAG = "SimpleDirection";
    private GoogleMap mMap;
    private InfoWindowManager mInfoWindowManager;
    private SimpleDirectionRoute mDirectionRoute = new SimpleDirectionRoute();
    private GoogleMap.OnPolylineClickListener mPolylineClickListener = new GoogleMap.OnPolylineClickListener() {
        @Override
        public void onPolylineClick(Polyline polyline) {
            if (mDirectionRoute.line != null && mDirectionRoute.window != null) {
                if (polyline.getId().equals(mDirectionRoute.line.getId())) {
                    mInfoWindowManager.toggle(mDirectionRoute.window, true);
                }
            }
        }
    };

    SimpleDirection(GoogleMap googleMap, InfoWindowManager infoWindowManager) {
        mMap = googleMap;
        mMap.setOnPolylineClickListener(mPolylineClickListener);
        mInfoWindowManager = infoWindowManager;
    }

    private static void getDirection(Location to, Location from, SimpleDirectionListener listener) {
        DownloadDirection DownloadDirection = new DownloadDirection(listener);

        String url = getUrl(from, to);

        // Start downloading json data from Google Directions API
        DownloadDirection.execute(url);
    }

    private static String getUrl(Location origin, Location dest) {

        // Origin of route
        String str_origin = "origin=" + origin.getLatitude() + "," + origin.getLongitude();

        // Destination of route
        String str_dest = "destination=" + dest.getLatitude() + "," + dest.getLongitude();

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private static String downloadUrl(String strUrl) throws Exception {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            //Log.d(TAG, "downloadUrl = " + data.toString());
            br.close();
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    void addDirection(final Activity activity, final Location locationTo, final Location locationFrom) {
        if (activity.isFinishing()) {
            return;
        }
        if (Utils.distanceTo(locationFrom, locationTo) < 100) {
            Toast.makeText(activity, R.string.too_short_direction, Toast.LENGTH_SHORT).show();
            /*
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.direction)
                    .setMessage(R.string.too_short_direction)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            */
            return;
        }
        removeDirection();
        SimpleDirection.getDirection(locationTo, locationFrom, new SimpleDirection.SimpleDirectionListener() {
            @Override
            public void onSuccess(List<SimpleDirection.Route> routes) {
                SimpleDirection.Route route = routes.get(0);
                PolylineOptions lineOptions = new PolylineOptions();
                lineOptions.addAll(route.points);
                lineOptions.width(10);
                lineOptions.color(ContextCompat.getColor(activity, R.color.dodgerblue));
                mDirectionRoute.line = mMap.addPolyline(lineOptions);
                mDirectionRoute.line.setClickable(true);

                //
                int n = route.points.size() / 2;
                LatLng pos = route.points.get(n);
                Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.TRANSPARENT);
                BitmapDescriptor transparent = BitmapDescriptorFactory.fromBitmap(bitmap);

                MarkerOptions options = new MarkerOptions()
                        .position(pos)
                        .icon(transparent)
                        .anchor((float) 0.5, (float) 0.5);
                mDirectionRoute.marker = mMap.addMarker(options);

                DirectionInfoFragment dif = new DirectionInfoFragment();
                dif.setDistance(route.distance);
                dif.setDuration(route.duration);
                dif.setDestination(locationTo);
                InfoWindow.MarkerSpecification mMarkerOffset = new InfoWindow.MarkerSpecification(0, 0);
                mDirectionRoute.window = new InfoWindow(mDirectionRoute.marker, mMarkerOffset, dif);
                mInfoWindowManager.setHideOnFling(true);
                mInfoWindowManager.toggle(mDirectionRoute.window, true);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(Utils.getLatLng(locationTo));
                builder.include(Utils.getLatLng(locationFrom));
                LatLngBounds bounds = builder.build();
                int padding = 200; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.moveCamera(cu);
            }

            @Override
            public void onFail(String err) {
                String message = MainApplication.getContext().getResources().getString(R.string.no_direction_available);
                if (err != null) {
                    if (!err.isEmpty()) {
                        message += " (" + err + ")";
                    }
                }
                Toast.makeText(MainApplication.getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void removeDirection() {
        if (mDirectionRoute.line != null) {
            mDirectionRoute.line.remove();
            mDirectionRoute.line = null;
        }
        if (mDirectionRoute.window != null) {
            mInfoWindowManager.hide(mDirectionRoute.window);
            Fragment fragment = mDirectionRoute.window.getWindowFragment();
            FragmentManager fragmentManager = fragment.getFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.remove(fragment);
                trans.commit();
            }
            mDirectionRoute.window = null;
        }
        if (mDirectionRoute.marker != null) {
            mDirectionRoute.marker.remove();
            mDirectionRoute.marker = null;
        }
    }

    interface SimpleDirectionListener {
        void onSuccess(List<Route> routes);

        void onFail(String err);
    }

    static class Route {
        String distance;
        String duration;
        List<LatLng> points;
    }

    // Fetches data from url passed
    private static class DownloadDirection extends AsyncTask<String, Void, String> {
        SimpleDirectionListener mListener;
        String mError;

        DownloadDirection(SimpleDirectionListener listener) {
            mListener = listener;
        }

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = null;
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                mError = e.getLocalizedMessage();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.isEmpty()) {
                if (mListener != null) {
                    mListener.onFail(mError);
                }
            } else {
                ParseDirectionResponse parseDirectionResponse = new ParseDirectionResponse(mListener);
                // Invokes the thread for parsing the JSON data
                parseDirectionResponse.execute(result);
            }
        }
    }

    private static class ParseDirectionResponse extends AsyncTask<String, Integer, List<DirectionResponseParser.ResponseRoute>> {

        SimpleDirectionListener mListener;
        String mError;

        ParseDirectionResponse(SimpleDirectionListener listener) {
            mListener = listener;
        }

        // Parsing the data in non-ui thread
        @Override
        protected List<DirectionResponseParser.ResponseRoute> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<DirectionResponseParser.ResponseRoute> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionResponseParser parser = new DirectionResponseParser();
                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                mError = e.getLocalizedMessage();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<DirectionResponseParser.ResponseRoute> result) {
            if (result == null || result.isEmpty()) {
                if (mListener != null) mListener.onFail(mError);
                return;
            }

            List<Route> routes = new ArrayList<>();

            ArrayList<LatLng> points;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                //lineOptions = new PolylineOptions();

                // Fetching i-th route
                DirectionResponseParser.ResponseRoute r = result.get(i);
                List<HashMap<String, String>> path = r.route;

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                //Log.d(TAG, "Distance: " + r.distance + " Duration: " + r.duration);
                //Log.d(TAG, "onPostExecute lineoptions decoded");

                Route route = new Route();
                route.distance = r.distance;
                route.duration = r.duration;
                route.points = points;
                routes.add(route);
            }

            if (routes.isEmpty()) {
                if (mListener != null) mListener.onFail("No direction found");
            } else {
                if (mListener != null) mListener.onSuccess(routes);
            }
        }
    }

    static class DirectionResponseParser {

        /**
         * Receives a JSONObject and returns a list of lists containing latitude and longitude
         */
        public List<ResponseRoute> parse(JSONObject jObject) {
            List<ResponseRoute> routes = new ArrayList<>();

            try {
                JSONArray jRoutes = jObject.getJSONArray("routes");

                // Traversing all routes
                for (int i = 0; i < jRoutes.length(); i++) {
                    ResponseRoute route = new ResponseRoute();
                    routes.add(route);

                    String polyline = (String) ((JSONObject) ((JSONObject) jRoutes.get(i)).get("overview_polyline")).get("points");
                    if (polyline != null && !polyline.isEmpty()) {
                        List<LatLng> list = PolyUtil.decode(polyline);
                        List<HashMap<String, String>> path = new ArrayList<>();
                        // Traversing all points
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString((list.get(l)).latitude));
                            hm.put("lng", Double.toString((list.get(l)).longitude));
                            path.add(hm);
                        }
                        route.route = path;
                    }

                    JSONArray jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");

                    JSONObject jDistance = ((JSONObject) jLegs.get(i)).getJSONObject("distance");
                    String distance = jDistance.getString("text");
                    route.distance = distance;

                    JSONObject jDuration = ((JSONObject) jLegs.get(i)).getJSONObject("duration");
                    String duration = jDuration.getString("text");
                    route.duration = duration;

                    if (route.route.isEmpty()) {
                        // Traversing all legs
                        for (int j = 0; j < jLegs.length(); j++) {
                            JSONArray jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                            List<HashMap<String, String>> path = new ArrayList<>();

                            // Traversing all steps
                            for (int k = 0; k < jSteps.length(); k++) {
                                polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                                List<LatLng> list = PolyUtil.decode(polyline);

                                // Traversing all points
                                for (int l = 0; l < list.size(); l++) {
                                    HashMap<String, String> hm = new HashMap<>();
                                    hm.put("lat", Double.toString((list.get(l)).latitude));
                                    hm.put("lng", Double.toString((list.get(l)).longitude));
                                    path.add(hm);
                                }
                            }
                            route.route = path;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return routes;
        }

        class ResponseRoute {
            String distance;
            String duration;
            List<HashMap<String, String>> route;
        }
    }

    private class SimpleDirectionRoute {
        Polyline line;
        InfoWindow window;
        Marker marker;
    }
}
