.PHONY: build test run docker-up docker-down clean

build:
	cd backend && mvn clean package -DskipTests

test:
	cd backend && mvn test

run:
	docker-compose up -d postgres
	cd backend/auth-service && mvn spring-boot:run &
	cd backend/account-service && mvn spring-boot:run &
	cd backend/transaction-service && mvn spring-boot:run &
	cd backend/analytics-service && mvn spring-boot:run &
	cd backend/api-gateway && mvn spring-boot:run &
	cd frontend && npm run dev

docker-up:
	docker-compose up -d --build

docker-down:
	docker-compose down -v

clean:
	cd backend && mvn clean
	cd frontend && rm -rf node_modules dist
