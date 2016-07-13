package com.slowalker.musicbox.mood;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.os.Environment;
import android.util.Pair;

public class MusicFeaturesProvider {
   
   Map<String, FeatureNode> features = new HashMap<String, FeatureNode>();
   Iterator<String> it;
   public MusicFeaturesProvider() throws Exception
   {
	   String path = Environment.getExternalStoragePublicDirectory(
			                         Environment.DIRECTORY_MUSIC)+"/features";
	   getFeatureFromFile(path);
	   it = features.keySet().iterator();
   }
   private void getFeatureFromFile(String path) throws Exception
   {
	   File file = new File(path);
	   if (file.exists())
	   {
		   BufferedReader reader = new BufferedReader(new FileReader(path));
		   String s;
		   String current = null;
		   while ( (s = reader.readLine()) != null )
		   {
			   s = s.trim();
			   if (s.charAt(0) == '#') // music display name
			   {
				   current = s.substring(1, s.length());
				   FeatureNode node = new FeatureNode();
				   features.put(current, node);
			   }
			   else if (s.contains(",")) // refrain time range
			   {
				   String[] range = s.split(",");
				   if (current == null)
					   throw new Exception("Miss '#' in the features file!");
				   features.get(current).start = Integer.parseInt(range[0]);
				   features.get(current).end = Integer.parseInt(range[1]);
			   }
			   else                     // features values
			   {
				   String[] pairs = s.split(" ");
				   if (features.get(current).features == null)
					   throw new Exception("features in FeatureNode is null!");
				   for (String pair : pairs)
				   {
					   int splitPos = pair.indexOf(':');
					   int label = Integer.parseInt(pair.substring(0, splitPos));//label starts from 1
					   double value = Double.parseDouble(pair.substring(splitPos+1, pair.length()));
					   features.get(current).features[label - 1] = value;
					               
				   }
			   }
		   }
		   reader.close();
	   }
   }
   public boolean hasNext()
   {
	   return it.hasNext();
   }
   public Pair<String, FeatureNode> next()
   {
	   String key = it.next();
	   return new Pair<String, FeatureNode>(key, features.get(key));
   }
   
}
