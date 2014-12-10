import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class Location {
	final static double R = 6372.8; // In kilometers
	
	public static double haversine(double lat1, double lon1, double lat2, double lon2) 
	{
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return R * c*1000;
	}
	
	public static String updateCentroid(ArrayList<?> Latitude, ArrayList<?> Longitude) {
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
		String str = "" + (Math.atan2(y, x) * (180/Math.PI)) + ", " + (Math.atan2(z, Math.sqrt(x * x + y * y)) * (180/Math.PI));
		return str;
	}
	
	public static double distanceCheck(ArrayList<ArrayList<String>> source, ArrayList<ArrayList<String>> destination) {
		double result = 0;
		for(int i = 0; i < source.size(); i++) {
			double sourceLat = Double.parseDouble(source.get(i).get(0));
			double sourceLong = Double.parseDouble(source.get(i).get(1));
			double dist = 0;
			for(int j = 0; j < destination.size(); j++) {
				double destLat = Double.parseDouble(destination.get(j).get(0));
				double destLong = Double.parseDouble(destination.get(j).get(1));
				dist = dist + haversine(sourceLat, sourceLong, destLat, destLong);
			}
			result = result + (dist / destination.size());
		}
		return result/((double)source.size());
	}
	//returns an array of size 4 referring to the possible locations where grouping is possible
	//returns null if no such combination exists
	public static String[] isGroupingPossible(Hashtable<ArrayList<String>, ArrayList<ArrayList<String>>> clusters, double threshold) {
		Enumeration<ArrayList<String>> keys = clusters.keys();
		while(keys.hasMoreElements()) {
			ArrayList<String> key = keys.nextElement();
			Double clat = Double.parseDouble(key.get(0));
			Double clong = Double.parseDouble(key.get(1));
			Enumeration<ArrayList<String>> getKeys = clusters.keys();
			while(getKeys.hasMoreElements()) {
				ArrayList<String> minKey = getKeys.nextElement();
				Double glat = Double.parseDouble(minKey.get(0));
				Double glong = Double.parseDouble(minKey.get(1));
				double dist = haversine(clat, clong, glat, glong);
				if(dist != 0.0 && dist < 2 * threshold) {
					ArrayList<ArrayList<String>> source = clusters.get(key);
					ArrayList<ArrayList<String>> destination = clusters.get(minKey);
					if(distanceCheck(source, destination) < threshold) {
						String[] locations = {key.get(0), key.get(1), minKey.get(0), minKey.get(1)};
						return locations;
					}
				}
			}
		}
		return null;
	}
	
	public static Hashtable<ArrayList<String>, ArrayList<ArrayList<String>>> combineClusters(Hashtable<ArrayList<String>, ArrayList<ArrayList<String>>> clusters, String[] possibleClusters) {
		ArrayList<String> key = new ArrayList<String>();
		key.add(possibleClusters[0]);
		key.add(possibleClusters[1]);
		ArrayList<String> anotherKey = new ArrayList<String>();
		anotherKey.add(possibleClusters[2]);
		anotherKey.add(possibleClusters[3]);
		ArrayList<ArrayList<String>> source = clusters.get(key);
		ArrayList<ArrayList<String>> destination = clusters.get(anotherKey);
		clusters.remove(key);
		clusters.remove(anotherKey);
		key = null;
		anotherKey = null;
		for(int i = 0; i < destination.size(); ++i) {
			ArrayList<String> value = new ArrayList<String>();
			value = destination.get(i);
			source.add(value);
		}
		ArrayList<Double> Latitude = new ArrayList<Double>();
		ArrayList<Double> Longitude = new ArrayList<Double>();
		for(int i = 0; i < source.size(); ++i) {
			Latitude.add(Double.parseDouble(source.get(i).get(0)));
			Longitude.add(Double.parseDouble(source.get(i).get(0)));
		}
		String[] key1 = updateCentroid(Latitude, Longitude).split(", ");
		key = new ArrayList<String>();
		key.add(key1[1]);
		key.add(key1[0]);
		clusters.put(key, source);
		return clusters;
	}
	
	public static boolean checkClustersCentroid(Enumeration<ArrayList<String>> oldClusters, Enumeration<ArrayList<String>> newClusters) {
		ArrayList<String> key;
		while(oldClusters.hasMoreElements()) {
			key = oldClusters.nextElement();
			if(contains(key, newClusters) != 1) {
				return false;
			}
		}
		return true;
	}
	
	public static int contains(ArrayList<String> key, Enumeration<ArrayList<String>> newClusters) {
		while(newClusters.hasMoreElements()) {
			ArrayList<String> newKey = newClusters.nextElement();
			if(newKey.get(0).compareTo(key.get(0)) == 0 && newKey.get(1).compareTo(key.get(1)) == 0) {
				return 1;
			}
		}
		return 0;
	}
	
	public static void main(String[] args) {
		URL url;
		try {
			url = new URL("https://autocode.pythonanywhere.com/AAT/webapi/locationjson?email=rrcmuedu@gmail.com&api=NDdlMDRiYWQyYmE3MzQ2NzM5NjhjMDIyYTQ5Y2JkMjMyZmRiNjVjMjliMzY0ZmYwNGU3ZGFlNGI");
			BufferedReader in=new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine;
			inputLine=in.readLine();
			JSONParser parser = new JSONParser();
			JSONObject jObj = (JSONObject) parser.parse(inputLine);
			JSONArray jArray = (JSONArray) jObj.get("data");
			Hashtable<ArrayList<String>, ArrayList<ArrayList<String>>> clusters = new Hashtable<ArrayList<String>, ArrayList<ArrayList<String>>>();
			
			int i = 0;
			for(i = 0; i < jArray.size(); i++) {
				JSONArray jArray2 = new JSONArray();
				jArray2 = (JSONArray) jArray.get(i);
				String lat = (String)jArray2.get(2);
				String lon = (String)jArray2.get(3);
				ArrayList<String> key = new ArrayList<String>();
				ArrayList<String> value = new ArrayList<String>();
				key.add(lat);
				key.add(lon);
				value.add(lat);
				value.add(lon);
				ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
				values.add(value);
				clusters.put(key, values);
			}
			i = 0;

			while(true) {
				String[] canBeGrouped = isGroupingPossible(clusters, 30);
				
				if(canBeGrouped != null) {
					clusters = combineClusters(clusters, canBeGrouped);
					Hashtable<ArrayList<String>, ArrayList<ArrayList<String>>> newClusters = combineClusters(clusters, canBeGrouped);
					if(clusters.size() != newClusters.size()) {
						clusters = newClusters;
					}
					else {
						if(checkClustersCentroid(clusters.keys(), newClusters.keys())) {
							break;
						}
						else {
							clusters = newClusters;
						}
					}
				}
			}
			Enumeration<ArrayList<String>> keys = clusters.keys();
			i = 0;
			while(keys.hasMoreElements()) {
				ArrayList<ArrayList<String>> values = clusters.get(keys.nextElement());
				if(values.size() != 1) {
					System.out.println("i: " + i + values.size() + "\t" + values);
				}
				++i;
			}
			System.out.println(clusters.size());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
		
	}
}