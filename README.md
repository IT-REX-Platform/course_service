# Course Service

## Description

The course service provides the information which courses are available as well as information about the chapters of each course. It also allows the creation of new courses, editing and deleting of existing ones. It provides information about which chapters are part of the course and their order, but does not know which content is a part of these chapters.

Who has access to the course and the different roles such as lecturer, teaching assistent and student are not handled by the course service. For these functionalities the course service invokes keycloak.
## Environment variables
### Relevant for deployment
| Name                       | Description                        | Value in Dev Environment                        | Value in Prod Environment                                          |
|----------------------------|------------------------------------|-------------------------------------------------|--------------------------------------------------------------------|
| spring.datasource.url      | PostgreSQL database URL            | jdbc:postgresql://localhost:2032/course_service | jdbc:postgresql://course-service-db-postgresql:5432/course-service |
| spring.datasource.username | Database username                  | root                                            | gits                                                               |
| spring.datasource.password | Database password                  | root                                            | *secret*                                                           |
| DAPR_HTTP_PORT             | Dapr HTTP Port                     | 2000                                            | 3500                                                               |
| server.port                | Port on which the application runs | 2001                                            | 2001                                                               |

### Other properties
| Name                                    | Description                               | Value in Dev Environment                | Value in Prod Environment               |
|-----------------------------------------|-------------------------------------------|-----------------------------------------|-----------------------------------------|
| spring.graphql.graphiql.enabled         | Enable GraphiQL web interface for GraphQL | true                                    | true                                    |
| spring.graphql.graphiql.path            | Path for GraphiQL when enabled            | /graphiql                               | /graphiql                               |
| spring.profiles.active                  | Active Spring profile                     | dev                                     | prod                                    |
| spring.jpa.properties.hibernate.dialect | Hibernate dialect for PostgreSQL          | org.hibernate.dialect.PostgreSQLDialect | org.hibernate.dialect.PostgreSQLDialect |
| spring.sql.init.mode                    | SQL initialization mode                   | always                                  | always                                  |
| spring.jpa.show-sql                     | Show SQL queries in logs                  | true                                    | true                                    |
| spring.sql.init.continue-on-error       | Continue on SQL init error                | true                                    | true                                    |
| spring.jpa.hibernate.ddl-auto           | Hibernate DDL auto strategy               | update                                  | update                                  |
| DAPR_GRPC_PORT                          | Dapr gRPC Port                            | -                                       | 50001                                   |

## GraphQL API

The API documentation can be found in the wiki in the [API docs](api.md).

The API is available at `/graphql` and the GraphiQL interface is available at `/graphiql`.
