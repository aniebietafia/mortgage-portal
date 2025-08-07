# Mortgage Portal Management API

# Docker compose
# To run the application, use the following command:
```bash
docker compose up
```
# To run the application with a specific profile, use the following command:
```bash
docker compose --profile <profile_name> up
```

* Running docker-compose up will start the core services (app, postgres, kafka, zookeeper).
* Create profile-specific property files in the `src/main/resources` directory, such as `application-dev.properties`, `application-prod.properties`, etc.
* The application will automatically load the appropriate properties file based on the active profile.
* Running `docker-compose --profile dev` up will start the core services plus any services in the dev profile (like kafka-ui).
* Secrets are loaded from the `application.properties` file and are not hardcoded. For production, you can use a separate .env.prod file or manage secrets using Docker Secrets or another secrets management tool.