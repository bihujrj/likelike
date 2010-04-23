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
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.unigram.likelike.common.Candidate;
import org.unigram.likelike.common.SeedClusterId;
import org.unigram.likelike.common.RelatedUsersWritable;
import org.unigram.likelike.lsh.GetRecommendationsMapper;


import junit.framework.TestCase;

import static org.mockito.Mockito.*;

public class TestGetRecommendationsMapper extends TestCase {

    public TestGetRecommendationsMapper(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testMap() {
        GetRecommendationsMapper mapper =
            new GetRecommendationsMapper();

        Mapper<SeedClusterId, RelatedUsersWritable, LongWritable, 
        MapWritable>.Context mock_context
            = mock(Mapper.Context.class);        
        
        List<LongWritable> value = new ArrayList<LongWritable>();
        value.add(new LongWritable(1));
        value.add(new LongWritable(443));
        value.add(new LongWritable(2));
        value.add(new LongWritable(5));
        value.add(new LongWritable(3));
        value.add(new LongWritable(54));
        value.add(new LongWritable(434));
        
        SeedClusterId hashedClusterId 
            = new SeedClusterId(1L,143248978L); 
        LongWritable clusterSize 
            = new LongWritable(7L); 
        try {
            /*
             * key - hashed clusterId
             * value - example ids exist in the cluster with clusterId. 
             */
            mapper.map(hashedClusterId, new RelatedUsersWritable(value), mock_context);
        } catch (IOException e) {
            e.printStackTrace();
            TestCase.fail();
         } catch (InterruptedException e) {
             e.printStackTrace();
             TestCase.fail();
         } catch (Exception e) {
             e.printStackTrace();
             TestCase.fail();
         }
        
         
         MapWritable expectedValue = mapper.createUserMap(value);
         //try {
             /* case: simple */
             //verify(mock_context, times(1)).write(new LongWritable(1L),
             //expectedValue);

             //verify(mock_context, times(1)).write(new LongWritable(5L),
             //        expectedValue);
             
             /* case: symmetric */
             //verify(mock_context, times(1)).write(new LongWritable(443L),
             //        expectedValue);
             
             /* case: self recommendaton */
             //verify(mock_context, times(0)).write(new LongWritable(443L),
             //        expectedValue);
             
             /* case: id not in the cluster */
             //verify(mock_context, times(0)).write(new LongWritable(98L),
             //        expectedValue);
             
         //} catch (Exception e) {
         //    TestCase.fail();
         //}         
        
    }
    
}
