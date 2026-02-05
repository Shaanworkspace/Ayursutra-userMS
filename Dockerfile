# Stage 1: Build the JAR
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: AWS Lambda Runtime (Ye official base image hai)
FROM public.ecr.aws/lambda/java:21

# JAR copy karo
COPY --from=builder /app/target/*.jar ${LAMBDA_TASK_ROOT}/app.jar

# Handler class (Ye class aapko banana padega)
CMD ["com.user.StreamLambdaHandler::handleRequest"]