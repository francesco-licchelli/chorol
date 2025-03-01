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

                provide
                    [ f2 (request)(response)]{
                        response = 1
                    }
                until
                    [ f3 () () { nullProcess}]
            }]


        until
            [ f4( )( ) ]{ nullProcess }
    }
}