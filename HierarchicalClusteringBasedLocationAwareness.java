import java.io.BufferedReader;
import java.io.FileWriter;
//import java.io.IOException;
import java.io.InputStreamReader;
//import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;



class HierarchicalClusteringBasedLocationAwareness {
	
	final static double R = 6372.8; // In kilometers
	private Hashtable<String, ArrayList<String>> clusters = new Hashtable<String, ArrayList<String>>();
	
	//returns the haversine distance between two latitudes and longitudes
	public double haversine(double lat1, double lon1, double lat2, double lon2) 
	{
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return R * c*1000;
	}
	//
	public double distanceCheck(ArrayList<String> source, ArrayList<String> destination) {
		double result = 0;
		for(int i = 0; i < source.size(); i++) {
			String[] sLatLong = source.get(i).split(", ");
			double sourceLat = Double.parseDouble(sLatLong[0]);
			double sourceLong = Double.parseDouble(sLatLong[1]);
			double dist = 0;
			for(int j = 0; j < destination.size(); j++) {
				String[] dLatLong = destination.get(j).split(", ");
				double destLat = Double.parseDouble(dLatLong[0]);
				double destLong = Double.parseDouble(dLatLong[1]);
				dist = dist + haversine(sourceLat, sourceLong, destLat, destLong);
			}
			result = result + (dist / (double)destination.size());
			//System.out.println("In Distance Check, " + result/((double) source.size()));
		}
		return result/((double)source.size());
	}
	
	public String updateCentroid(ArrayList<?> Latitude, ArrayList<?> Longitude) {
		double x = 0;
		double y = 0;
		double z = 0;
		for(int i = 0; i < Latitude.size(); i++) {
			x += Math.cos(((double) Latitude.get(i)) * (Math.PI/180)) * Math.cos(((double) (Longitude.get(i)) * (Math.PI/180)));
			y += Math.cos(((double) Latitude.get(i)) * (Math.PI/180)) * Math.sin(((double) Longitude.get(i)) * (Math.PI/180));
			z += Math.sin(((double) Latitude.get(i)) * (Math.PI/180));
		}
		x = (x / Latitude.size());
		y = (y / Latitude.size());
		z = (z / Latitude.size());
		String str = "" + (Math.atan2(z, Math.sqrt(x * x + y * y)) * (180/Math.PI)) + ", " + (Math.atan2(y, x) * (180/Math.PI));
		return str;
	}
	
	public String[] isGroupingPossible(double threshold) {
		Enumeration<String> keys = clusters.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			String lat = key.substring(0, key.indexOf(", "));
			String lon = key.substring(key.indexOf(", ") + 2);
			Double clat = Double.parseDouble(lat);
			Double clong = Double.parseDouble(lon);
			Enumeration<String> getKeys = clusters.keys();
			while(getKeys.hasMoreElements()) {
				String minKey = getKeys.nextElement();
				String dlat = minKey.substring(0, minKey.indexOf(", "));
				String dlong = minKey.substring(minKey.indexOf(", ") + 2);
				Double glat = Double.parseDouble(dlat);
				Double glong = Double.parseDouble(dlong);
				double dist = haversine(clat, clong, glat, glong);
				if(dist != 0.0 && dist < threshold) {
					ArrayList<String> source = clusters.get(key);
					ArrayList<String> destination = clusters.get(minKey);
					if(distanceCheck(source, destination) < 2 * threshold) {
						String[] locations = {lat + ", " + lon, dlat + ", " + dlong};
						return locations;
					}
				}
			}
		}
		return null;
	}
	
	public void combineClusters(String[] possibleClusters) {
		ArrayList<String> source = clusters.get(possibleClusters[0]);
		ArrayList<String> destination = clusters.get(possibleClusters[1]);
		for(int i = 0; i < destination.size(); i++) {
			source.add(destination.get(i));
		}
		clusters.remove(possibleClusters[0]);
		clusters.remove(possibleClusters[1]);
		ArrayList<Double> Latitude = new ArrayList<Double>();
		ArrayList<Double> Longitude = new ArrayList<Double>();
		for(int i = 0; i < source.size(); ++i) {
			String latlong = source.get(i);
			Latitude.add(Double.parseDouble(latlong.substring(0, latlong.indexOf(", "))));
			Longitude.add(Double.parseDouble(latlong.substring(latlong.indexOf(", ") + 2)));
		}
		clusters.put(updateCentroid(Latitude, Longitude), source);
	}
	
	public boolean checkClustersCentroid(Enumeration<String> oldClusters, Enumeration<String> newClusters) {
		String key;
		while(oldClusters.hasMoreElements()) {
			key = oldClusters.nextElement();
			if(contains(key, newClusters) != 1) {
				return false;
			}
		}
		return true;
	}
	
	public int contains(String key, Enumeration<String> newClusters) {
		while(newClusters.hasMoreElements()) {
			String newKey = newClusters.nextElement();
			if(key.compareTo(newKey) == 0) {
				return 1;
			}
		}
		return 0;
	}
	
	
	public int size() {
		return clusters.size();
	}
	
	public void place(String key, ArrayList<String> values) {
		clusters.put(key, values);
	}
	
	public void makeClusters(URL url) {
		try {
			BufferedReader in=new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine;
			inputLine=in.readLine();
			JSONParser parser = new JSONParser();
			JSONObject jObj = (JSONObject) parser.parse(inputLine);
			JSONArray jArray = (JSONArray) jObj.get("data");
			for(int i = 0; i < jArray.size(); ++i) {
				JSONArray jArray2 = new JSONArray();
				jArray2 = (JSONArray) jArray.get(i);
				String lat = (String)jArray2.get(2);
				String lon = (String)jArray2.get(3);
				String key = lat + ", " + lon;
				String timeStamp = (String) jArray2.get(4);
				String value = lat + ", " + lon + ", " + timeStamp;
				ArrayList<String> values = new ArrayList<String>();
				values.add(value);
				clusters.put(key, values);
			}
			while(clusters.size() >= 5) {
				String[] canBeGrouped = isGroupingPossible(30);
				if(canBeGrouped != null) {
					combineClusters(canBeGrouped);
				}
				else 
					break;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	public void writeToJSONFile(String fileName) {
		Enumeration<String> keys = clusters.keys();
		JSONObject jObj = new JSONObject();
		JSONArray jKey = new JSONArray();
		JSONArray jVal = new JSONArray();
		FileWriter fw = null;
		try {
			fw = new FileWriter("fileName");
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				jKey.add(key);
				JSONArray val = new JSONArray();
				ArrayList<String> value = clusters.get(key);
				for(int i = 0; i < value.size(); ++i) {
					if(value.size() - 1 == i) {
						val.add(value.get(i));
					}
				}
				jVal.add(val);
			}
			jObj.put("key", jKey);
			jObj.put("Value", jVal);
			fw.write(jObj.toJSONString());
			fw.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}