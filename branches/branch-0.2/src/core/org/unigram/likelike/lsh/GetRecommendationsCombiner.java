/**
 * Copyright 2009 Takahiko Ito
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *        
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.unigram.likelike.lsh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.unigram.likelike.common.LikelikeConstants;
import org.unigram.likelike.common.RelatedUsersWritable;

/**
 * Reducer implementation. Extract pairs related to each other.
 */
public class GetRecommendationsCombiner extends
        Reducer<LongWritable, MapWritable, 
        LongWritable, MapWritable> {

    /**
     * reduce. 
     * @param targetId target
     * @param values candidates
     * @param context -
     * @throws IOException - 
     * @throws InterruptedException -
     */
    public void reduce(final LongWritable targetId,
            final Iterable<MapWritable> values,
            final Context context)
            throws IOException, InterruptedException {
        
        Map<LongWritable, FloatWritable> candidates 
            = new HashMap<LongWritable, FloatWritable>();
        
        for (MapWritable candMap : values) {
            Set<Writable> cands = candMap.keySet();
            for (Writable cand : cands) {
                LongWritable candId = (LongWritable) cand;
                if (candId.equals(targetId)) { continue; }
                
                if (candidates.containsKey(candId)) {
                    FloatWritable weight = candidates.get(candId);
                    FloatWritable tw = (FloatWritable) candMap.get(candId);
                    weight.set(tw.get() + weight.get());
                    candidates.put(candId, weight);
                } else {
                    candidates.put(candId, 
                            new FloatWritable(1.0F));
                }
            
                if (candidates.size() > 50000) { // TODO should be parameterized
                    break;
                }
            }
        }
        
        MapWritable rtMap = new MapWritable();
        rtMap.putAll(candidates);
        context.write(targetId, rtMap);
    }
    
    /**
     * setup.
     * 
     * @param context contains Configuration object to get settings
     */
    @Override
    public final void setup(final Context context) {
        Configuration jc = null; 
        if (context == null) {
            jc = new Configuration();
        } else {
            jc = context.getConfiguration();
        }
    }    
}
