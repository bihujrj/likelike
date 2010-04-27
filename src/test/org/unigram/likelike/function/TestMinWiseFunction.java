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
package org.unigram.likelike.lsh.function;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.MinimalHTMLWriter;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.unigram.likelike.common.LikelikeConstants;
import org.unigram.likelike.lsh.SelectClustersMapper;
import org.unigram.likelike.lsh.function.IHashFunction;
import org.unigram.likelike.lsh.function.MinWiseFunction;

public class TestMinWiseFunction extends TestCase {

    public TestMinWiseFunction(String name) {
        super(name);
    }
    
    private Set<Long> initMap(Long[] keys) {
        Set<Long> rtMap = new HashSet<Long>();
        for(int i=0;i<keys.length;i++) {
            rtMap.add(keys[i]);
        }
        return rtMap;
    }
    
    private IHashFunction createFunction(int depth) {
        Configuration conf = new Configuration();        
        conf.setLong(SelectClustersMapper.MINWISE_HASH_SEEDS, 1438L);
        conf.setInt(LikelikeConstants.FEATURE_DEPTH, depth);        
        return (IHashFunction) new MinWiseFunction(conf);
    }    
    
    public void testReturnHashDepth() {
        IHashFunction function = this.createFunction(2);
        long seed = 1438L;
        
        Long[] keys1   = {10L, 438L, 43L, 438L, 3489L};
        Set<Long> set1 = this.initMap(keys1);
        LongWritable hashValue1 = function.returnClusterId(set1, seed);
        
        Long[] keys2   = {10L, 438L, 43L, 438L, 3489L};
        Set<Long> set2 = this.initMap(keys2);        
        LongWritable hashValue2 = function.returnClusterId(set2, seed);
        
        Long[] keys3   = {11L, 439L};
        Set<Long> set3 = this.initMap(keys1);  
        LongWritable hashValue3 = function.returnClusterId(set3, seed);        
        
        assertEquals(hashValue1, hashValue2);
        assertFalse((hashValue1 == hashValue3));
        
        function = this.createFunction(1);
        long seed2 = 1438L; 
        LongWritable hashValue1_dash = function.returnClusterId(set1, seed2);

        assertFalse(hashValue1_dash == hashValue1);
        
    }

    public void testReturnHashValue() {
        final int depth = 1; 
        IHashFunction function = this.createFunction(depth);
        long seed = 1438L;
        
        Long[] keys1   = {10L, 438L, 43L, 438L, 3489L};
        Set<Long> set1 = this.initMap(keys1);
        LongWritable hashValue1 = function.returnClusterId(set1, seed);
        
        Long[] keys2   = {10L, 438L, 43L, 438L, 3489L};
        Set<Long> set2 = this.initMap(keys2);        
        LongWritable hashValue2 = function.returnClusterId(set2, seed);
        
        Long[] keys3   = {11L, 439L};
        Set<Long> set3 = this.initMap(keys1);        
        LongWritable hashValue3 = function.returnClusterId(set3, seed);        
        
        assertEquals(hashValue1, hashValue2);
        assertFalse((hashValue1 == hashValue3));

        function = this.createFunction(depth);
        long seed2 = 243L;
        LongWritable hashValue1_dash = function.returnClusterId(set1, seed);
        LongWritable hashValue2_dash = function.returnClusterId(set2, seed);
        
        assertFalse(hashValue1 
                == hashValue1_dash);

        assertFalse(hashValue2 
                == hashValue2_dash);        
    }
}