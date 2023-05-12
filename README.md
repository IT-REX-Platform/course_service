# Course Service

## Description

The course service provides the information which courses are available as well as information about the chapters of each course. It also allows the creation of new courses, editing and deleting of existing ones. It provides information about which chapters are part of the course and their order, but does not know which content is a part of these chapters.

Who has access to the course and the different roles such as lecturer, teaching assistent and student are not handled by the course service. For these functionalities the course service invokes keycloak.

## GraphQL API

[Link to GraphQL API](schema.md)

The API is available at `/graphql` and the GraphiQL interface is available at `/graphiql`.
