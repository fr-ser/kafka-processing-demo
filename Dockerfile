FROM python:3.7-slim-buster

RUN pip install pipenv

WORKDIR /app

COPY Pipfile Pipfile.lock /app/

RUN pipenv install --system --deploy

COPY src/ /app/

CMD ["faust", "-A", "filter", "worker", "-l", "info", "--without-web"]
