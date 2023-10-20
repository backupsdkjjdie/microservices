#Get the jdk 11 as my application is also running on jdk 11
FROM openjdk:11
#Copy the jar file in container
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} configserver.jar
#Add the command which will run when container is started. It is giving by using ENTRYPOINT
ENTRYPOINT ["java","-jar","/configserver.jar"]
#Expose 8761 port container port to let the developers know
#The EXPOSE instruction is used to indicate which ports your container application may use.
EXPOSE 9296