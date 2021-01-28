# Kafka Processing Demo

Demo Repo to compare different processing frameworks and languages for Kafka

The implementation can be found in branches

- Python with Faust: <https://github.com/fr-ser/kafka-processing-demo/tree/python-faust>
- (horrible) Java with KafkaStreams: <https://github.com/fr-ser/kafka-processing-demo/tree/java-kafka-streams>
- (amateurish) Scala with KafkaStreams: <https://github.com/fr-ser/kafka-processing-demo/tree/scala-kafka-streams>

## Notes

We use a two nodes in the docker-compose to stay more realistic and also have multiple "workers"
running to show the scaling scenario in the e2e tests.
