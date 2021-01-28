install:
	pipenv install --dev

start-filters:
	docker-compose up --build --detach filter0 filter1 > /dev/null

	@echo "Waiting for the filter to run"

	@until [ $${counter:-0} -gt 60 ]; \
	do \
		if docker-compose logs filter0 | grep -q ": Ready" \
			&& docker-compose logs filter1 | grep -q ": Ready"; then \
			break; \
		fi; \
		counter=$$(($$counter+1)); \
	done;

	docker-compose logs filter0 | grep -q ": Ready"
	docker-compose logs filter1 | grep -q ": Ready"
	@echo "filter started and ready"

bootstrap-kafka:
	docker-compose run --rm kafka-cli

bootstrap: teardown bootstrap-kafka start-filters

e2e-test: bootstrap e2e-test-no-bootstrap
	@# need to stop manually as `teardown` is included in bootstrap
	@# and Makefile does not execute a recipe twice
	docker-compose down --remove-orphans --volumes --timeout=5

e2e-test-no-bootstrap:
	@echo Run as 'make e2e-test args="-w"' to pass flags
	PYTHONPATH=src pipenv run behave tests/e2e -q $(args)

unit-test:
	@echo Run as 'make unit-test args="-s -v"' to pass flags
	PYTHONPATH=src pipenv run python -m pytest tests/unit $(args)

linting-test:
	@echo
	@pipenv run flake8 && echo "===> Linting passed! <==="
	@echo

test: linting-test unit-test e2e-test

teardown:
	docker-compose down --remove-orphans --volumes --timeout=5

