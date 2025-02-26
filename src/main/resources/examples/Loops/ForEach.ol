from .ExampleInterface import ExampleInt
service example {

    outputPort output{
        location: "socket://localhost:0000"
        interfaces: ExampleInt
    }


    main {
        for (elem in array){
            op1@output(void)
        }
    }
}
