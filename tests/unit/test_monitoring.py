from unittest import mock

from monitoring import PerSecondCounter


def test_per_second_counter():
    with mock.patch("monitoring.time.time", new=lambda: 20):
        counter = PerSecondCounter()

    with mock.patch("monitoring.time.time", new=lambda: 40):
        first_stats = counter(10)
    assert first_stats == 0.5

    with mock.patch("monitoring.time.time", new=lambda: 50):
        second_stats = counter(110)
    assert second_stats == 10
