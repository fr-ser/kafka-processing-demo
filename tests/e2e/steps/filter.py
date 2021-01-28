import json
import time

from behave import step
from confluent_kafka import Producer, KafkaError, Consumer

BOOTSTRAP_SERVERS = "localhost:9092,localhost:9093"

producer = Producer({"bootstrap.servers": BOOTSTRAP_SERVERS})
consumer = Consumer({"bootstrap.servers": BOOTSTRAP_SERVERS,
                     "auto.offset.reset": "latest", "group.id": f"test-{time.time()}"})


@step(u'we listen on the following kafka topic "{topic}"')
def start_consumer_step(context, topic):
    _assigned = False

    def _assign_cb(consumer, partitions):
        nonlocal _assigned
        _assigned = True
    consumer.subscribe([topic], on_assign=_assign_cb)

    end_time = time.time() + 10
    while time.time() < end_time and not _assigned:
        time.sleep(0.5)
        consumer.poll(timeout=0)

    if not _assigned:
        raise TimeoutError(f"No assignment for topic {topic} within 10s")


@step(u'we clear the conditions for "{reading_ids}" in "filter-condition"')
def publish_tombstone_step(context, reading_ids):
    for id_ in reading_ids.split(","):
        producer.produce("filter-condition", value=None, key=json.dumps({"reading_id": int(id_)}))

    outstanding_messages = producer.flush(timeout=15)
    if outstanding_messages > 0:
        raise KafkaError("Could not flush/publish message")


@step(u'we publish the following messages to "{topic}"')
def publish_step(context, topic):
    for row in context.table:
        producer.produce(topic, value=row["value"], key=row["key"])

    outstanding_messages = producer.flush(timeout=15)
    if outstanding_messages > 0:
        raise KafkaError("Could not flush/publish message")


@step(u'we wait {duration:d} seconds for the config to propagate')
def wait_step(context, duration):
    time.sleep(duration)


@step(u'we expect to find the following messages in "{topic}"')
def check_messages_step(context, topic):

    expected_messages = []
    for row in context.table:
        expected_messages.append(
            {
                "key": json.loads(row["key"]),
                "value": json.loads(row["value"]),
            }
        )

    received_messages = get_messages(topic, expected_message_count=len(expected_messages))

    expected_messages.sort(key=lambda x: x["key"]["reading_id"])
    received_messages.sort(key=lambda x: x["key"]["reading_id"])

    assert expected_messages == received_messages, (
        f"\nexpected: {json.dumps(expected_messages)}\n"
        f"received: {json.dumps(received_messages)}"
    )


def get_messages(topic, expected_message_count, timeout=10):
    end_time = time.time() + timeout
    last_msg_ts = 0
    received = []

    while time.time() < end_time:
        msg = consumer.poll(timeout=0.3)

        if msg is None:
            if len(received) == expected_message_count and (time.time() - last_msg_ts) > 1:
                return received
            elif len(received) > expected_message_count:
                raise AssertionError(
                    f"Expected {expected_message_count} messages"
                    f" but {len(received)} messages where received")
            time.sleep(0.5)
            continue

        if msg.error():
            raise KafkaError(msg.error())

        received.append({
            "key": json.loads(msg.key()),
            "value": json.loads(msg.value()),
        })

    raise AssertionError(
        f"timed out looking for {expected_message_count} messages"
        f"\nCurrent message count: {len(received)}"
    )
