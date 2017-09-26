package com.routeal.cocoger.ui.main;

import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

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

public class SimpleDirection {
    private final static String TAG = "SimpleDirection";

    static class Route {
        String distance;
        String duration;
        List<LatLng> points;
    }

    interface SimpleDirectionListener {
        void onSuccess(List<Route> routes);

        void onFail(String err);
    }

    public static void getDirection(Location to, Location from, SimpleDirectionListener listener) {
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

        class ResponseRoute {
            String distance;
            String duration;
            List<HashMap<String, String>> route;
        }

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
                        List<LatLng> list = decodePoly(polyline);
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
                                List<LatLng> list = decodePoly(polyline);

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

        /**
         * Method to decode polyline points
         * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
         */
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
    }
}
