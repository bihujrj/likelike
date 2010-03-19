package org.unigram.likelike.validate;

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

/**
 *
 */
public class Validation extends Configured implements Tool {
    
    /**
     * run. 
     * @param args -
     * @return 0
     * @throws Exception -
     */
    @Override
    public int run(final String[] args) throws Exception {
        Configuration conf = getConf();
        return this.run(args, conf);        
    }

    /**
     * run.
     * @param args arguments
     * @param conf configuration
     * @return -
     * @throws Exception -
     */
    public int run(final String[] args, final Configuration conf) 
    throws Exception {
        String recommendDir = "";
        String addedFeatureDir = "";
        String featureDir = "";
        String outputDir = "";
        String tmpOutputDir = "";

        FileSystem fs = FileSystem.get(conf);

        for (int i = 0; i < args.length; ++i) {
            if ("-recommend".equals(args[i])) {
                recommendDir = args[++i];
                addedFeatureDir = recommendDir + ".feature";
            } else if ("-output".equals(args[i])) {
                outputDir = args[++i];
                tmpOutputDir = outputDir + ".tmp";
            } else if ("-feature".equals(args[i])) {
                featureDir = args[++i];
            } else if ("-threshold".equals(args[i])) {
                conf.setFloat(ValidationConstants.VALIDATION_THRESHOLD, 
                        Float.parseFloat(args[++i]));                
            }                      
        }

        this.addCandidateFeatures(recommendDir, 
                addedFeatureDir, featureDir, conf, fs);
        
        this.validate(addedFeatureDir, 
                    tmpOutputDir, featureDir, conf, fs);
        
        this.inverse(tmpOutputDir, outputDir, conf, fs);
        
        //FsUtil.clean(fs, tmpOutputDir);
        
        return 0;
    }    

    /**
     * inverse.
     * @param inputDir -
     * @param outputDir -
     * @param conf -
     * @param fs -
     * @return -
     * @throws IOException -
     * @throws InterruptedException -
     * @throws ClassNotFoundException -
     */
    private boolean inverse(final String inputDir, 
            final String outputDir, final Configuration conf, 
            final FileSystem fs) 
    throws IOException, InterruptedException, ClassNotFoundException {
        Path inputPath = new Path(inputDir);
        Path outputPath = new Path(outputDir);
        FsUtil.checkPath(outputPath, FileSystem.get(conf));
        
        Job job = new Job(conf);
        job.setJarByClass(Validation.class);
        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);
        job.setMapperClass(InverseMapper.class); 
        job.setReducerClass(IdentityReducer.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(conf.getInt(LikelikeConstants.NUMBER_OF_REDUCES,
                LikelikeConstants.DEFAULT_NUMBER_OF_REDUCES));

        return job.waitForCompletion(true);
    }

    /**
     * validate.
     * @param addedFeatureDir -
     * @param outputDir -
     * @param featureDir -
     * @param conf -
     * @param fs -
     * @return 0
     * @throws IOException -
     * @throws InterruptedException -
     * @throws ClassNotFoundException -
     */
    private boolean validate(final String addedFeatureDir, 
            final String outputDir, final String featureDir,
            final Configuration conf, final FileSystem fs)
    throws IOException, InterruptedException, ClassNotFoundException {
        Path addedfeaturePath = new Path(addedFeatureDir);
        Path outputPath = new Path(outputDir);
        Path featurePath = new Path(featureDir);
        FsUtil.checkPath(outputPath, FileSystem.get(conf));
        
        Job job = new Job(conf);
        job.setJarByClass(Validation.class);
        FileInputFormat.addInputPath(job, addedfeaturePath);
        FileInputFormat.addInputPath(job, featurePath);
        FileOutputFormat.setOutputPath(job, outputPath);
        job.setMapperClass(ValidationMapper.class); 
        job.setReducerClass(ValidationReducer.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(conf.getInt(LikelikeConstants.NUMBER_OF_REDUCES,
                LikelikeConstants.DEFAULT_NUMBER_OF_REDUCES));

        return job.waitForCompletion(true);                  
    }
    
    /**
     * addCandidateFeatures.
     * @param recommendDir -
     * @param outputFile -
     * @param featureDir -
     * @param conf -
     * @param fs -
     * @return 1
     * @throws IOException -
     * @throws InterruptedException -
     * @throws ClassNotFoundException -
     */
    private boolean addCandidateFeatures(final String recommendDir, 
            final String outputFile, final String featureDir,
            final Configuration conf, final FileSystem fs) throws 
            IOException, InterruptedException, ClassNotFoundException {
        Path recommendPath = new Path(recommendDir);
        Path featurePath = new Path(featureDir);
        Path outputPath = new Path(outputFile);
        FsUtil.checkPath(outputPath, FileSystem.get(conf));

        Job job = new Job(conf);
        job.setJarByClass(Validation.class);
        FileInputFormat.addInputPath(job, recommendPath);
        FileInputFormat.addInputPath(job, featurePath);
        FileOutputFormat.setOutputPath(job, outputPath);
        job.setMapperClass(AddCandidateFeatureMapper.class); 
        job.setReducerClass(AddCandidateFeatureReducer.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(LongWritable.class);
        job.setNumReduceTasks(conf.getInt(LikelikeConstants.NUMBER_OF_REDUCES,
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
                new Validation(), args);
        System.exit(exitCode);
    }    
}
