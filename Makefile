.PHONY: up up-ml up-backend test test-backend test-ml e2e deps-ml local-100

up:
	docker compose up --build

up-ml:
	cd ml-service && uvicorn app:app --reload --port 8000

up-backend:
	cd backend-java && mvn spring-boot:run

test: test-backend test-ml

test-backend:
	cd backend-java && mvn test

deps-ml:
	cd ml-service && python -m pip install -r requirements.txt

test-ml: deps-ml
	cd ml-service && python -m pytest -q

e2e:
	./scripts/local_e2e.sh

local-100:
	./scripts/local_100.sh
