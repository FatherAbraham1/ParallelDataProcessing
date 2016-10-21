package assignment2.WithCombiner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.metrics2.util.SampleStat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.helpers.NullEnumeration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ronnygeo on 10/5/16.
 */
public class WithCombiner {
    public static class LineMapper
            extends Mapper<Object,Text,Text,MinMaxTemp> {
        private static final Text word = new Text();
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
            //Creating the list of all words in the line
            List<String> words = Arrays.asList(value.toString().split(","));
            //Checking if the line has TMAX, then this line has the maximum temperature
            if (words.indexOf("TMAX") != -1) {
                word.set(words.get(0));
                context.write(word, new MinMaxTemp(99999, Integer.parseInt(words.get(3))));
            }
            //Checking if the line has TMIN, then this line has the minimum temperature
            else if (words.indexOf("TMIN") != -1) {
                word.set(words.get(0));
                context.write(word, new MinMaxTemp(Integer.parseInt(words.get(3)), -99999));
            }
        }
    }

    public static class TempReducer
    extends Reducer<Text,MinMaxTemp,Text,NullWritable> {
        private static final Text result = new Text();

        public void reduce(Text key, Iterable<MinMaxTemp> values, Context context) throws IOException, InterruptedException {
            //getting the mean values
            List<Integer> mlist = getMeans(values);
            result.set(key.toString() + "," + ((mlist.get(0) == 99999)? "NULL": mlist.get(0)) + "," + (mlist.get(1) == -99999? "NULL": mlist.get(1)));
            context.write(result, NullWritable.get());
        }
    }

    //Combiner class
    public static class TempCombiner extends Reducer<Text, MinMaxTemp,Text,MinMaxTemp> {
        private static final Text station = new Text();
        private int count = 0;

        public void reduce(Text key, Iterable<MinMaxTemp> values, Context context) throws IOException, InterruptedException {
            List mlist = getMeans(values);
            count++;
            System.out.println(count);
            context.write(key, new MinMaxTemp((Integer) mlist.get(0), (Integer) mlist.get(1)));
        }
    }

    //Helper method to calculate the min and max mean values.
    private static List getMeans(Iterable<MinMaxTemp> values) {
        List<Integer> meanList = new ArrayList<>();
        int minSum = 0;
        int maxSum = 0;
        int minCount = 0;
        int maxCount = 0;
        int minMean = 99999;
        int maxMean = -99999;
        for (MinMaxTemp val : values) {
            if (val.getMax() != -99999) {
                maxSum += val.getMax();
                maxCount++;
                if (val.ifMinMax()) {
                    minSum += val.getMin();
                    minCount++;
                }
            } else {
                minSum += val.getMin();
                minCount++;
            }
        }

        if (minCount > 0)
            minMean = minSum/minCount;

        if (maxCount > 0)
            maxMean = maxSum/maxCount;

        meanList.add(minMean);
        meanList.add(maxMean);
        return meanList;
    }

    public static void main(String args[]) throws Exception{
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length < 2) {
            System.err.println("Usage: hadoop jar This.jar <in> [<in>...] <out>");
            System.exit(2);
        }
        Job job = new Job(conf, "Temperature with Combiner");
        job.setJarByClass(assignment2.WithCombiner.WithCombiner.class);
        job.setMapperClass(LineMapper.class);
        job.setMapOutputValueClass(MinMaxTemp.class);
        job.setCombinerClass(TempCombiner.class);
        job.setReducerClass(TempReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        for (int i = 0; i < otherArgs.length - 1; ++i) {
            FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
        }
        FileOutputFormat.setOutputPath(job,
                new Path(otherArgs[otherArgs.length - 1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
