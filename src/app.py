import os
import signal
import sys

import faust
from loguru import logger

from config import (
    LOG_LEVEL, MONITORING_INTERVAL_S,
    FAUST_BROKERS, CONSUMER_GROUP_ID,
)
from monitoring import monitoring


def _setup_logging():
    logger.remove()
    if LOG_LEVEL == "DEBUG":
        logger.add(sys.stdout, level=LOG_LEVEL)
    else:
        logger.add(
            sys.stdout, colorize=False, level=LOG_LEVEL, serialize=True,
            backtrace=False, diagnose=False,
        )


def get_app():
    _setup_logging()

    app_name = CONSUMER_GROUP_ID
    app = faust.App(app_name, broker=FAUST_BROKERS)
    app.config_from_object({
        "consumer_auto_offset_reset": "latest",
        "topic_allow_declare": False,
        "table_standby_replicas": 0,
        "topic_disable_leader": True,
    })

    @app.timer(MONITORING_INTERVAL_S)
    async def call_monitor():
        monitoring(app.monitor)

    # setup alarm handler (crash app on errors)
    signal.signal(signal.SIGALRM, alarm_handler)

    return app


def alarm_handler(signum, frame):
    logger.info(f'Alarm signal handler called with signal {signum}')
    # os._exit is a very severe exit.
    # It does not close handlers or flush buffers.
    # But here it is our only choice, to also crash docker containers
    os._exit(1)


async def crash_app(app):
    # raise an alarm
    # and stop anyways if after 30 seconds the app is still running
    signal.alarm(30)

    logger.info("Initiating stopping application")
    await app.stop()
    logger.debug("Stopping finished")
    # this does not work in docker containers and the alarm signal
    # takes over then
    sys.exit(1)
