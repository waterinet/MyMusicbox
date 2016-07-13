package com.slowalker.musicbox.mood;


public class FeatureNode {
     public int start;
     public int end;
     public double[] features;
     FeatureNode(int start, int end, double[] features)
     {
    	 this.start = start;
    	 this.end = end;
    	 this.features = features;
     }
     FeatureNode()
     {
    	 start = 0;
    	 end = 0;
    	 features = new double[18];
     }
}
