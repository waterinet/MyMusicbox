package com.slowalker.musicbox.mood;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.slowalker.musicbox.R;

import android.content.Context;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;


public class SimpleMoodClassifier {
	private Context ctx;
	private Map<Integer, ArrayList<ValueRange>> ranges;
	private Map<Integer, svm_model> models;
	
    private int intensity = 4; // number of intensity features
	private int rhythm = 4; // number of rhythm features
    private int timbre = 10; // number of timbre features  
	//constructor
	//-------------------------------------------------------------
    public SimpleMoodClassifier(Context ctx) throws Exception
    {
    	this.ctx = ctx;
    	ranges = new HashMap<Integer, ArrayList<ValueRange>>();
    	models = new HashMap<Integer, svm_model>();
    	getRanges();
    	getModels();
    }
    
    //public methods
    //--------------------------------------------------------------
    public int classify(double[] features) throws Exception 
    {
    	int start = 0;
    	int end = start + intensity;
    	double[] intensityFeatures = Arrays.copyOfRange(features, start, end);
    	start = end;
		end = start + rhythm;
		double[] rhythmFeatures = Arrays.copyOfRange(features, start, end);
		start = end;
		end = start + timbre;
		double[] timbreFeatures = Arrays.copyOfRange(features, start, end);
		
		scaleFeatures(intensityFeatures, R.raw.group_range);
		int group = (int) svm.svm_predict(models.get(R.raw.group_model), 
				                            generateSvmNodes(intensityFeatures));
		
		
		if (group == 1)
		{
			scaleFeatures(rhythmFeatures, R.raw.class34_range_r);
			svm_model model_r = models.get(R.raw.class34_model_r);
			double[] prob_r = predictProb(model_r, rhythmFeatures); //0.2
			
			scaleFeatures(timbreFeatures, R.raw.class34_range_t);
			svm_model model_t = models.get(R.raw.class34_model_t);
			double[] prob_t = predictProb(model_t, timbreFeatures);  //0.8
			
			double prob_3 = prob_t[0] * 0.8 + prob_r[0] * 0.2;
			double prob_4 = prob_t[1] * 0.8 + prob_r[1] * 0.2;
			if (prob_3 <= prob_4)
				return 4;
			else 
				return 3;
			
		}
		else if (group == 2)
		{
			scaleFeatures(rhythmFeatures, R.raw.class12_range_r);
			svm_model model_r = models.get(R.raw.class12_model_r);
			double[] prob_r = predictProb(model_r, rhythmFeatures); //0.6
			
			scaleFeatures(timbreFeatures, R.raw.class12_range_t);
			svm_model model_t = models.get(R.raw.class12_model_t);
			double[] prob_t = predictProb(model_t, timbreFeatures);  //0.4
			
			double prob_1 = prob_t[0] * 0.4 + prob_r[0] * 0.6;
			double prob_2 = prob_t[1] * 0.4 + prob_r[1] * 0.6;
			if (prob_2 <= prob_1)
				return 1;
			else 
				return 2;
		}
		else
			throw new Error("SimpleMoodClassifier.classity: wrong group label got!");
    }
    
    private double[] predictProb(svm_model model, double[] features)
    {
    	if (svm.svm_check_probability_model(model) == 1)
		{
			int nr_class = svm.svm_get_nr_class(model);
			double[] prob = new double[nr_class];
			svm.svm_predict_probability(model, generateSvmNodes(features), prob);
			return prob;
		}
    	return null;
    }
    
    //private methods
    //-------------------------------------------------------------------
    private void getModels() throws IOException
    {
    	int[] fileIds = new int[] { R.raw.group_model, 
					                R.raw.class12_model_r,
					                R.raw.class12_model_t,
					                R.raw.class34_model_r,
					                R.raw.class34_model_t };
    	for (int id : fileIds)
    	{
    		InputStream input = readModel(id);
    		if (input != null)
    		{
    			BufferedReader reader = new BufferedReader(
         			                          new InputStreamReader(input));
    			svm_model model = svm.svm_load_model(reader);
    			models.put(id, model);
    			reader.close();
    		}
    		input.close();
    	}
    	
    }
    private void getRanges() throws Exception
    {
    	int[] fileIds = new int[] { R.raw.group_range, 
    			                    R.raw.class12_range_r,
    			                    R.raw.class12_range_t,
    			                    R.raw.class34_range_r,
    			                    R.raw.class34_range_t };
    	for (int id : fileIds)
    	{
    		ArrayList<ValueRange> list = new ArrayList<ValueRange>();
    		list = readRange(id);
    		ranges.put(id, list);
    	}
    	
    	
    }
    private InputStream readModel(int id)
    {
    	InputStream input = ctx.getResources().openRawResource(id);
    	return input;
    }
    private ArrayList<ValueRange> readRange(int id) throws IOException
    {
    	ArrayList<ValueRange> list = new ArrayList<ValueRange>();
    	InputStream input = ctx.getResources().openRawResource(id);
        if (input != null)
        {
        	BufferedReader reader = new BufferedReader(
          			 new InputStreamReader(input));
       		String str;
       		while ((str = reader.readLine()) != null)
       		{
       			Scanner scn = new Scanner(str);
       		    int index = scn.nextInt();
       		    ValueRange vr = new ValueRange(scn.nextDouble(), scn.nextDouble());
       		    if (index > 0)
       		    {
       		    	list.add(vr);
       		    }
       		    
       		}
       		reader.close();
        }
        input.close();
        return list;
    	
    	
    }
    
    
    private void scaleFeatures(double[] features, int id) throws Exception 
    {
    	ArrayList<ValueRange> list = ranges.get(id);
		if (features.length != list.size())
			throw new Exception("Arguments of doScale() is not matched!");
		for (int i = 0; i < features.length; ++i)
		{
			double value = features[i];
			ValueRange vr = list.get(i);
		    double scaledValue;
		    if (value >= vr.max)
	    		scaledValue = 1;
	    	else if (value <= vr.min)
	    		scaledValue = -1;
	    	else
	    	    scaledValue = (1 - (-1)) * (value - vr.min) / (vr.max - vr.min) + (-1);
		    BigDecimal bd = new BigDecimal(scaledValue); 
		    features[i] = bd.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue(); 
	    	
		}
		
	}
	
	private svm_node[] generateSvmNodes(double[] features)
	{
		svm_node[] sn = new svm_node[features.length];
		for (int i = 0; i < features.length; ++i)
		{
			svm_node node = new svm_node();
			node.index = i + 1;
			node.value = features[i];
			sn[i] = node;
		}
		return sn;
	}
    
}
