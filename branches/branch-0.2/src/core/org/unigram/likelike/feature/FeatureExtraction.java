package org.unigram.likelike.feature;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import org.unigram.likelike.common.FsUtil;
import org.unigram.likelike.common.LikelikeConstants;
import org.unigram.likelike.util.IdentityReducer;
import org.unigram.likelike.util.InverseMapper;
import org.unigram.likelike.util.AddFeatureMapper;
import org.unigram.likelike.util.AddFeatureReducer;

public class FeatureExtraction  extends Configured 
    implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        return this.run(args, conf);   
    }

    
    public int run(String[] args, Configuration conf) 
        throws IOException, InterruptedException, ClassNotFoundException {
        
        String recommendDir = "";
        String inversedDir = "";
        String addedFeatureDir = "";
        String featureDir = "";
        String outputDir = "";
        String tmpOutputDir = "";

        this.fs = FileSystem.get(conf);

        for (int i = 0; i < args.length; ++i) {
            if ("-recommend".equals(args[i])) {
                recommendDir = args[++i];
                inversedDir = recommendDir + ".inv";
                addedFeatureDir = recommendDir + ".feature";
            } else if ("-output".equals(args[i])) {
                outputDir = args[++i];
                tmpOutputDir = outputDir + ".tmp";
            } else if ("-feature".equals(args[i])) {
                featureDir = args[++i];
            } 
        }
        
        this.inverse(recommendDir, inversedDir, conf);
        this.addFeatures(inversedDir, addedFeatureDir, featureDir, conf);
        
        
        this.relatedFeatureExtraction(addedFeatureDir, 
                outputDir, featureDir, conf);
        
        FsUtil.clean(this.fs, addedFeatureDir, inversedDir);
        
        return 0;
    }        

    private boolean relatedFeatureExtraction(String addedFeatureDir,
            String outputDir, String featureDir, Configuration conf) 
           throws  IOException, InterruptedException, ClassNotFoundException {
        Path addedfeaturePath = new Path(addedFeatureDir);
        Path outputPath = new Path(outputDir);
        Path featurePath = new Path(featureDir);
        FsUtil.checkPath(outputPath, this.fs);
        
        Job job = new Job(conf);
        job.setJarByClass(FeatureExtraction.class);
        FileInputFormat.addInputPath(job, addedfeaturePath);
        FileInputFormat.addInputPath(job, featurePath);
        FileOutputFormat.setOutputPath(job, outputPath);
        job.setMapperClass(FeatureExtractionMapper.class); 
        job.setReducerClass(FeatureExtractionReducer.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(
                conf.getInt(LikelikeConstants.NUMBER_OF_REDUCES,
                LikelikeConstants.DEFAULT_NUMBER_OF_REDUCES));

        return job.waitForCompletion(true);            
    }


    private boolean inverse(String inputDir, String outputDir,
            Configuration conf) 
    throws IOException, InterruptedException, ClassNotFoundException {
        Path inputPath = new Path(inputDir);
        Path outputPath = new Path(outputDir);
        FsUtil.checkPath(outputPath, this.fs);
        
        Job job = new Job(conf);
        job.setJarByClass(FeatureExtraction.class);
        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);
        job.setMapperClass(InverseMapper.class); 
        job.setReducerClass(IdentityReducer.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(
                conf.getInt(LikelikeConstants.NUMBER_OF_REDUCES,
                LikelikeConstants.DEFAULT_NUMBER_OF_REDUCES));

        return job.waitForCompletion(true);
    }    

    private boolean addFeatures(String recommendDir, 
            String outputFile, String featureDir,
            Configuration conf) throws 
            IOException, InterruptedException, ClassNotFoundException {
        Path recommendPath = new Path(recommendDir);
        Path featurePath = new Path(featureDir);
        Path outputPath = new Path(outputFile);
        FsUtil.checkPath(outputPath, this.fs);

        Job job = new Job(conf);
        job.setJarByClass(FeatureExtraction.class);
        FileInputFormat.addInputPath(job, recommendPath);
        FileInputFormat.addInputPath(job, featurePath);
        FileOutputFormat.setOutputPath(job, outputPath);
        job.setMapperClass(AddFeatureMapper.class); 
        job.setReducerClass(AddFeatureReducer.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(LongWritable.class);
        job.setNumReduceTasks(
                conf.getInt(LikelikeConstants.NUMBER_OF_REDUCES,
                LikelikeConstants.DEFAULT_NUMBER_OF_REDUCES));

        return job.waitForCompletion(true);          
    }    
    
    
    
    /**
     * Main method.
     *
     * @param args argument strings which contain input and output files.
     * @throws Exception -
     */
    public static void main(final String[] args)
    throws Exception {
        int exitCode = ToolRunner.run(
                new FeatureExtraction(), args);
        System.exit(exitCode);
    }
    
    private FileSystem fs = null;
    
}
