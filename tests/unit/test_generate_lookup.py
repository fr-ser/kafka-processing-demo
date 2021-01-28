import pytest

from filter import generate_lookup


@pytest.mark.asyncio()
async def test_generate_lookup(mock_lookup):
    key = {"reading_id": 4}
    value = {"is_sensitive": True}

    await generate_lookup(key, value)

    assert mock_lookup[4] is True


@pytest.mark.asyncio()
async def test_delete(mock_lookup):
    mock_lookup[4] = True
    key = {"reading_id": 4}
    value = None  # tombstone

    await generate_lookup(key, value)

    assert 4 not in mock_lookup


@pytest.mark.asyncio()
async def test_delete_not_pre_existing(mock_lookup):
    key = {"reading_id": 4}
    value = None  # tombstone

    await generate_lookup(key, value)

    assert 4 not in mock_lookup
