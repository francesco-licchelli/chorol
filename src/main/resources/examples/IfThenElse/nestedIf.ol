from .ExampleInterface import ExampleInt
service example {

    outputPort output{
        location: "socket://localhost:0000"
        interfaces: ExampleInt
    }


    main {
        if ( condition1 ){
            if ( condition1_1 ){ op1@output(void) }
            else if ( condition1_2 ){
                if (condition1_2_1) {
                    op2@output(void)
                }
            }
        }
    }
}