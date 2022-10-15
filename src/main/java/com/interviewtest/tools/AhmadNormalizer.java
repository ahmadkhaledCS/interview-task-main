package com.interviewtest.tools;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class AhmadNormalizer implements Normalizer{
    ArrayList<ArrayList<String>>data;
    ArrayList<Double> desiredColumnNumbers;
    BigDecimal mean;
    int indexOfColumn;
    @Override
    public ScoringSummary zscore(Path sourcePath, Path destPath, String colToStandardize) {
        return normalize(sourcePath,destPath,colToStandardize,"z-score");
    }
    @Override
    public ScoringSummary minMaxScaling(Path sourcePath, Path destPath, String colToNormalize) {
        return normalize(sourcePath,destPath,colToNormalize,"min-max");
    }

    private ScoringSummary normalize(Path sourcePath, Path destPath, String colToNormalize,String normalizationType){
        String fileType;
        String outputFileType;
        try {
            //get input file type from path
            String []tempArray=sourcePath.toString().split("\\.");
            fileType=tempArray[tempArray.length-1];

            //get output file type from path
            tempArray=destPath.toString().split("\\.");
            outputFileType=tempArray[tempArray.length-1];
        }catch (Exception e){
            throw new IllegalArgumentException("source file not found");
        }

        //get the scoring summary this has to run before creating the file
        ScoringSummary summary=getSummary(sourcePath,colToNormalize,fileType);

        //creating the new file
        switch (outputFileType){
            case "csv":createNewCSVFile(destPath,normalizationType,summary);
                break;
            case "json":createNewJSONFile(destPath,normalizationType,summary);
                break;
            default:
                throw new IllegalArgumentException("unsupported output file type only json|csv is allowed");
        }
        return summary;
    }

    private ScoringSummary getSummary(Path sourcePath, String colToWorkOn,String fileType){

        //initializing the variables
        data=new ArrayList<>();
        mean=new BigDecimal("0");
        BigDecimal standardDeviation=new BigDecimal("0");
        BigDecimal variance;
        BigDecimal median;
        BigDecimal min;
        BigDecimal max;

        //extract data fills the objects we need to calculate the summary
        //and saves data to write it later
        switch (fileType){
            case "csv":extractCSVData(sourcePath,colToWorkOn);
                break;
            case "json":extractJSONData(sourcePath,colToWorkOn);
                break;
            default:
                throw new IllegalArgumentException("source file not found");
        }

        int rows = desiredColumnNumbers.size();

        //the mean contains the sum it was filled from extracted data
        mean=mean.divide(new BigDecimal(rows),RoundingMode.HALF_EVEN)
                .setScale(2,RoundingMode.HALF_EVEN);

        //sort the array to get the median and min, max
        Collections.sort(desiredColumnNumbers);

        //second iteration on the data to calculate the std
        for(Double num: desiredColumnNumbers){
            standardDeviation=standardDeviation.add(new BigDecimal(String.valueOf(num)).subtract(mean).pow(2));
        }

        standardDeviation=standardDeviation.divide(new BigDecimal(String.valueOf(rows)),RoundingMode.HALF_EVEN)
                .sqrt(new MathContext(standardDeviation.precision()))
                .setScale(2,RoundingMode.CEILING);

        //calculate the variance after getting the std
        variance=standardDeviation.pow(2)
                .setScale(0,RoundingMode.HALF_EVEN)
                .setScale(2,RoundingMode.HALF_EVEN);

        //find the median
        int half = rows /2;
        median= BigDecimal.valueOf(desiredColumnNumbers.get(half));

        //if it's an even set we change the median
        if(rows %2==0){
            median= BigDecimal.valueOf((desiredColumnNumbers.get(half) + desiredColumnNumbers.get(half- 1) ) / 2.0);
        }
        median=median.setScale(2,RoundingMode.HALF_EVEN);

        //min and max are last and first element because we sorted the list
        min=new BigDecimal(String.valueOf(desiredColumnNumbers.get(0))).setScale(2,RoundingMode.HALF_EVEN);
        max=new BigDecimal(String.valueOf(desiredColumnNumbers.get(rows -1))).setScale(2,RoundingMode.HALF_EVEN);

        return new AhmadScoringNormalizer(mean,standardDeviation,variance,median,min,max);
    }

    private void extractCSVData(Path sourcePath,String colToWorkOn) {
        Scanner scanner;
        try {
            scanner = new Scanner(sourcePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //arrayList to store the numbers in the desired column
        desiredColumnNumbers =new ArrayList<>();
        //read the first line to get desired column index
        data.add(new ArrayList<>(Arrays.asList(scanner.nextLine().split(","))));
        indexOfColumn = data.get(0).indexOf(colToWorkOn);

        //if the index is -1 that means the desired column doesn't exist
        if(indexOfColumn==-1)
            throw new IllegalArgumentException(String.format("column %s not found",colToWorkOn));

        //first iteration to get mean and store data
        String []temp;
        while (scanner.hasNext()){
            temp=scanner.nextLine().split(",");
            desiredColumnNumbers.add(Double.parseDouble(temp[indexOfColumn]));
            //I am currently saving the summation in the mean variable
            mean=mean.add(new BigDecimal(String.valueOf(temp[indexOfColumn])));
            data.add(new ArrayList<>(Arrays.asList(temp)));
        }
    }

    private void extractJSONData(Path sourcePath,String colToWorkOn) {
        Reader reader;

        //arrayList to store the numbers in the desired column
        desiredColumnNumbers =new ArrayList<>();

        try {
            reader = Files.newBufferedReader(sourcePath);
        } catch (IOException e) {
            throw new RuntimeException("corrupted or non existing json file");
        }
        //read json file into an array of maps
        Map<?,?>[] list = new Gson().fromJson(reader, Map[].class);

        indexOfColumn = new ArrayList<>(list[0].keySet()).indexOf(colToWorkOn);

        //if the index is -1 that means the desired column doesn't exist
        if(indexOfColumn==-1)
            throw new IllegalArgumentException(String.format("entry %s not found",colToWorkOn));

        //add the first column witch contains the header
        ArrayList<String>temp=new ArrayList<>();
        for(Object key:list[0].keySet())
            temp.add(key.toString());
        data.add(temp);

        //add the rest of values
        for(Map<?,?> row:list){
            temp=new ArrayList<>();
            for(Object value:row.values())
                temp.add(value.toString());
            mean=mean.add(new BigDecimal(String.valueOf(temp.get(indexOfColumn))));
            desiredColumnNumbers.add(Double.parseDouble(temp.get(indexOfColumn)));
            data.add(temp);
        }

    }
    private void createNewJSONFile(Path destPath, String normalizationType, ScoringSummary summary) {
        //get map list ready
        ArrayList<Map<String,String>>mapList=new ArrayList<>();
        ArrayList<String> firstRow =data.remove(0);
        firstRow.add(indexOfColumn+1,firstRow.get(indexOfColumn)+((normalizationType.equals("min-max"))?"_mm":"_z"));

        //add normalization data column
        BigDecimal currentValue;
        if(normalizationType.equals("min-max")){
            //in minMax we use (x-min)/(max-min)
            for(ArrayList<String>line:data){
                currentValue=new BigDecimal(String.valueOf(line.get(indexOfColumn)));
                currentValue=currentValue.subtract(summary.min());
                currentValue=currentValue.divide(summary.max().subtract(summary.min()),RoundingMode.HALF_EVEN);

                line.add(indexOfColumn+1,currentValue.toString());
            }
        }else{
            //in z-score we use (x-mean)/std
            for(ArrayList<String>line:data){
                currentValue=new BigDecimal(String.valueOf(line.get(indexOfColumn)));
                currentValue=currentValue.subtract(summary.mean());
                currentValue=currentValue.divide(summary.standardDeviation(),RoundingMode.HALF_EVEN);
                line.add(indexOfColumn+1,currentValue.toString());
            }
        }
        //adding the new data to mapList
        Map<String,String>temp;
        for(ArrayList<String>row:data){
            temp=new HashMap<>();
            for(int i = 0; i< firstRow.size(); i++)
                temp.put(firstRow.get(i),row.get(i));
            mapList.add(temp);
        }

        //writing the mapList to the json file
        Writer writer;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            writer = Files.newBufferedWriter(destPath);
            gson.toJson(mapList, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private void createNewCSVFile(Path destPath, String normalizationType, ScoringSummary summary){
        //create new file and add normalized column
        PrintWriter writer;
        try {
            writer = new PrintWriter(destPath.toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        ArrayList<String>firstRow=data.remove(0);
        firstRow.add(indexOfColumn+1,firstRow.get(indexOfColumn)+((normalizationType.equals("min-max"))?"_mm":"_z"));

        writer.println(String.join(",",firstRow));
        BigDecimal currentValue;
        if(normalizationType.equals("min-max")){
            //in minMax we use (x-min)/(max-min)
            for(ArrayList<String>line:data){
                currentValue=new BigDecimal(String.valueOf(line.get(indexOfColumn)));
                currentValue=currentValue.subtract(summary.min());
                currentValue=currentValue.divide(summary.max().subtract(summary.min()),RoundingMode.HALF_EVEN);

                line.add(indexOfColumn+1,currentValue.toString());
                writer.println(String.join(",",line));
            }
        }else{
            //in z-score we use (x-mean)/std
            for(ArrayList<String>line:data){
                currentValue=new BigDecimal(String.valueOf(line.get(indexOfColumn)));
                currentValue=currentValue.subtract(summary.mean());
                currentValue=currentValue.divide(summary.standardDeviation(),RoundingMode.HALF_EVEN);

                line.add(indexOfColumn+1,currentValue.toString());
                writer.println(String.join(",",line));
            }
        }
        writer.close();
    }
}