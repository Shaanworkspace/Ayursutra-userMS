# Official AWS Lambda Java 21 base image (Ye mandatory hai)
FROM public.ecr.aws/lambda/java:21

# Aapka JAR copy karo (target folder se)
COPY target/*.jar ${LAMBDA_TASK_ROOT}/app.jar

# Handler class (Ye class aapko banana padega, niche code diya hai)
CMD ["com.user.StreamLambdaHandler::handleRequest"]