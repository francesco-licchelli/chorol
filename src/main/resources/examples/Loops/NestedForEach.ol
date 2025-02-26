from .ExampleInterface import ExampleInt
service example {

    outputPort output{
        location: "socket://localhost:0000"
        interfaces: ExampleInt
    }


    main {
        for (elem1 in array1){
            op1@output(void)
            for (elem2 in array2){
                op2@output(void)
            }
            op3@output(void)
        }
    }
}
