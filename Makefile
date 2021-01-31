include bootstrap.env
export

install:
	sbt compile

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
	sbt test

test: unit-test integration-test

run:
	sbt run

teardown:
	docker-compose down --remove-orphans --volumes --timeout=5

