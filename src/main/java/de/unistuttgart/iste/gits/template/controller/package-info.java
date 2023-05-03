/**
 * This package contains the controller classes. The controllers are used to handle the graphql requests.
 * <p>
 * <b>They <i>DO NOT</i> implement the business logic</b>. The business logic is implemented in the service classes.
 * <p>
 * A controller class is annotated with {@link org.springframework.stereotype.Controller @Controller}.
 * The methods are usually annotated with @QueryMapping, @SchemaMapping or @MutationMapping.
 * <p>
 * To get started with graphql in spring, see the <a href=https://www.baeldung.com/spring-graphql}>this tutorial</a>.
 * An example of our dummy backend would be <a href=https://github.com/IT-REX-Platform/dummy-backend/blob/master/api_gateway_service/src/main/java/de/unistuttgart/iste/GITS/apigateway/course/CourseController.java>
 * this controller</a>.
 */
package de.unistuttgart.iste.gits.template.controller;