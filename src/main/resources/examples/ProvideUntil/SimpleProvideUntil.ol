service Main {
    inputPort input{
        requestResponse:
            f1(any)(any), f2(any)(any)
            location: "socket://localhost:8000"
            protocol: http
    }


    main {
        provide
            [ f1( request )( response ) {
                nullProcess
            }]


        until
            [ f2( )( ) ]{
                nullProcess
            }
    }
}