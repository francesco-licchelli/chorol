service Main {
    inputPort input{
        requestResponse:
            op1( any )( any ), op2( any )( any ), op3( any )( any ), op4( any )( any )
            location: "socket://localhost:8000"
            protocol: http
    }


    main {
        provide
            [ op1( request )( response ) {
                nullProcess
            }]

            [ op2( request )( response ) {
                nullProcess
            }]

            [ op3( request )( response ) {
                nullProcess
            }]


        until
            [ op4( )( ) ]{
                nullProcess
            }
    }
}