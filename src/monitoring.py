import time

from loguru import logger


class PerSecondCounter:
    def __init__(self):
        self.previous_time = time.time()
        self.previous_count = 0

    def __call__(self, current_count):
        count_time = time.time()
        stats = round(
            (current_count - self.previous_count) / (count_time - self.previous_time),
            1,
        )

        self.previous_count = current_count
        self.previous_time = count_time

        return stats


rx_msg_counter = PerSecondCounter()
tx_msg_counter = PerSecondCounter()


def monitoring(monitor):
    log_info = {
        "type": "faust",
        "messages_received": monitor.messages_received_total,
        "messages_received_per_s": rx_msg_counter(monitor.messages_received_total),
        "rebalances": monitor.rebalances,
        "messages_sent": monitor.messages_sent,
        "messages_sent_per_s": tx_msg_counter(monitor.messages_sent),
    }
    logger.bind(**log_info).info("Monitoring")
    logger.debug(log_info)
