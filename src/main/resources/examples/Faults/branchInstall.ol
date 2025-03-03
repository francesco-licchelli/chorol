service Main {
  outputPort output{
        oneWay:
            op1( void ), op2( void ), op3( void ), op4( void ), op5( void ), op6( void ), op7( void ), op8( void )
    }
    main {
        if ( condition1 ){
            if ( condition1_1 ){
                if ( condition1_1_1 )
                    install( SampleFault => op1@output() )
                else
                    install( SampleFault => op2@output() )
            }
            else{
                if ( condition1_0_1 )
                    install( SampleFault => op3@output() )
                else
                    install( SampleFault => op4@output() )
            }
        }
        else {
            if ( condition0_1 ){
                if ( condition0_1_1 )
                    install( SampleFault => op5@output() )
                else
                    install( SampleFault => op6@output() )
            }
            else{
                if ( condition0_0_1 )
                    install( SampleFault => op7@output() )
                else
                    install( SampleFault => op8@output() )
            }
        }

        throw SampleFault( void )
    }
}