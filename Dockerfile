FROM websphere-liberty:18.0.0.4-microProfile1
COPY target/rsapp.war /config/dropins/
COPY final/server.xml /config/
COPY server.env /config/

RUN installUtility install --acceptLicense defaultServer
