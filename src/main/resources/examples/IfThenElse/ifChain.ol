from .ExampleInterface import ExampleInt
service example {

    outputPort output{
        location: "socket://localhost:0000"
        interfaces: ExampleInt
    }


    main {
        if ( condition1 ){
            op1@output(void)
        } else if ( condition2 ){
            op2@output(void)
        } else if ( condition3 ){
            op3@output(void)
        } else {
            op4@output(void)
        }
    }
}
