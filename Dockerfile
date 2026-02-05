# AWS Lambda official Java 21 base image (Ye mandatory hai)
FROM public.ecr.aws/lambda/java:21

# Aapka JAR copy karo
COPY target/*.jar ${LAMBDA_TASK_ROOT}/app.jar

# Handler class (Ye aapko banana padega, code change sirf 1 file)
CMD ["com.user.StreamLambdaHandler::handleRequest"]