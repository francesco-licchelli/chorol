from .CalculatorInterfaceModule import CalculatorInterface
from console import Console

 service CalculatorService {

     execution: concurrent

     embed Console as Console

     inputPort CalculatorPort {
         location: "socket://localhost:8000"
         protocol: http { format = "json" }
         interfaces: CalculatorInterface
     }     

     main {

         [ sum( request )( response ) {
             for( t in request.term ) {
                 response = response + t
             }
         }] {
            if (response == 5)
                exit
            else if (response == 6)
                exit
            else if (response == 8)
                exit
		    else exit
         }

         [ sub( request )( response ) {
             response = request.minuend - request.subtraend
         }]

         [ mul( request )( response ) {
             response = 1
             for ( f in request.factor ) {
                 response = response * f 
             }
         }]

         [ div( request )( response ) {
             response = request.dividend / request.divisor
         }]

        [ shutdown( )( ) ]{
            exit
        }
     }

 }
