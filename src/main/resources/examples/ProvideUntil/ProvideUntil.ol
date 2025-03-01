service Main {
    inputPort input{
        requestResponse:
            f1(any)(any), f2(any)(any), f3(any)(any), f4(any)(any)
            location: "socket://localhost:8000"
            protocol: http
    }


    main {
        provide
            [ f1( request )( response ) {
                nullProcess
            }]

            [ f2( request )( response ) {
                nullProcess
            }]

            [ f3( request )( response ) {
                nullProcess
            }]




        until
            [ f4( )( ) ]{
                nullProcess
            }
    }
}