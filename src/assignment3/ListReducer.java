package assignment3;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ronnygeo on 10/17/16.
 */
public class ListReducer extends Reducer<Text, Text, Node, NullWritable> {

    public void setup(Context ctx) {

    }


    public void reduce(Text key, Iterable<Text> values, Context ctx) throws IOException, InterruptedException {
        ArrayList<String> list = new ArrayList<>();
        Node node = new Node(key.toString());
        node.setPageRank(0);
        for (Text val : values) {
            if (!key.toString().equals(val.toString()))
            list.add(val.toString());
        }
        node.setLinks(list);
        ctx.write(node, NullWritable.get());
    }

    public void cleanup(Context ctx) {
    }
}
