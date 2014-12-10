import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class kmeansclustering 
{
	public static void main(String[] args) throws Exception
	{
		URL url1 = null;
		Scanner in=new Scanner(System.in);
		System.out.println("Give the number of top locations you want");
		int m=in.nextInt();
		K_Means_Clustering k=new K_Means_Clustering(url1, m);
		URL url = null;
		//k.kmeans(url);
		k.jsontop();
	}
}

class Common_Functions
{
	final static double R = 6372.8; // In kilometers
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
	public String mean(ArrayList<Double> Latitude, ArrayList<Double> Longitude) {
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
	public ArrayList<String> getNearCentroid(String latlon, ArrayList<String> cent)
	{
		String split[]=latlon.split(", ");
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
	public String centroid(ArrayList<Double> latitude, ArrayList<Double> longitude) 
	{
		int i=0,j=0,h2=0;
		double lat1=0.0,lon1=0.0,lat2=0.0,lon2=0.0,h=0,h1=0;
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
			h1=0;
		}
		return latitude.get(h2)+", "+longitude.get(h2);
	}
	public String[] toplocation(Hashtable<String,ArrayList<String>> a1)
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
}
class K_Means_Clustering extends Common_Functions
{
	URL url;
	int numbers;
	Hashtable<String, ArrayList<String>> location;
	public K_Means_Clustering(URL url, int numbers)
	{
		this.url=url;
		this.numbers=numbers;
		location=new Hashtable<>();
	}
	public void jsontop() throws Exception //for giving json file as top location output for givven data
	{
		String r[]=toplocation(url,numbers);
		for(int i=0;i<r.length;i++) {
			System.out.println("the "+i+1+"th top location is "+r[i]);
		}
		JSONObject ob1=new JSONObject();
		JSONArray val2=new JSONArray();
		for(int j=0;j<numbers;j++) 
		{
			String vs[]=r[j].split(", ");
			vs[0]=vs[0].substring(1,vs[0].length()-1);
			vs[1]=vs[1].substring(1,vs[1].length()-1);
			String ke=vs[0]+", "+vs[1];
			val2.add(ke);
		}
		ob1.put("top",val2);
		jsonfile(ob1,"TopJSON.json");
	}
	public String[] toplocation(URL url,int n) throws Exception //used to find top locations of given cluster
	{
		Hashtable<String,ArrayList<String>> a1=kmeans(url);
		String r[]=new String[n];
		JSONObject ob1=new JSONObject();
		for(int i=0;i<n;i++)
			r[i]=" ";
		int i=0,m=0;		
		for(int j=0;j<n;j++) {
			m=0;
			Enumeration<String> names = a1.keys();
			while(names.hasMoreElements()) {
				String str = (String) names.nextElement();	
				if(m==0){
					i=a1.get(str).size();
					r[j]=str;
					m++;
				}
				if(i<a1.get(str).size()) {
					i=a1.get(str).size();
					r[j]=str;
				}
			} 
			a1.remove(r[j]);
		}
		JSONArray val2=new JSONArray();
		for(int j=0;j<n;j++) {
			String vs[]=r[j].split(", ");
			vs[0]=vs[0].substring(1,vs[0].length()-1);
			vs[1]=vs[1].substring(1,vs[1].length()-1);
			String ke=vs[0]+", "+vs[1];
			val2.add(ke);
		}
		ob1.put("top",val2);
		return r;
	}
	public Hashtable<String,ArrayList<String>>  kmeans(URL url1) throws Exception//finds the centroid and gives the clusters
	{
		Hashtable<String,String[]> tm=new Hashtable<>();
		Hashtable<String, ArrayList<String>> tn=new Hashtable<>();
		URL url= new URL("https://autocode.pythonanywhere.com/AAT/webapi/locationjson?email=rrcmuedu@gmail.com&api=NDdlMDRiYWQyYmE3MzQ2NzM5NjhjMDIyYTQ5Y2JkMjMyZmRiNjVjMjliMzY0ZmYwNGU3ZGFlNGI");
		BufferedReader in=new BufferedReader(new InputStreamReader(url.openStream()));
		String data;
		Scanner in1=new Scanner(System.in);
		//JSONObject obj=new JSONObject();
		data=in.readLine();
		data=data.substring(10,data.length()-2);
		//System.out.println(data);
		String a[]=data.split("], ");
		String d[][]=new String[a.length][];
		System.out.println("give the date to process data");
		String date1=in1.next();
		System.out.println("Give the number of clusters you want");
		int n1=in1.nextInt();
		String sf[]=new String[n1];
		in1.close();
		//System.out.println(a.length+"  ==alength");
		ArrayList<String> sa=new ArrayList<String>();
		ArrayList<String> sa1=new ArrayList<String>();
		FileWriter file1=new FileWriter("json1.json");
		JSONObject o1=new JSONObject();
		JSONArray k1=new JSONArray();
		JSONArray k2=new JSONArray();
		int p12=0;
		for(int i=0;i<a.length;i++ )
		{
			String s[]=a[i].split(", ");
			if(finddate(date1,s[4]))
			{
				p12++;
				JSONArray v1=new JSONArray();
				//System.out.println(s[2]+", "+s[3]);
				String sr=s[2].substring(1,s[2].length()-1)+", "+s[3].substring(1,s[3].length()-1);
				sa.add(s[2]+", "+s[3]);
				v1.add(sr);
				k2.add(sr);
				System.out.println(s[3].substring(1,s[3].length()-1)+"  p12="+p12);
				sa1.add(a[i]);
				k1.add(v1);
			}	
		}
		System.out.println(p12+"    ==p12");
		o1.put("value", k1);
		o1.put("key", k2);
		try
		{
			file1.write(o1.toJSONString());
			//System.out.println("Done");
		}
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
		file1.flush();
		file1.close();
		int v=sa.size();
		for(int i=0;i<v;i++) {
			tm.put(sa.get(i), sa1.get(i).split(", "));
			for(int j=0;j<n1;j++)
			{
				sf[j]=sa.get((int)(Math.random()*v));
			}
		}
		int n2=100;
		for(int k=0;k<n2;k++) {
			tn=bucket(sf,tm);
			Enumeration<String> e1=tn.keys();
			int pp=0;
			while(e1.hasMoreElements()) {
				String key=e1.nextElement();
				ArrayList<String> p=tn.get(key);
				ArrayList<Double> d1=new ArrayList<Double>();
				ArrayList<Double> d2=new ArrayList<Double>();						
				for(int i=0;i<p.size();i++) {
					String s[]=p.get(i).split(", ");
					s[0]=s[0].substring(1,s[0].length()-1);
					s[1]=s[1].substring(1,s[1].length()-1);
					d1.add(Double.parseDouble(s[0]));
					d2.add(Double.parseDouble(s[1]));
				}
				if(k>n2/2) {
					sf[pp]=minimum(d1,d2);
				}else {
					sf[pp]=avg(d1,d2);
				}
				pp++;
			}
		}
		JSONObject ob1=new JSONObject();	
		JSONArray key1=new JSONArray();
		JSONArray val11=new JSONArray();
		Enumeration<String> e2=tn.keys();
		while(e2.hasMoreElements()) {
			String key=(String)e2.nextElement();
			String ks[]=key.split(", ");
			ks[0]=ks[0].substring(1,ks[0].length()-1);
			ks[1]=ks[1].substring(1,ks[1].length()-1);
			String ky=ks[0]+", "+ks[1];
			key1.add(ky);
			ArrayList<String> p=tn.get(key);
			JSONArray val2=new JSONArray();
			System.out.println(p.size());
			for(int i=0;i<p.size();i++) 
			{
				String vs[]=p.get(i).split(", ");
				vs[0]=vs[0].substring(1,vs[0].length()-1);
				vs[1]=vs[1].substring(1,vs[1].length()-1);
				String ke=vs[0]+", "+vs[1];
				val2.add(ke);
			}
			val11.add(val2);
		}
		ob1.put("key", key1);
		ob1.put("Value", val11);
		jsonfile(ob1, "ClusterJSON.json");
		return tn;
	}
	public Hashtable<String, ArrayList<String>> bucket(String[] s,Hashtable<String,String[]> t) //used to do one itration of kmeans
	{
		Hashtable<String, ArrayList<String>> ta=new  Hashtable<String, ArrayList<String>>();
		ArrayList<String> g1=new ArrayList<String>();
		Enumeration<String> e=t.keys();
		while(e.hasMoreElements()) 
		{
			String key=e.nextElement();
			//System.out.println("error\n"+key);
			String[] k2=key.split(", ");			
			k2[0]=k2[0].substring(1,k2[0].length()-1);
			k2[1]=k2[1].substring(1,k2[1].length()-1);
			Double la2=Double.parseDouble(k2[0]);
			Double ln2=Double.parseDouble(k2[1]);
			double di[]=new double[s.length];
			
			for(int i=0;i<s.length;i++)
			{
				String[] k1=s[i].split(", ");
				k1[0]=k1[0].substring(1,k1[0].length()-1);
				k1[1]=k1[1].substring(1,k1[1].length()-1);
				Double la1=Double.parseDouble(k1[0]);
				Double ln1=Double.parseDouble(k1[1]);
				di[i]=haversine(la1,ln1,la2,ln2);
			}
			ArrayList<String> gp=new ArrayList<String>();
			int h=0;
			double h1=di[0];
			for(int i=0;i<di.length;i++) {
				if(di[i]<h1) {
					h=i;
					h1=di[i];
				}
			}
			if(ta.containsKey(s[h])){
				gp=ta.get(s[h]);
				gp.add(key);
				ta.put(s[h],gp);
			} else {
				gp.add(key);
				ta.put(s[h], gp);
			}
		}
		return ta;
	}
	public void jsonfile(JSONObject obj, String name) throws IOException {//to read and print a json object
		FileWriter file=new FileWriter(name);
		try
		{
			file.write(obj.toJSONString());
			System.out.println("Done");
		}
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
		file.flush();
		file.close();
	}
	public String avg(ArrayList<Double> Latitude, ArrayList<Double> Longitude) { //method for centroid using average
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
		String str = "\""+(Math.atan2(z, Math.sqrt(x * x + y * y)) * (180/Math.PI))+"\""+", "+"\""+(Math.atan2(y, x) * (180/Math.PI))+"\"";
		return str;
	}
	
