import os


MONITORING_INTERVAL_S = int(os.environ.get("MONITORING_INTERVAL_S", "60"))
LOG_LEVEL = os.environ.get("LOG_LEVEL", "INFO")

CONSUMER_GROUP_ID = "readings-filter"
_BOOTSTRAP_SERVERS = os.environ.get("BOOTSTRAP_SERVERS", "localhost:9092")
FAUST_BROKERS = f'kafka://{_BOOTSTRAP_SERVERS.replace(",", ";")}'
SOURCE_TOPIC_NAME = os.environ.get("SOURCE_TOPIC", "readings")
DESTINATION_TOPIC_NAME = os.environ.get("DESTINATION_TOPIC", "filtered-readings")
FILTER_CONDITION_TOPIC_NAME = os.environ.get("FILTER_TOPIC", "filter-condition")
TABLE_NAME = "lookup"
# it is not used in code, but this name is default for a global table
# in the Faust framework (and we cannot change it)
GLOBAL_TABLE_CHANGELOG_TOPIC = f"{CONSUMER_GROUP_ID}-{TABLE_NAME}-changelog"
