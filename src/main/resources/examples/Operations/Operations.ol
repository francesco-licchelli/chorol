service Main{
    inputPort input{
        location: "socket://localhost:0000"
        protocol: http
        oneWay:
            iow( any )
        requestResponse:
            irr( any )( any )
    }

    outputPort output{
        oneWay:
            oww( any )
        requestResponse:
            orr( any )( any )
    }

    main{
        [irr( req )( res ){
            orr@output( void )( void )
        }]{
            oww@output( void )( void )
            res=4
        }
    }
}