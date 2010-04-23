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
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.unigram.likelike.common.Candidate;
import org.unigram.likelike.lsh.GetRecommendationsReducer;

import junit.framework.TestCase;

import static org.mockito.Mockito.*;
import org.mockito.InOrder;

public class TestGetRecommendationsReducer extends TestCase {

    public TestGetRecommendationsReducer(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testReduce() {
        GetRecommendationsReducer reducer = new GetRecommendationsReducer();
        Reducer<LongWritable, MapWritable, 
        LongWritable, LongWritable>.Context mock_context 
        = mock(Reducer.Context.class);        

    }
}
