import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MatrixMultiplication {
    public static class MultiplicationMapper extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: movie_B \t movie_A=ratio
            // output: < key=movie_B, value="movie_A=ratio" >

            String[] tokens = value.toString().trim().split("\t");
            context.write(new Text(tokens[0]), new Text(tokens[1]));
        }
    }

    public static class RatingMatrixMapper extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: userID, movieID, rating
            // output: < key=movie_B, value="userID: rating" >

            String[] tokens = value.toString().trim().split(",");
            String userID = tokens[0];
            String movieID = tokens[1];
            String rating = tokens[2];

            context.write(new Text(movieID), new Text(userID + ":" + rating));
        }
    }

    public static class  MultiplicationReducer extends Reducer<Text, Text, Text, DoubleWritable> {
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            // input: < key=movie_B, value="movie_A=ratio1, movie_C=ratio2, ..., user1: rating1, user2: rating2, ..." >
            // output: < key="userID: movieID", value=ratio * rating >

            Map<String, Double> movieRatioMap = new HashMap<String, Double>();
            Map<String, Double> userRatingMap = new HashMap<String, Double>();

            for (Text value: values) {
                if (value.toString().contains("=")) {
                    String[] movieRatio = value.toString().trim().split("=");
                    movieRatioMap.put(movieRatio[0], Double.parseDouble(movieRatio[1]));
                } else {
                    String[] userRating = value.toString().trim().split(":");
                    userRatingMap.put(userRating[0], Double.parseDouble(userRating[1]));
                }
            }

            for (Map.Entry<String, Double> entry: movieRatioMap.entrySet()) {
                String movie = entry.getKey();
                double ratio = entry.getValue();

                for (Map.Entry<String, Double> element: userRatingMap.entrySet()) {
                    String user = element.getKey();
                    double rating = element.getValue();

                    context.write(new Text(user + ":" + movie), new DoubleWritable(ratio * rating));
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);
        job.setJarByClass(MatrixMultiplication.class);

//        ChainMapper.addMapper(job, MultiplicationMapper.class, LongWritable.class, Text.class, Text.class, Text.class, conf);
//        ChainMapper.addMapper(job, RatingMatrixMapper.class, Text.class, Text.class, Text.class, Text.class, conf);

        job.setMapperClass(MultiplicationMapper.class);
        job.setMapperClass(RatingMatrixMapper.class);

        job.setReducerClass(MultiplicationReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, MultiplicationMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, RatingMatrixMapper.class);

        TextOutputFormat.setOutputPath(job, new Path(args[2]));

        job.waitForCompletion(true);
    }
}
