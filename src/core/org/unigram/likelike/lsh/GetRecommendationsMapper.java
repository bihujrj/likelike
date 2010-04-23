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
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.unigram.likelike.common.Candidate;
import org.unigram.likelike.common.RelatedUsersWritable;
import org.unigram.likelike.common.SeedClusterId;

/**
 * Mapper. 
 */
public class GetRecommendationsMapper extends
        Mapper<SeedClusterId, RelatedUsersWritable, LongWritable, MapWritable> {
    
    /**
     * Map method.
     * 
     * @param key dummy
     * @param value related users
     * @param context for writing
     * @throws IOException -
     * @throws InterruptedException -
     */
    @Override
    public final void map(final SeedClusterId key,
            final RelatedUsersWritable value, final Context context) 
    throws IOException, InterruptedException  {
        List<LongWritable> users = value.getRelatedUsers();
        MapWritable relatedUsers = this.createUserMap(value.getRelatedUsers());
        
        //System.out.println("users:" + users);
        
        for (int i = 0; i < users.size(); i++) {
            LongWritable targetId 
                = new LongWritable(users.get(i).get());
            //System.out.println("\n\ntargetId: " + targetId);
            context.write(targetId, new MapWritable(relatedUsers));          
        }
    }

    MapWritable createUserMap(List<LongWritable> relatedUsers) {
        HashMap<LongWritable, FloatWritable> tmpMap
                = new HashMap<LongWritable, FloatWritable>();
        for (LongWritable user : relatedUsers) {
            tmpMap.put(user, new FloatWritable(1.0F));
        }
        MapWritable rtMap = new MapWritable();
        rtMap.putAll(tmpMap);
        return rtMap;
    }
    

    
}