	public static boolean finddate(String s1,String s2)throws Exception
	{
		s2=s2.substring(1,s2.length()-1);
		String sa1[]=s1.split("-");
		String sa2[]=s2.split(" ");
		String sa21[]=sa2[0].split("-");
		String sa22[]=sa2[1].split(":");
		int[] a1=new int[3];
		int[] b1=new int[3];
		int[]b2=new int[3];
		for(int i=0;i<3;i++)
		{
			a1[i]=Integer.parseInt(sa1[i]);
			b1[i]=Integer.parseInt(sa21[i]);
			b2[i]=Integer.parseInt(sa22[i]);
		}
		if((a1[0]==b1[0]||a1[0]+1==b1[0])&&(a1[1]==b1[1])&&(a1[2]==b1[2]))
		{
			if(a1[0]==b1[0])
			{
				if(b2[0]>=6)
					return true;
			}
			else
			{
				if(b2[0]<6)
					return true;
			}
		}
		return false;
	}

	public String minimum(ArrayList<Double> latitude, ArrayList<Double> longitude) //method for centroid using minimum distance 
	{
		
		int i=0,j=0,h2=0;
		double lat1=0.0,lon1=0.0,lat2=0.0,lon2=0.0,h=0,h1=0;
		for( i = 0 ; i < latitude.size() ; i++) {
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
			h1=0;
		}
		return "\""+latitude.get(h2)+"\""+", "+"\""+longitude.get(h2)+"\"";
	}
}