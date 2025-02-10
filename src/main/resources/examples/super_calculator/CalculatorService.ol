from .CalculatorInterfaceModule import CalculatorInterface

service CalculatorService {
    /*
    Definisco la modalita' di esecuzione del servizio:
    Posso usare 3 modalita':
    concurrent, single o sequential
    */
    execution: concurrent
    inputPort CalculatorPort {
        protocol: http {
            .format = "json"
            .method = "post"
        }
        interfaces: CalculatorInterface
        location: "socket://localhost:8000"
    }

    outputPort Twitter {
        location: "socket://api.x.com:443/"
        protocol: https {
            .osc.user_timeline.method = "get"
            .osc.user_timeline.alias = "2/tweets/search/recent?query=from%3Aelonmusk&max_results=10&tweet.fields=author_id,geo,note_tweet,source,text"
            .osc.user_timeline.outHeaders.("Authorization") = "Bearer "+"BLA BLA BLA"
        }
        RequestResponse: user_timeline, rr1, rr2, rr3, rr4, rr5, rr6, rr7
        OneWay: ow1, ow2
    }


    main {
        [ sum( request )( response ) {
            for ( t in request.term ) {
                response = response + t
            }
        }]

        [ sub( request )( response ) {
            response = request.minuend - request.subtrahend
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
            response = "shut down"
            exit
        }
    }
}

/*
    La lista delle operazioni viene specificata con le input choices, e questo viene rappresentato con le []. Quando ci sono piu' operazioni nelle input choices queste sono tutte disponibili ma solo quella che riceve un messaggio viene eseguita. La sintassi completa e':
    [ inputOperation ]{ post -operation code }


*/