import pytest

from filter import filter_message


@pytest.mark.asyncio()
async def test_publish_known_message(mock_send, mock_lookup):
    mock_lookup[9] = False

    key = {"reading_id": 9}
    value = {"value": 11, "timestamp": 1}

    await filter_message(key, value)

    mock_send.assert_awaited_once()
    mock_send.assert_awaited_with(key=key, value=value)


@pytest.mark.asyncio()
async def test_publish_unknown_message(mock_send, mock_lookup):
    key = {"reading_id": 9}
    value = {"value": 11, "timestamp": 1}

    await filter_message(key, value)

    mock_send.assert_awaited_once()
    mock_send.assert_awaited_with(key=key, value=value)


@pytest.mark.asyncio()
async def test_filter_out_known_message(mock_send, mock_lookup):
    mock_lookup[9] = True

    key = {"reading_id": 9}
    value = {"value": 11, "timestamp": 1}

    await filter_message(key, value)

    mock_send.assert_not_awaited()
