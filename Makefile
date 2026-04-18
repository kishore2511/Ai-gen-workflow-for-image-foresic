.PHONY: up up-ml up-backend test test-backend test-ml e2e

up:
	docker compose up --build

up-ml:
	cd ml-service && uvicorn app:app --reload --port 8000

up-backend:
	cd backend-java && mvn spring-boot:run

test: test-backend test-ml

test-backend:
	cd backend-java && mvn test

test-ml:
	cd ml-service && python -m pytest -q

e2e:
	@echo "Run backend + ml-service, then execute API smoke tests (docs/e2e.md)"
