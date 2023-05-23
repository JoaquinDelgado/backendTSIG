# Utilizar una imagen base con Maven y Java
FROM jelastic/maven:3.8.6-openjdk-20.ea-b9 AS build
# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el archivo pom.xml y el código fuente al contenedor
COPY pom.xml .
COPY src ./src

# Compilar el proyecto utilizando Maven
USER root
RUN mvn clean package -DskipTests

# Establecer una nueva imagen base con Java
FROM openjdk:20-oracle AS runtime

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el archivo JAR generado durante la compilación al contenedor
COPY --from=build /app/target/tsig-0.0.1-SNAPSHOT.jar .

# Puerto expuesto por la aplicación Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación Spring Boot al iniciar el contenedor
CMD ["java", "-jar", "tsig-0.0.1-SNAPSHOT.jar"]