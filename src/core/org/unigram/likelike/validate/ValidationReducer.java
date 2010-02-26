package org.unigram.likelike.validate;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class ValidationReducer extends
    Reducer<LongWritable, Text, LongWritable, Text> {

    /** 
     * value candidate
     * key targets and candidateFeature
     */
    @Override
    public void reduce(final LongWritable candidate,
            final Iterable<Text> values,
            final Context context)
            throws IOException, InterruptedException {    

        /* extract features */
        List<String> targets = new LinkedList<String>();
        Map<Long, Long> candidateFeature = new HashMap<Long, Long>();  
        for (Text v : values) {
            String inputStr = v.toString();
             String[] infoAry = inputStr.split("\t");
             if (infoAry.length == 1) {  // candidate feature
                 candidateFeature = getFeature(inputStr);
             } else if (infoAry.length == 2) { // targets
                 targets.add(inputStr);
             }
        }
        
        /* run validation */
        for (String targetStr : targets) {
            String[] targetInfoAry = targetStr.split("\t");
            String targetIdStr = targetInfoAry[0];
            String targetFeatureStr = targetInfoAry[1];
            Map<Long, Long> targetFeature 
                = this.getFeature(targetFeatureStr);
            
            /* TODO to be efficient */
            double result 
                = this.calcCosine(candidateFeature, targetFeature);

            if (result >= threshold) {
                context.write(candidate, new Text(targetIdStr));
            }
        }
    }

    private double calcCosine(Map<Long, Long> vectorOne,
            Map<Long, Long> vectorTwo) {
        double ip = (double) calcInnerProduct(vectorOne, vectorTwo);
        double normOne = (double) calcNorm(vectorOne);
        double normTwo = (double) calcNorm(vectorTwo);
        double norm = (normOne * normTwo);
        
        if (norm <= 0.0) {
            return 0.0;
        } else {
            return (ip / norm);
        }

    }    
    
    /**                                                                                                                                                      
     * Calculate inner product between two vectors.                                                                                                          
     *                                                                                                                                                       
     * @param vectorOne vector extracted first query string                                                                                                  
     * @param vectorTwo vector extracted second query string                                                                                                 
     * @return inner product between vectorOne and vectorTwo                                                                                                 
     */
    private long calcInnerProduct(final Map vectorOne,
            final Map vectorTwo) {
        long ip = 0;
        Set<Long> demensions = vectorOne.keySet();
        Iterator iterator = demensions.iterator();
        while (iterator.hasNext()) {
            Long demension = (Long) iterator.next();
            if (vectorTwo.containsKey(demension)) {
                Long vOne = (Long) vectorOne.get(demension);
                Long vTwo = (Long) vectorTwo.get(demension);
                ip += (vOne * vTwo);
            }
        }
        return ip;
    }

    /**                                                                                                                                                      
     * Calculate norm for vector.                                                                                                                            
     *                                                                                                                                                       
     * @param vector character based query string vector                                                                                                     
     * @return norm                                                                                                                                          
     */
    private double calcNorm(final Map vector) {
        long norm = 0;
        Set demensions = vector.keySet();
        Iterator iterator = demensions.iterator();

        while (iterator.hasNext()) {
            Long demension = (Long) iterator.next();
            Long value = (Long) vector.get(demension);
            norm += (value * value);
        }
        return Math.sqrt(norm);
    }

    
    private final Map<Long, Long> getFeature(
            String featureStr) {
        Map<Long, Long> rtMap = new HashMap<Long, Long>();
        String[] featureArray = featureStr.split(" ");
        for (int i=0; i<featureArray.length; i++) {
            String[] segArray = featureArray[i].split(":");
            rtMap.put(Long.parseLong(segArray[0]), 
                    Long.parseLong(segArray[1]));
        }
        return rtMap;
    }    
    
    
    public final void setup(final Context context) {
        Configuration jc = context.getConfiguration();
        this.threshold = jc.getFloat(
                ValidationConstants.VALIDATION_THRESHOLD,
                ValidationConstants.DEFAULT_VALIDATION_THRESHOLD);        
    }    
    
    private float threshold;
}
