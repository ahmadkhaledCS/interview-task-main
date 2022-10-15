This jar can take a csv or a json as input and can output in csv or json as well
important note : use jre 9

From csv to csv
java -jar interview-task-1.0-SNAPSHOT.jar marks.csv marks_normalized.csv mark z-score

From csv to json
java -jar interview-task-1.0-SNAPSHOT.jar marks.csv marks_normalized.json mark z-score

From json to csv
java -jar interview-task-1.0-SNAPSHOT.jar marks.json marks_normalized.csv mark z-score

From json to json
java -jar interview-task-1.0-SNAPSHOT.jar marks.json marks_normalized.json mark z-score