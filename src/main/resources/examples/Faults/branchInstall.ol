interface ExampleFaultInt {
    oneWay:
        f1(void),
        f2(void),
        f3(void),
        f4(void)
    requestResponse:
        op(any)(any) throws
}




service Main {

  outputPort output{
        location: "socket://localhost:0000"
        interfaces: ExampleFaultInt
    }

    main {
        if ( condizione1 ){
            if (condizione2)
                install( InvalidNumberFormat => f1@output() )
            else
                install( InvalidNumberFormat => f2@output() )
        }
        else{
            if (condizione3)
                install( InvalidNumberFormat => f3@output() )
            else
                install( InvalidNumberFormat => f4@output() )
        }

        throw InvalidNumberFormat(void)
    }
}