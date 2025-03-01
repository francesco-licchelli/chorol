service Main {

  outputPort output{
        oneWay:
            f1(void), f2(void), f3(void), f4(void), f5(void), f6(void), f7(void), f8(void)
    }

    main {
        if (condizione1 ){
            if ( condizione1 ){
                if (condizione2)
                    install( SampleFault => f1@output() )
                else
                    install( SampleFault => f2@output() )
            }
            else{
                if (condizione3)
                    install( SampleFault => f3@output() )
                else
                    install( SampleFault => f4@output() )
            }
        }
        else {
            if ( condizione4 ){
                if (condizione5)
                    install( SampleFault => f5@output() )
                else
                    install( SampleFault => f6@output() )
            }
            else{
                if (condizione6)
                    install( SampleFault => f7@output() )
                else
                    install( SampleFault => f8@output() )
            }
        }

        throw SampleFault(void)
    }
}