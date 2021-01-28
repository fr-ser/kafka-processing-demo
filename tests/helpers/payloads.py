from collections import namedtuple
import json

SimpleEvent = namedtuple("SimpleEvent", ("key", "value"))


def get_reading_msg(reading_id=1, timestamp=2, value=3, _raw=False):

    key = {"reading_id": reading_id}
    value = {"value": value, "timestamp": timestamp}

    if _raw:
        return SimpleEvent(json.dumps(key), json.dumps(value))
    else:
        return SimpleEvent(key, value)


def get_filter_condition_msg(reading_id=1, is_sensitive=True, _raw=False):
    key = {"reading_id": reading_id}
    value = {"is_sensitive": is_sensitive}

    if _raw:
        return SimpleEvent(json.dumps(key), json.dumps(value))
    else:
        return SimpleEvent(key, value)
