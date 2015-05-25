/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.aggregations.pipeline;

import com.google.common.collect.Lists;

import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.InternalAggregation.ReduceContext;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.aggregations.InternalMultiBucketAggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SiblingPipelineAggregator extends PipelineAggregator {

    protected SiblingPipelineAggregator() { // for Serialisation
        super();
    }

    protected SiblingPipelineAggregator(String name, String[] bucketsPaths, Map<String, Object> metaData) {
        super(name, bucketsPaths, metaData);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InternalAggregation reduce(InternalAggregation aggregation, ReduceContext reduceContext) {
        @SuppressWarnings("rawtypes")
        InternalMultiBucketAggregation multiBucketsAgg = (InternalMultiBucketAggregation) aggregation;
        List<? extends Bucket> buckets = multiBucketsAgg.getBuckets();
        List<Bucket> newBuckets = new ArrayList<>();
        for (int i = 0; i < buckets.size(); i++) {
            InternalMultiBucketAggregation.InternalBucket bucket = (InternalMultiBucketAggregation.InternalBucket) buckets.get(i);
            InternalAggregation aggToAdd = doReduce(bucket.getAggregations(), reduceContext);
            List<InternalAggregation> aggs = new ArrayList<>(Lists.transform(bucket.getAggregations().asList(), AGGREGATION_TRANFORM_FUNCTION));
            aggs.add(aggToAdd);
            InternalMultiBucketAggregation.InternalBucket newBucket = multiBucketsAgg.createBucket(new InternalAggregations(aggs), bucket);
            newBuckets.add(newBucket);
        }

        return multiBucketsAgg.create(newBuckets);
    }

    public abstract InternalAggregation doReduce(Aggregations aggregations, ReduceContext context);
}