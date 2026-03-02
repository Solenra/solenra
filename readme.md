# Solenra

A solar system payback calculator.

## Supported platforms

* SolarEdge

## Screenshots

![Solar System ROI screenshot](docs/images/solar_system_roi_screenshot.png?raw=true "Solar System ROI screenshot")

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

The things you need before installing the software.

* Java 25
* Node.js 25

### Installation

A step by step guide that will tell you how to get the development environment up and running.

#### Run API server
```
$ cd server
$ mvnw spring-boot:run
```

#### Run Client server
```
$ cd client
$ npm install
$ npm run start_8080_proxy
```

## Docker deployment with PostgreSQL database

```
$ git clone https://github.com/Solenra/Solenra.git
$ cd Solenra
$ docker build --progress=plain --no-cache -t solenra:latest .
$ docker-compose -f docker-compose.postgres.yml up --build -d
```

### Configuration

Edit the .env file to configure the web application.

| Property | Default | Description |
| ------------- | ------------- | ------------- |
| ADMIN_USERNAME | admin | The username for the administrator account. |
| ADMIN_INITIAL_PASSWORD | password | The initial password for the administrator account. |
| ADMIN_INITIAL_EMAIL | admin@invalid | The initial email for the administrator account. |
| BASE_URL | http://localhost:8080 | Base URL for the web application. |
| POSTGRES_USER | solenra | DB user. |
| POSTGRES_PASSWORD | supersecret | DB password. |
| DB_DATA_LOCATION |  | Directory on host for database docker volume. |
| JWT_BASE64_ENCODED_SECRET | random | Base64-encoded secret for signing of JWT tokens. Configure this to keep tokens valid across application restarts. |
