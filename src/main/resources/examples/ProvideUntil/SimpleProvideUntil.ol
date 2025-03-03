service Main {
    inputPort input{
        requestResponse:
            op1( any )( any ), op2( any )( any )
            location: "socket://localhost:8000"
            protocol: http
    }


    main {
        provide
            [ op1( request )( response ) {
                nullProcess
            }]


        until
            [ op2( )( ) ]{
                nullProcess
            }
    }
}