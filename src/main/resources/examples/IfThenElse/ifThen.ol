from .ExampleInterface import ExampleInt
service example {

    outputPort output{
        location: "socket://localhost:0000"
        interfaces: ExampleInt
    }


    main {
        if ( condition ){
            op1@output(void)
        }
    }
}
