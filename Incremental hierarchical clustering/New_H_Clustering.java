import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class New_H
{
	final static double R = 6372.8; // In kilometers
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException
	{
		//Scanner in=new Scanner(new File("rrData.txt"));
		String data="";
		try
		{
			URL url= new URL("https://autocode.pythonanywhere.com/AAT/webapi/locationjson?email=rrcmuedu@gmail.com&api=NDdlMDRiYWQyYmE3MzQ2NzM5NjhjMDIyYTQ5Y2JkMjMyZmRiNjVjMjliMzY0ZmYwNGU3ZGFlNGI");
			BufferedReader in=new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine;
			JSONObject obj=new JSONObject();
			int a=0;
			inputLine=in.readLine();
			obj.put(a,inputLine);
			data=(String)obj.get(0);
			data=data.substring(10,data.length()-2);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		String data1[]=data.split("], ");
		System.out.println("Size: "+data1.length);
		Hashtable<String, ArrayList<String>> location=new Hashtable<>();
		for(int i=0;i<data1.length;i++)
		{
			ArrayList<String> count=new ArrayList<>();
			ArrayList<String> value=new ArrayList<>();
			String key="";
			String split[]=data1[i].split(", ");
			if(!split[2].substring(1,split[2].length()-1).equalsIgnoreCase("notAvailable") && !split[3].substring(1,split[3].length()-1).equalsIgnoreCase("notAvailable") && !split[2].substring(1,split[2].length()-1).equalsIgnoreCase("Location Service is turnedOff") && !split[3].substring(1,split[3].length()-1).equalsIgnoreCase("Location Service is turnedOff"))
			{
				key=key+split[2].substring(1,split[2].length()-1)+", "+split[3].substring(1,split[3].length()-1);
				double lat=Double.parseDouble(split[2].substring(1,split[2].length()-1));
				double lon=Double.parseDouble(split[3].substring(1,split[3].length()-1));
				value.add(lat+", "+lon);
				if(i==0)
					location.put(key,value);
				else
				{
					Enumeration<String> e=location.keys();
					while(e.hasMoreElements())
					{
						String hKey=e.nextElement();
						String kLlt[]=hKey.split(", ");  //Splitting key into lat, long and time stamp
						double klat=Double.parseDouble(kLlt[0]);  //Latitude
						double klong=Double.parseDouble(kLlt[1]);  //Longitude
						if(haversine(klat, klong, lat, lon)<=15)
						{
							count.add(hKey);
						}
					}
					if(count.size()==0)
					{
						location.put(key, value);
					}
					else if(count.size()==1)
					{
						String hKey=count.get(0);
						value=location.get(count.get(0));
						value.add(lat+", "+lon);
						location.put(hKey, value);
					}
					else
					{
						int size=count.size();
						double dist[]=new double[size];
						for(int j=0;j<size;j++)
						{
							String hKey[]=count.get(j).split(", ");
							dist[j]=haversine(Double.parseDouble(hKey[0]), Double.parseDouble(hKey[1]), lat, lon);
						}
						int min1=0;
						for(int j=1;j<size;j++)
						{
							if(dist[min1]>dist[j])
								min1=j;
						}
						String hKey=count.get(min1);
						value=location.get(count.get(min1));
						value.add(lat+", "+lon);
						location.put(hKey, value);
					}
				}
			}
			count.clear();
		}
		Enumeration<String> e = location.keys();
		ArrayList<String> centroids=new ArrayList<>();
		while(e.hasMoreElements())
		{
			String key=e.nextElement();
			String split[]=key.split(", ");
			ArrayList<String> value=location.get(key);
			ArrayList<Double> lat=new ArrayList<Double>();
			ArrayList<Double> lon=new ArrayList<Double>();
			lat.add(Double.parseDouble(split[0]));
			lon.add(Double.parseDouble(split[1]));
			for(int j=0;j<value.size();j++)
			{
				String latlon=value.get(j);
				split = latlon.split(", ");
				lat.add(Double.parseDouble(split[0]));
				lon.add(Double.parseDouble(split[1]));
			}
			String centroid=centroid(lat, lon);
			value.clear();
			centroids.add(centroid);
		}
		location.clear();
		int count=0;
		while(count==0)
		{
			int c=1;
			for(int i=0;i<centroids.size();i++)
			{
				ArrayList<String> close=getNearCentroid(centroids.get(i), centroids);
				if(close.size()>1)
				{
					c=0;
					ArrayList<Double> lat1=new ArrayList<>();
					ArrayList<Double> lon1=new ArrayList<>();
					for(int j=0;j<close.size();j++)
					{

						String split1[]=close.get(j).split(", ");
						lat1.add(Double.parseDouble(split1[0]));
						lon1.add(Double.parseDouble(split1[1]));
						centroids.remove(close.get(j));
					}
					centroids.add(mean(lat1, lon1));
				}
			}
			if(c==0)
				count=0;
			else
				count=1;
		}
		location.clear();
		ArrayList<String> value1=new ArrayList<>();
		for(int i=0;i<centroids.size();i++)
			location.put(centroids.get(i), value1);

		int out=0;
		count=0;
		int ky=0;
		int k=0;
		for(int j=0;j<data1.length;j++)
		{				
			String split3[]=data1[j].split(", ");
			double accuracy=0;
			//int confidence=0;
			if(!split3[5].equals("null") && !split3[7].equals("null"))
			{
				accuracy=Double.parseDouble(split3[5].substring(1,split3[5].length()-1));
				//confidence=Integer.parseInt(split[7].substring(1,split[7].length()-1));
			}
			int date=0, month=0, year=0, hour=0, min=0, sec=0;
			String timedate=split3[4].substring(1,split3[4].length()-1);
			String stamp[]=split3[4].split(" "); // time of id
			String day=stamp[0].substring(1);
			String time=stamp[1].substring(0,stamp[1].length()-1);
			String split1[]=day.split("-");
			date=Integer.parseInt(split1[0]);						
			month=Integer.parseInt(split1[1]);
			year=Integer.parseInt(split1[2]);					
			String split2[]=time.split(":");
			hour=Integer.parseInt(split2[0]);
			min=Integer.parseInt(split2[1]);
			sec=Integer.parseInt(split2[2]);			
			if(((date==23 && hour>5 && month==10) || (date==24 && hour<=5 && min<=59) && month==10) && accuracy<70 && !split3[2].substring(1,split3[2].length()-1).equalsIgnoreCase("notAvailable") && !split3[3].substring(1,split3[3].length()-1).equalsIgnoreCase("notAvailable") && !split3[2].substring(1,split3[2].length()-1).equalsIgnoreCase("Location Service is turnedOff") && !split3[3].substring(1,split3[3].length()-1).equalsIgnoreCase("Location Service is turnedOff"))
			{
				Double lat3=Double.parseDouble(split3[2].substring(1,split3[2].length()-1));
				Double lon3=Double.parseDouble(split3[3].substring(1,split3[3].length()-1));
//				if(k==1 || k==2 || k==3)
//				{
//					System.out.println(lat3+", "+lon3);
//					System.out.println("1111111111111111111111111111111111111111111111111");
//					Enumeration<String> ke = location.keys();
//					while(ke.hasMoreElements())
//					{
//						String Key=ke.nextElement();
//						ArrayList<String> value11 = location.get(Key);
//						if(value11.size()>=1)
//						System.out.println(Key+"Size: "+value11.get(0));
//					}
//				}
//				k++;
				String act=split3[6].substring(1,split3[6].length()-1);
				while(act.equalsIgnoreCase("still"))
				{
					j++;
					if(j>=data1.length)
						break;
					split3=data1[j].split(", ");
					if(!split3[5].equalsIgnoreCase("None") && !split3[2].substring(1,split3[2].length()-1).equalsIgnoreCase("notAvailable") && !split3[3].substring(1,split3[3].length()-1).equalsIgnoreCase("notAvailable") && !split3[2].substring(1,split3[2].length()-1).equalsIgnoreCase("Location Service is turnedOff") && !split3[3].substring(1,split3[3].length()-1).equalsIgnoreCase("Location Service is turnedOff"))
					{
						if(accuracy>Double.parseDouble(split3[5].substring(1,split3[5].length()-1)))
						{
							accuracy=Double.parseDouble(split3[5].substring(1,split3[5].length()-1));
							lat3=Double.parseDouble(split3[2].substring(1,split3[2].length()-1));
							lon3=Double.parseDouble(split3[3].substring(1,split3[3].length()-1));
						}
					}
					act=split3[6].substring(1,split3[6].length()-1);
				}									
				int c=0;
				Enumeration<String> ke=location.keys();
				ke=location.keys();
				while(ke.hasMoreElements() && c==0)
				{					
					String hKey=ke.nextElement();
					String spl[]=hKey.split(", ");
					Double kLat=Double.parseDouble(spl[0]);
					Double kLon=Double.parseDouble(spl[1]);
					if(haversine(lat3, lon3, kLat, kLon)<=30)
					{
						count++;
						c=1;
						ArrayList<String> value = new ArrayList<>();
						value=location.get(hKey);
						location.remove(hKey);
						if(spl.length==2)
						{
							ky++;
							hKey=hKey+", "+timedate;
						}
						else
							hKey=kLat+", "+kLon+", "+timedate;						
						location.put(hKey, value);
						//System.out.println("Before: "+location.get(hKey).size());
						value=location.get(hKey);
						value.add(lat3+", "+lon3+", "+timedate);
						location.put(hKey, value);
						//System.out.println("After: "+location.get(hKey).size());
					}					
				}				
				if(c==0)
					out++;
			}			
		}
		System.out.println("\nTotal out: "+out);
		System.out.println("Total in: "+count);
		System.out.println("Total keys: "+ky);
		System.out.println("Clustering Done!!\n------------------------------------------------------------");
		Enumeration<String> ke=location.keys();
		while(ke.hasMoreElements())
		{
			String Key=ke.nextElement();
			ArrayList<String> value = location.get(Key);
			System.out.println(Key+"Size: "+value.size());
		}		
		e = location.keys();
		FileWriter file= new FileWriter("NewJSON.json");
		JSONObject ob=new JSONObject();	
		JSONArray key1=new JSONArray();
		JSONArray val1=new JSONArray();
		count=0;
		while(e.hasMoreElements())
		{
			String hKey=(String) e.nextElement();
			String spli[]=hKey.split(", ");
			ArrayList<String> last=new ArrayList<>();
			if(location.get(hKey).size()>=1 && spli.length==3)
			{
				ArrayList<String> val=location.get(hKey);									
				JSONArray val2=new JSONArray();
				key1.add(hKey);					
				for(int i=0;i<val.size();i++)
				{
					String set=val.get(i);
					String splt[]=set.split(", ");
					if(haversine(Double.parseDouble(spli[0]), Double.parseDouble(spli[1]), Double.parseDouble(splt[0]),Double.parseDouble(splt[1]))<=30)
					{
						last.add(set);
						if(i==val.size()-1)
							val2.add(set);
						else
							val2.add(set+", ");
					}					
				}
				location.put(hKey, last);
				val1.add(val2);
				count++;
			}
			else
				location.put(hKey, last);
		}
		System.out.println(" --------------------------------------  ");
		ke=location.keys();
		while(ke.hasMoreElements())
		{
			String Key=ke.nextElement();
			ArrayList<String> value = location.get(Key);
			System.out.println(Key+"Size: "+value.size());
		}
		System.out.println("\nTotal keys: "+count);
		ob.put("key", key1);
		ob.put("Value", val1);
		try
		{
			file.write(ob.toJSONString());
			System.out.println("Done");
		}
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
		file.flush();
		file.close();
		String top[]=toplocation(location);
		FileWriter file1= new FileWriter("TopJSON.json");
		JSONObject ob1=new JSONObject();	
		JSONArray key11=new JSONArray();
		for(int j=0;j<3;j++)
		{
			String split[]=top[j].split(", ");
			ArrayList<Double> lat=new ArrayList<>();
			ArrayList<Double> lon=new ArrayList<>();
			lat.add(Double.parseDouble(split[0]));
			lon.add(Double.parseDouble(split[1]));
			value1=location.get(top[j]);
			for(k=0;k<value1.size();k++)
			{
				split=value1.get(k).split(", ");
				lat.add(Double.parseDouble(split[0]));
				lon.add(Double.parseDouble(split[1]));
			}
			key11.add(centroid(lat, lon));
		}
		ob1.put("top", key11);
		file1.write(ob1.toJSONString());
		System.out.println("Top Location Done");
		file1.flush();
		file1.close();
	}
	public static String[] toplocation(Hashtable<String,ArrayList<String>> a1)
	{
		String r[]=new String[3];
		for(int i=0;i<3;i++)
			r[i]=" ";
		int a[]=new int[3];		
		Enumeration<String> names = a1.keys();
		while(names.hasMoreElements()) {
			String str = (String) names.nextElement();
			int i=a1.get(str).size();
			if(a[2]<=i)
			{
				a[2]=i;
				r[2]=str;
				if(a[1]<=i)
				{
					r[2]=r[1];
					r[1]=str;
					a[2]=a[1];
					a[1]=i;
					if(a[0]<=i)
					{
						r[1]=r[0];
						r[0]=str;
						a[1]=a[0];
						a[0]=i;
					}
				}
			}

		} return r;
	}
	public static String mean(ArrayList<Double> Latitude, ArrayList<Double> Longitude) {
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
		String str = "" +(Math.atan2(z, Math.sqrt(x * x + y * y)) * (180/Math.PI))+", "+ (Math.atan2(y, x) * (180/Math.PI));
		return str;
	}
	public static ArrayList<String> getNearCentroid(String latlon, ArrayList<String> cent)
	{
		String split[]=latlon.split(", ");
		//		System.out.println("latlon: "+latlon);
		//		System.out.println("latlon: "+latlon+"Lat: "+split[0]+" Lon: "+split[1]);
		Double lat=Double.parseDouble(split[0]);
		Double lon=Double.parseDouble(split[1]);
		ArrayList<String> close=new ArrayList<>();
		for(int i=0;i<cent.size();i++)
		{
			split=cent.get(i).split(", ");
			Double la=Double.parseDouble(split[0]);
			Double lo=Double.parseDouble(split[1]);
			if(haversine(lat,lon,la,lo)<=60)
			{
				close.add(cent.get(i));
			}
		}
		return close;
	}	
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
	public static String centroid(ArrayList<Double> latitude, ArrayList<Double> longitude) 
	{
		int i=0,j=0,h2=0;
		double lat1=0.0,lon1=0.0,lat2=0.0,lon2=0.0,h=0,h1=0;
		//System.out.println("Lat: "+latitude.size()+" Long: "+longitude.size());
		for( i = 0 ; i < latitude.size() ; i++)
		{
			lat1=latitude.get(i);
			lon1=longitude.get(i);
			for(j=0 ; j< latitude.size() ; j++)
			{
				lat2=latitude.get(j);
				lon2=longitude.get(j);
				h1+=haversine(lat1,lon1,lat2,lon2);
			}
			if(i==0)
			{
				h=h1;
			}
			if(h1<=h)
			{				
				h2=i;
				h=h1;
			}
			//System.out.println(h1+" "+h+" "+h2);
			h1=0;
		}
		//System.out.println("Lat: "+latitude.get(h2));
		return latitude.get(h2)+", "+longitude.get(h2);
	}
}