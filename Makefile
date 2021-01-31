include bootstrap.env
export

install:
	pipenv install --dev

bootstrap: teardown
	docker-compose run --rm kafka-cli

integration-test: bootstrap integration-test-no-bootstrap
	@# need to stop manually as `teardown` is included in bootstrap
	@# and Makefile does not execute a recipe twice
	docker-compose down --remove-orphans --volumes --timeout=5

integration-test-no-bootstrap:
	@echo -n "Is the app running? [y/N] " && read ans && [ $${ans:-N} = y ]
	mvn clean integration-test -P integration-tests

unit-test:
	mvn clean test

test: unit-test integration-test

run:
	mvn compile && mvn exec:java -Dexec.mainClass="com.example.reading_filter.App"

teardown:
	docker-compose down --remove-orphans --volumes --timeout=5

