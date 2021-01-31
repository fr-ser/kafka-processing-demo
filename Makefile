install:
	sbt compile

bootstrap: teardown
	docker-compose run --rm kafka-cli

integration-test: bootstrap start-filter integration-test-no-bootstrap
	@# need to stop manually as `teardown` is included in bootstrap
	@# and Makefile does not execute a recipe twice
	docker-compose down --remove-orphans --volumes --timeout=5

integration-test-no-bootstrap:
	echo "Nothing yet" && exit 1

start-filter:
	docker-compose build filter-build-cache > /dev/null
	docker-compose up --build --detach filter > /dev/null

	@echo "Waiting for the filter"

	@until [ $${counter:-0} -gt 20 ]; \
	do \
		if docker-compose logs filter | grep -q "Current state is: RUNNING" ; then \
			break; \
		fi; \
		counter=$$(($$counter+1)); \
	done;

	docker-compose logs filter | grep -q "Current state is: RUNNING"
	@echo "filter started and ready"

unit-test:
	sbt test

test: unit-test integration-test

run:
	sbt run

build-docker-cache:
	docker-compose build filter-build-cache

teardown:
	docker-compose down --remove-orphans --volumes --timeout=5

