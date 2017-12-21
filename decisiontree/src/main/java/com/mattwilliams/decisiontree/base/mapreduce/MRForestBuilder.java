package com.mattwilliams.decisiontree.base.mapreduce;

import com.google.gson.Gson;
import com.mattwilliams.decisiontree.algorithms.RandomInfoGainStrategy;
import com.mattwilliams.decisiontree.base.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The MRForestBuilder class builds a random forest model using
 * Hadoop MapReduce functionality.
 */
public class MRForestBuilder extends RandomForestBuilder {

    private String treeFile = "/users/matt/tree.txt";
    private String keyFile = "/users/matt/key.txt";
    private String outputPath = "/users/matt/output";
    private String jobName = "abd5";

    /**
     * Creates a MRForestBuilder
     *
     * @param numTrees - number of trees to grow
     * @param label1   - first classification label
     * @param label2   - second classification label
     */
    public MRForestBuilder(int numTrees, String label1, String label2) {
        super(numTrees, label1, label2);
    }

    @Override
    public Predictor train(Matrix matrix) {

        RandomForest randomForest = new RandomForest();

        try {
            Configuration conf = getConfiguration();

            // This is the strategy I'm using to grow trees using MapReduce:
            //
            // Step 1: Write the input feature matrix to a file on HDFS (tree.txt):
            String csv = matrix.toCsv();
            writeHdfsFile(treeFile, csv, conf);

            // Step 2: Create a file on HDFS with a path to this input file, repeated
            // once for however many trees are being grown. The idea is to use NLineInputFormat
            // to feed each mapper the path to the input file, and let the Mapper read it and
            // grow a tree. In this way we make sure that # of Mappers = # of trees:
            ArrayList<String> filenames = new ArrayList<>();

            for (int i=0; i<numTrees; ++i) {
                filenames.add(treeFile);
            }
            String filelist = String.join(System.lineSeparator(), filenames);
            writeHdfsFile(keyFile, filelist, conf);

            // Step 3: Run the MapReduce job
            Job job = getJob(conf);
            job.waitForCompletion(true);

            // Step 4: Find the output file and deserialize it back into a RandomForest
            String json = readHdfsFile("/users/matt/output/part-r-00000", conf).get(0);
            Gson gson = new Gson();
            randomForest = gson.fromJson(json, RandomForest.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return randomForest;

    }

    /**
     * Utility method for writing a file to HDFS
     * @param destination - file path including name
     * @param content - content of the file
     * @param conf - Hadoop configuration
     * @throws IOException
     */
    private static void writeHdfsFile(String destination, String content, Configuration conf) throws IOException {
        FileSystem fs = FileSystem.get(URI.create(destination), conf);
        OutputStream out = fs.create(new Path(destination));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write(content);
        writer.close();
    }

    /**
     * Utility method for reading a file on HDFS
     * @param path - file to read
     * @param conf - Hadoop configuration
     * @return - the lines in the file as a list of Strings
     * @throws IOException
     */
    private static List<String> readHdfsFile(String path, Configuration conf) throws IOException {
        FileSystem fs = FileSystem.get(URI.create(path), conf);
        InputStream in = fs.open(new Path(path));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        List<String> contents = new ArrayList<>();
        reader.lines().forEach(contents::add);
        reader.close();
        return contents;
    }

    private static class TreeMapper extends Mapper<Object, Text, Object, Text> {
        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            String file = value.toString(); // This is the input feature file, tree.txt
            List<String> inputFileRows = readHdfsFile(file, conf);
            ArrayList<Row> matrixRows = new ArrayList<>();

            // Read all the feature rows
            for (String inputDataRow : inputFileRows) {
                String[] inputData = inputDataRow.split(",");
                Row row = new Row();
                row.addFeature(Double.parseDouble(inputData[0]));
                row.addFeature(Double.parseDouble(inputData[1]));
                row.addFeature(Double.parseDouble(inputData[2]));
                row.addFeature(Double.parseDouble(inputData[3]));
                row.setLabel(inputData[4]);
                matrixRows.add(row);
            }

            // Draw a random subset of the original rows
            Collections.shuffle(matrixRows);
            int numRows = (int) (matrixRows.size() * SUBSET);

            // Create a new matrix from the random subset
            Matrix dataSubset = new Matrix();
            for (int j=0; j<numRows; j++) {
                dataSubset.addRow(matrixRows.get(j));
            }

            // Create a tree
            DecisionTreeBuilder treeBuilder = new DecisionTreeBuilder(conf.get("abd5.label1"), conf.get("abd5.label2"),
                    new RandomInfoGainStrategy());
            TreeNode tree = (TreeNode)treeBuilder.train(dataSubset);

            // Serialize the tree to JSON and store it as mapper output
            Gson gson = new Gson();
            String json = gson.toJson(tree);
            context.write(NullWritable.get(), new Text(json));
        }
    }

    private static class ForestReducer extends Reducer<Object, Text, Object, Text> {
        @Override
        protected void reduce(Object key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            RandomForest forest = new RandomForest();
            Gson gson = new Gson();

            for (Text text : values) {
                TreeNode tree = gson.fromJson(text.toString(), TreeNode.class);
                forest.addTree(tree);
            }
            context.write(NullWritable.get(), new Text(gson.toJson(forest)));
        }
    }

    private Configuration getConfiguration() {
        Configuration conf = new Configuration();

        // These are for running Hadoop locally on my Mac (installed using Homebrew)
        conf.addResource(new Path("/usr/local/Cellar/hadoop/2.8.2/libexec/etc/hadoop/hdfs-site.xml"));
        conf.addResource(new Path("/usr/local/Cellar/hadoop/2.8.2/libexec/etc/hadoop/core-site.xml"));
        conf.addResource(new Path("/usr/local/Cellar/hadoop/2.8.2/libexec/etc/hadoop/mapred-site.xml"));

        // Label values so that the Mapper can get to them
        conf.set("abd5.label1", label1);
        conf.set("abd5.label2", label2);

        // This makes sure that each Mapper will get a single line of text, which will be
        // the path to the input feature matrix file on HDFS
        conf.set("mapreduce.input.lineinputformat.linespermap", "1");
        return conf;
    }

    private Job getJob(Configuration conf) throws IOException {
        Job job = Job.getInstance(conf, jobName);
        job.setMapperClass(TreeMapper.class);
        job.setReducerClass(ForestReducer.class);
        job.setJarByClass(MRForestBuilder.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(NLineInputFormat.class); // Makes sure Mappers get only 1 line
        FileInputFormat.addInputPath(job, new Path(keyFile));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        return job;
    }
}
