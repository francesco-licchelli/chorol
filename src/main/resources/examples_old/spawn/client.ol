
from .TemperatureCollectorInterface import TemperatureCollectorInterface
from console import Console

service Client {
    outputPort TemperatureCollector {
      Location: "socket://localhost:9000"
      Protocol: sodep
      Interfaces: TemperatureCollectorInterface
    }
    embed Console as Console

    main {
      getAverageTemperature@TemperatureCollector()( response );
      println@Console( response )()
    }
}
