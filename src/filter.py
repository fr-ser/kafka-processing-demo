import json

from loguru import logger

from config import (
    SOURCE_TOPIC_NAME, DESTINATION_TOPIC_NAME,
    FILTER_CONDITION_TOPIC_NAME, TABLE_NAME,
)
from app import get_app, crash_app

app = get_app()


# Using the faust json serializer leads to committing messages
# even with a SerializationError
"""
key={"reading_id": 14}
value={"value": 4, "timestamp": 1606149557}
"""
filter_condition_topic = app.topic(
    FILTER_CONDITION_TOPIC_NAME,
    key_serializer="raw", value_serializer="raw", compacting=True, allow_empty=True,
)

"""
key={"reading_id": 2}
value={"value": 4, "timestamp": 1606149557}
"""
source_topic = app.topic(SOURCE_TOPIC_NAME, key_serializer="raw", value_serializer="raw")

destination_topic = app.topic(DESTINATION_TOPIC_NAME,
                              key_serializer="json", value_serializer="json")


is_sensitive_lookup = app.GlobalTable(TABLE_NAME)
"""
The table looks like this:
{
    1: False,
    {reading_id}: {is_sensitive},
    ...
}
"""


async def filter_message(key, value):
    """
    Filter messages and publish the to destination topic unchanged
    In case the lookups returns nothing, the message is published
    :param key: {"reading_id": 2}
    :param value: {"value": 4, "timestamp": 1606149557}
    """

    if not is_sensitive_lookup.get(key["reading_id"]):
        await destination_topic.send(key=key, value=value)


async def generate_lookup(key, value):
    """
    :param key: {"reading_id": 4}
    :param value: {"is_sensitive": False}. Can be a Tombstone
    """

    if not value:
        if key["reading_id"] not in is_sensitive_lookup:
            # it might just not be synced here
            # but to delete it, we need it in the table
            is_sensitive_lookup[key["reading_id"]] = False
        is_sensitive_lookup.pop(key["reading_id"], None)
    else:
        is_sensitive_lookup[key["reading_id"]] = value["is_sensitive"]


@app.agent(filter_condition_topic)
async def lookup_agent(stream):
    try:
        async for event in stream.noack().events():
            if event.value:
                value = json.loads(event.value)
            else:
                value = event.value
            await generate_lookup(json.loads(event.key), value)

            await stream.ack(event)

    except Exception:
        logger.exception("Critical Exception in is_sensitive_lookup agent. Exiting")
        await crash_app(app)


@app.agent(source_topic)
async def filter_agent(stream):
    logger.info("Agent started. Ready to consume")
    try:
        async for event in stream.noack().events():
            await filter_message(json.loads(event.key), json.loads(event.value))
            await stream.ack(event)

    except Exception:
        logger.exception("Critical Exception in conversion agent. Exiting")
        await crash_app(app)


if __name__ == "__main__":
    app.main()
