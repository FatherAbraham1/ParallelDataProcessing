package assignment1.WordCount;

/**
 * Created by ronnygeo on 9/27/16.
 */


/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;

/*
The default partitioner, partitions the data using the key.
int x = key.hashcode();
return x % numPartitions;
 */
public class WordSort {

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        private final static Pattern nw1 = Pattern.compile("[^'a-zA-Z]");
        private final static Pattern     nw2 = Pattern.compile("(^'+|'+$)");
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        private HashSet<String> words;

        public void setup(Context context) {
            words = new HashSet<>();
        }
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                Matcher mm1 = nw1.matcher(itr.nextToken());
                Matcher mm2 = nw2.matcher(mm1.replaceAll(""));
                String ww = mm2.replaceAll("").toLowerCase();

                if (!ww.equals("")) {
                    words.add(ww);
                }
            }

        }

        public void cleanup(Context context) throws InterruptedException, IOException{
            for (String ww: words) {
                word.set(ww);
                context.write(word, one);
            }
        }
    }

    public static class IntSumReducer
            extends Reducer<Text,IntWritable,Text,IntWritable> {
        private IntWritable result = new IntWritable();
        private IntWritable one = new IntWritable(1);

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, one);
        }
    }


    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length < 2) {
            System.err.println("Usage: hadoop jar This.jar <in> [<in>...] <out>");
            System.exit(2);
        }
        Job job = new Job(conf, "word count");
        job.setJarByClass(WordSort.class);
        job.setMapperClass(TokenizerMapper.class);
        //job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setPartitionerClass(SortPartitioner.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.setNumReduceTasks(2);
        for (int i = 0; i < otherArgs.length - 1; ++i) {
            FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
        }
        FileOutputFormat.setOutputPath(job,
                new Path(otherArgs[otherArgs.length - 1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

//Sort Partitioner
class SortPartitioner extends Partitioner<Text, IntWritable>{
    public SortPartitioner() {
    }

    public int getPartition(Text key, IntWritable value, int numReducers){
        int A = Character.getNumericValue('a');
        int Z = Character.getNumericValue('z');
        int R = Z - A + 1;
        int C = R / numReducers;
        char flet = key.toString().charAt(0);
        int F = Character.getNumericValue(flet) - A;

        return (F/C);
    }
}
