import json

import asynctest
import pytest

from filter import filter_agent, lookup_agent

from .conftest import get_finite_mock_stream
from tests.helpers.payloads import get_filter_condition_msg, get_reading_msg, SimpleEvent


@pytest.mark.asyncio()
@pytest.mark.parametrize("agent", [filter_agent, lookup_agent])
async def test_crash_and_not_commit_invalid_json(test_app, agent):
    mock_stream = get_finite_mock_stream(
        test_app, [SimpleEvent("key1", "val2"), SimpleEvent("key1", "val2")]
    )

    with asynctest.patch('filter.crash_app', autospec=True) as crash_mock:
        await agent.fun(mock_stream)
        crash_mock.assert_awaited_once()

    mock_stream.ack.assert_not_awaited()


@pytest.mark.asyncio()
@asynctest.patch('filter.crash_app', autospec=True)
@asynctest.patch('filter.filter_message', autospec=True, side_effect=Exception)
async def test_crash_and_not_commit_execution_error(filter_mock, crash_mock, test_app):
    mock_stream = get_finite_mock_stream(test_app, [
        get_reading_msg(_raw=True), get_reading_msg(_raw=True),
    ])

    await filter_agent.fun(mock_stream)

    mock_stream.ack.assert_not_awaited()
    crash_mock.assert_awaited_once()


@pytest.mark.asyncio()
@asynctest.patch('filter.crash_app', autospec=True)
@asynctest.patch('filter.filter_message', autospec=True)
async def test_commit_filter(filter_mock, crash_mock, test_app):
    events = [SimpleEvent("1", "2"), SimpleEvent("3", "4")]
    mock_stream = get_finite_mock_stream(test_app, events)

    await filter_agent.fun(mock_stream)

    assert filter_mock.await_count == 2
    for received, expected in zip(filter_mock.await_args_list, events):
        assert received[0] == (json.loads(expected.key), json.loads(expected.value))

    assert mock_stream.ack.await_count == 2


@pytest.mark.asyncio()
@asynctest.patch('filter.crash_app', autospec=True)
@asynctest.patch('filter.generate_lookup', autospec=True)
async def test_commit_lookup(generate_lookup_mock, crash_mock, test_app):
    events = [get_filter_condition_msg(_raw=True), get_filter_condition_msg(_raw=True)]
    mock_stream = get_finite_mock_stream(test_app, events)

    await lookup_agent.fun(mock_stream)

    assert generate_lookup_mock.await_count == 2
    for received, expected in zip(generate_lookup_mock.await_args_list, events):
        assert received[0] == (json.loads(expected.key), json.loads(expected.value))

    assert mock_stream.ack.await_count == 2
