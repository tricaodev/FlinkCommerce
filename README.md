# System Architecture
![system_architecture](https://github.com/user-attachments/assets/e4835095-441e-44f0-93e8-0575a9cee604)

# How to run app
## 1. Build system architecture
```docker-compose up -d```
## 2. Run file GenerateData/TransactionData.java to produce data to kafka topic
## 3. Submit Flink Job
* ```mvn clean compile package```
* ```docker exec -it flink-jobmanager bash```
* ```bin/flink run target/FlinkCommerce-1.0-SNAPSHOT.jar```
## 4. View data
* Postgres:
  ```docker exec -it postgres bash```
  ```psql -h localhost -p 5432 -U admin -d postgres```
  ```select * from transactions;```
* Elasticsearch: Launch to ```http://localhost:5601``` and create visualization dashboard
