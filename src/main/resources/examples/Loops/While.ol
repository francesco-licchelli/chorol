from .ExampleInterface import ExampleInt
service example {

    outputPort output{
        location: "socket://localhost:0000"
        interfaces: ExampleInt
    }
    main {
        while (condition) {
            op1@output(void)
        }
    }
}
