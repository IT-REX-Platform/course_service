# Example graphQL queries

## Query 1: Create new course
```graphql
mutation {
    createCourse(
        input: {
            title: "New Course"
            description: "This is a new course"
            startDate: "2020-01-01T00:00:00.000Z"
            endDate: "2020-01-01T00:00:00.000Z"
            published: false
        }
    ) {
        id
        title
        description
        startDate
        endDate
        published
    }
}
```

## Query 2: Get all courses
```graphql
query {
    courses {
        id
        title
        description
        startDate
        endDate
        published
        chapters {
            id
        }
    }
}
```

## Query 3: Get course by id
```graphql
query {
    coursesById(ids: ["fcd6af01-50b9-4256-b7ad-e864df70040c"]) {
        id
        title
        description
        startDate
        endDate
        published
    }
}
```

## Query 4: Create new chapter
```graphql
mutation {
    createChapter(
        input: {
            title: "New Chapter"
            description: "This is a new chapter"
            courseId: "fcd6af01-50b9-4256-b7ad-e864df70040c"
            startDate: "2020-01-01T00:00:00.000Z"
            endDate: "2020-01-01T00:00:00.000Z"
            number: 1
        }
    ) {
        id
        title
        description
        course {
            id
            title
        }
    }
}
```

## Query 5: Update chapter
```graphql
mutation {
    updateChapter(
        input: {
            id: "54136a62-e81d-406e-99f5-aef64638b6dd"
            title: "Updated Chapter"
            description: "This is an updated chapter"
            startDate: "2020-01-01T00:00:00.000Z"
            endDate: "2020-01-01T00:00:00.000Z"
            number: 1
        }
    ) {
        id
        title
        description
        course {
            id
            title
        }
    }
}
```