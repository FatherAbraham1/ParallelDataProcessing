package assignment3;

import org.apache.hadoop.io.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ronnygeo on 10/18/16.
 */
//LinkedEdges extend ArrayWritable to store the links of a node.
    // Not currently used.
public class LinkedEdges extends ArrayWritable {
//        public LinkedEdges(Class<? extends Writable> elementClass) {
//            super(elementClass);
//        }

    public LinkedEdges() {
        super(Text.class);
    }


    public LinkedEdges(String[] strings) {
            super(Text.class);
            if (strings.length > 0) {
                Text[] texts = new Text[strings.length];
                for (int i = 0; i < strings.length; i++) {
                    texts[i] = new Text(strings[i]);
                }
                set(texts);
            }
        }

        @Override
        public String toString() {
            String value = "";
            Text[] values = (Text[]) get();
            if (values.length > 0) {
                for (int i = 0; i < values.length; i++) {
                    value += values[i];
                    if (i != values.length - 1) {
                        value += ",";
                    }
                }
            }
            return value;
        }

        public int size() {
            Text[] values = (Text[]) get();
            return values.length;
        }
    }
