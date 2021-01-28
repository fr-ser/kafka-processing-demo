from collections.abc import MutableMapping
from unittest import mock

import asynctest
import pytest

from app import get_app


@pytest.fixture()
def mock_send():
    with asynctest.patch("filter.destination_topic.send") as m:
        yield m


@pytest.fixture()
def test_app(event_loop):
    # passing in event_loop helps avoid
    # 'attached to a different loop' error
    app = get_app()
    app.finalize()
    app.flow_control.resume()
    return app


@pytest.fixture()
def mock_lookup(test_app):
    is_sensitive_lookup = MockTable()

    with mock.patch("filter.is_sensitive_lookup", new=is_sensitive_lookup) as m:
        yield m


def get_finite_mock_stream(app, stream_content):
    mock_stream = mock.MagicMock()

    async def mock_async_iterable():
        for item in stream_content:
            yield item

    mock_stream.noack.return_value.noack_take.return_value = mock_async_iterable()
    mock_stream.noack.return_value.events.return_value = mock_async_iterable()
    mock_stream.ack = asynctest.CoroutineMock()

    return mock_stream


class MockTable(MutableMapping):
    def __init__(self, default=None):
        self.default = default

        # make table mostly behave like a dictionary
        self.dict_ = {}

    def __getitem__(self, key):
        if key in self.dict_:
            return self.dict_[key]
        elif self.default:
            return self.default()
        else:
            raise KeyError(f"'{key}' not found in MockTable")

    def __setitem__(self, key, value):
        self.dict_[key] = value

    def __delitem__(self, key):
        del self.dict_[key]

    def __iter__(self):
        return self.dict_.__iter__()

    def __len__(self):
        return len(self.dict_)

    def __contains__(self, x):
        return self.dict_.__contains__(x)
