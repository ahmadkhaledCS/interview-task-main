package com.interviewtest.tools;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {

        String error="arguments error";
        error+="\njava -jar interview-task-1.0-SNAPSHOT.jar [SOURCE_PATH] [DEST_PATH] [COLUMN_TO_NORMALIZE] [NORMALIZATION_METHOD]";
        error+="\n[NORMALIZATION_METHOD] : min-max|z-score";

        Path SOURCE_PATH;
        Path DEST_PATH ;
        String COLUMN_TO_NORMALIZE;
        String NORMALIZATION_METHOD;

        try {
            SOURCE_PATH= Paths.get(args[0]);
            DEST_PATH = Paths.get(args[1]);
            COLUMN_TO_NORMALIZE=args[2];
            NORMALIZATION_METHOD=args[3];
        }catch (IndexOutOfBoundsException e){
            throw new IllegalArgumentException(error);
        }

        AhmadNormalizer normalizer=new AhmadNormalizer();

        if(NORMALIZATION_METHOD.equals("min-max")){
            System.out.println("working on it");
            normalizer.minMaxScaling(SOURCE_PATH,DEST_PATH,COLUMN_TO_NORMALIZE);
            System.out.println("finished");
        }
        else if(NORMALIZATION_METHOD.equals("z-score")){
            System.out.println("working on it");
            normalizer.zscore(SOURCE_PATH,DEST_PATH,COLUMN_TO_NORMALIZE);
            System.out.println("done with no errors");
        }
        else
            System.out.println("invalid NORMALIZATION_METHOD");
    }

}
