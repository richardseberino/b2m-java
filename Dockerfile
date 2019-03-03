FROM websphere-liberty:18.0.0.4-microProfile1
COPY target/rsapp.war /config/dropins/
COPY src/main/liberty/config/server.xml /config/
RUN installUtility install --acceptLicense defaultServer
